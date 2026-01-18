package ai.acolite.agentsdk.core.memory;

import ai.acolite.agentsdk.core.types.AgentInputItem;
import ai.acolite.agentsdk.openai.SerializationUtils;
import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SQLiteSession - Database-backed session storage
 *
 * <p>Stores conversation history in SQLite database with full persistence. Suitable for: -
 * Production applications requiring data persistence - Multi-session management - Conversation
 * history across restarts
 *
 * <p>Schema: - agent_sessions: Session metadata (session_id, created_at, updated_at) -
 * agent_messages: Conversation items (session_id, item_json, timestamp, sequence_num)
 *
 * <p>Thread-safety: Each operation uses the connection in a thread-safe manner. WAL mode enables
 * concurrent reads while writes are happening.
 *
 * <p>Ported from TypeScript OpenAI Agents SDK Source: https://github.com/openai/openai-agents-js
 * (DrizzleSession pattern)
 */
public class SQLiteSession implements Session, AutoCloseable {
  private static final Logger log = LoggerFactory.getLogger(SQLiteSession.class);

  @Getter private final String sessionId;

  private final Connection connection;
  private final String sessionsTable;
  private final String messagesTable;

  /**
   * Create SQLiteSession with in-memory database. Data is lost when connection closes.
   *
   * @param sessionId Unique session identifier
   * @return SQLiteSession instance
   * @throws SQLException if database initialization fails
   */
  public static SQLiteSession inMemory(String sessionId) throws SQLException {
    return new SQLiteSession(sessionId, ":memory:");
  }

  /**
   * Create SQLiteSession with file-based database. Data persists across application restarts.
   *
   * @param sessionId Unique session identifier
   * @param dbPath Path to SQLite database file
   * @return SQLiteSession instance
   * @throws SQLException if database initialization fails
   */
  public static SQLiteSession fromFile(String sessionId, Path dbPath) throws SQLException {
    return new SQLiteSession(sessionId, dbPath.toString());
  }

  /** Private constructor - use factory methods inMemory() or fromFile() */
  private SQLiteSession(String sessionId, String dbPath) throws SQLException {
    this.sessionId = sessionId;
    this.sessionsTable = "agent_sessions";
    this.messagesTable = "agent_messages";

    // Connect to database.
    this.connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);

    // Enable WAL mode for better concurrency.
    try (Statement stmt = connection.createStatement()) {
      stmt.execute("PRAGMA journal_mode=WAL");
    }

    initializeTables();
    ensureSessionExists();
  }

  private void initializeTables() throws SQLException {
    try (Statement stmt = connection.createStatement()) {
      // Sessions table.
      stmt.execute(
          String.format(
              """
                CREATE TABLE IF NOT EXISTS %s (
                    session_id TEXT PRIMARY KEY,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """,
              sessionsTable));

      // Messages table
      stmt.execute(
          String.format(
              """
                CREATE TABLE IF NOT EXISTS %s (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    session_id TEXT NOT NULL,
                    item_json TEXT NOT NULL,
                    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    sequence_num INTEGER NOT NULL,
                    FOREIGN KEY (session_id) REFERENCES %s(session_id)
                )
                """,
              messagesTable, sessionsTable));

      // Index for fast retrieval.
      stmt.execute(
          String.format(
              """
                CREATE INDEX IF NOT EXISTS idx_session_sequence
                ON %s(session_id, sequence_num)
                """,
              messagesTable));
      log.debug("SQLiteSession tables initialized");
    }
  }

  private void ensureSessionExists() throws SQLException {
    String sql = String.format("INSERT OR IGNORE INTO %s (session_id) VALUES (?)", sessionsTable);

    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
      stmt.setString(1, sessionId);
      stmt.executeUpdate();
    }
  }

  @Override
  public CompletableFuture<String> getSessionId() {
    return CompletableFuture.completedFuture(sessionId);
  }

  @Override
  public CompletableFuture<List<AgentInputItem>> getItems(Double limit) {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            if (limit != null && limit <= 0) {
              return new ArrayList<>();
            }

            StringBuilder sql =
                new StringBuilder(
                    String.format(
                        """
                    SELECT item_json FROM %s
                    WHERE session_id = ?
                    ORDER BY sequence_num ASC
                    """,
                        messagesTable));
            if (limit != null && limit > 0) {
              // SQLite LIMIT requires integer
              int limitInt = limit.intValue();
              // Get last N items by ordering desc and then reversing
              sql =
                  new StringBuilder(
                      String.format(
                          """
                        SELECT item_json FROM (
                            SELECT item_json, sequence_num FROM %s
                            WHERE session_id = ?
                            ORDER BY sequence_num DESC
                            LIMIT ?
                        ) ORDER BY sequence_num ASC
                        """,
                          messagesTable));

              try (PreparedStatement stmt = connection.prepareStatement(sql.toString())) {
                stmt.setString(1, sessionId);
                stmt.setInt(2, limitInt);
                return executeGetItems(stmt);
              }
            }

            try (PreparedStatement stmt = connection.prepareStatement(sql.toString())) {
              stmt.setString(1, sessionId);
              return executeGetItems(stmt);
            }
          } catch (SQLException e) {
            log.error("Failed to retrieve items from session: {}", sessionId, e);
            throw new RuntimeException("Failed to retrieve items from session", e);
          }
        });
  }

  private List<AgentInputItem> executeGetItems(PreparedStatement stmt) throws SQLException {
    ResultSet rs = stmt.executeQuery();
    List<AgentInputItem> items = new ArrayList<>();

    while (rs.next()) {
      String json = rs.getString("item_json");
      try {
        AgentInputItem item = SerializationUtils.deserializeFromJson(json, AgentInputItem.class);
        items.add(item);
      } catch (Exception e) {
        log.warn(
            "Failed to deserialize item from session {}, skipping: {}", sessionId, e.getMessage());
      }
    }

    log.debug("Retrieved {} items from session: {}", items.size(), sessionId);
    return items;
  }

  @Override
  public CompletableFuture<Void> addItems(List<AgentInputItem> items) {
    return CompletableFuture.runAsync(
        () -> {
          if (items == null || items.isEmpty()) {
            return;
          }

          try {
            connection.setAutoCommit(false);
            int nextSequence = getNextSequenceNumber();
            String sql =
                String.format(
                    """
                    INSERT INTO %s (session_id, item_json, sequence_num)
                    VALUES (?, ?, ?)
                    """,
                    messagesTable);

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
              for (AgentInputItem item : items) {
                String json = SerializationUtils.serializeToJson(item);

                stmt.setString(1, sessionId);
                stmt.setString(2, json);
                stmt.setInt(3, nextSequence++);
                stmt.addBatch();
              }

              stmt.executeBatch();
            }

            updateSessionTimestamp();
            connection.commit();
            connection.setAutoCommit(true);
          } catch (SQLException e) {
            try {
              connection.rollback();
              connection.setAutoCommit(true);
            } catch (SQLException rollbackEx) {
              log.error("Failed to rollback transaction", rollbackEx);
            }
            log.error("Failed to add items to session: {}", sessionId, e);
            throw new RuntimeException("Failed to add items to session", e);
          }
        });
  }

  @Override
  public CompletableFuture<Optional<AgentInputItem>> popItem() {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            // SQLite doesn't support RETURNING, so we do it in two steps
            String selectSql =
                String.format(
                    """
                    SELECT id, item_json FROM %s
                    WHERE session_id = ?
                    ORDER BY sequence_num DESC
                    LIMIT 1
                    """,
                    messagesTable);

            try (PreparedStatement stmt = connection.prepareStatement(selectSql)) {
              stmt.setString(1, sessionId);
              ResultSet rs = stmt.executeQuery();

              if (!rs.next()) {
                return Optional.empty();
              }

              int id = rs.getInt("id");
              String json = rs.getString("item_json");
              AgentInputItem item =
                  SerializationUtils.deserializeFromJson(json, AgentInputItem.class);

              // Delete the item
              String deleteSql = String.format("DELETE FROM %s WHERE id = ?", messagesTable);

              try (PreparedStatement deleteStmt = connection.prepareStatement(deleteSql)) {
                deleteStmt.setInt(1, id);
                deleteStmt.executeUpdate();
              }

              return Optional.of(item);
            }
          } catch (SQLException e) {
            log.error("Failed to pop item from session: {}", sessionId, e);
            throw new RuntimeException("Failed to pop item from session", e);
          }
        });
  }

  @Override
  public CompletableFuture<Void> clearSession() {
    return CompletableFuture.runAsync(
        () -> {
          try {
            String sql = String.format("DELETE FROM %s WHERE session_id = ?", messagesTable);

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
              stmt.setString(1, sessionId);
              stmt.executeUpdate();
            }
          } catch (SQLException e) {
            log.error("Failed to clear session: {}", sessionId, e);
            throw new RuntimeException("Failed to clear session", e);
          }
        });
  }

  @Override
  public void close() throws SQLException {
    if (connection != null && !connection.isClosed()) {
      connection.close();
      log.debug("SQLiteSession connection closed for: {}", sessionId);
    }
  }

  private int getNextSequenceNumber() throws SQLException {
    String sql =
        String.format(
            """
            SELECT COALESCE(MAX(sequence_num), -1) + 1 as next_seq
            FROM %s
            WHERE session_id = ?
            """,
            messagesTable);

    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
      stmt.setString(1, sessionId);
      ResultSet rs = stmt.executeQuery();
      rs.next();
      return rs.getInt("next_seq");
    }
  }

  private void updateSessionTimestamp() throws SQLException {
    String sql =
        String.format(
            "UPDATE %s SET updated_at = CURRENT_TIMESTAMP WHERE session_id = ?", sessionsTable);

    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
      stmt.setString(1, sessionId);
      stmt.executeUpdate();
    }
  }
}
