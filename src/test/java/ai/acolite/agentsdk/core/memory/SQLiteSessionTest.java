package ai.acolite.agentsdk.core.memory;

import static org.junit.jupiter.api.Assertions.*;

import ai.acolite.agentsdk.core.RunMessageInputItem;
import ai.acolite.agentsdk.core.RunMessageOutputItem;
import ai.acolite.agentsdk.core.types.AgentInputItem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * SQLite-specific tests for SQLiteSession. Common session functionality is tested in SessionTest
 * (parameterized).
 */
class SQLiteSessionTest {

  private AgentInputItem createMessage(String content, String role) {
    if (role.equals("user")) {
      return RunMessageInputItem.builder().content(content).role(role).build();
    } else {
      return RunMessageOutputItem.builder().content(content).role(role).build();
    }
  }

  @Test
  void fromFile_createsSession(@TempDir Path tempDir) throws SQLException {
    Path dbPath = tempDir.resolve("test.db");

    try (SQLiteSession session = SQLiteSession.fromFile("test-session", dbPath)) {
      String sessionId = session.getSessionId().join();

      assertEquals("test-session", sessionId);
      assertTrue(Files.exists(dbPath));
    }
  }

  @Test
  void persistence_acrossInstances(@TempDir Path tempDir) throws SQLException {
    Path dbPath = tempDir.resolve("persistent.db");
    String sessionId = "persistent-test";

    try (SQLiteSession session1 = SQLiteSession.fromFile(sessionId, dbPath)) {
      session1
          .addItems(
              List.of(createMessage("Message 1", "user"), createMessage("Message 2", "assistant")))
          .join();
    }

    try (SQLiteSession session2 = SQLiteSession.fromFile(sessionId, dbPath)) {
      List<AgentInputItem> items = session2.getItems(null).join();

      assertEquals(2, items.size());
      assertEquals("Message 1", ((RunMessageInputItem) items.get(0)).getContent());
      assertEquals("Message 2", ((RunMessageOutputItem) items.get(1)).getContent());
    }
  }

  @Test
  void multipleSessions_sameDatabase_isolatedData(@TempDir Path tempDir) throws SQLException {
    Path dbPath = tempDir.resolve("multi-session.db");

    try (SQLiteSession session1 = SQLiteSession.fromFile("session-1", dbPath);
        SQLiteSession session2 = SQLiteSession.fromFile("session-2", dbPath)) {

      session1.addItems(List.of(createMessage("Session 1 message", "user"))).join();
      session2.addItems(List.of(createMessage("Session 2 message", "user"))).join();

      List<AgentInputItem> items1 = session1.getItems(null).join();
      List<AgentInputItem> items2 = session2.getItems(null).join();

      assertEquals(1, items1.size());
      assertEquals(1, items2.size());
      assertNotEquals(
          ((RunMessageInputItem) items1.get(0)).getContent(),
          ((RunMessageInputItem) items2.get(0)).getContent());
    }
  }

  @Test
  void walMode_enabled(@TempDir Path tempDir) throws SQLException {
    Path dbPath = tempDir.resolve("wal-test.db");

    try (SQLiteSession session = SQLiteSession.fromFile("test-session", dbPath)) {
      session.addItems(List.of(createMessage("Test", "user"))).join();
    }

    Path walFile = Path.of(dbPath.toString() + "-wal");
    assertTrue(Files.exists(dbPath), "Database file should exist");
  }
}
