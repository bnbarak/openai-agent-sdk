package ai.acolite.agentsdk.core.memory;

import static org.junit.jupiter.api.Assertions.*;

import ai.acolite.agentsdk.core.RunMessageInputItem;
import ai.acolite.agentsdk.core.RunMessageOutputItem;
import ai.acolite.agentsdk.core.types.AgentInputItem;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class MemorySessionTest {

  private AgentInputItem createMessage(String content, String role) {
    if (role.equals("user")) {
      return RunMessageInputItem.builder().content(content).role(role).build();
    } else {
      return RunMessageOutputItem.builder().content(content).role(role).build();
    }
  }

  @Test
  void constructor_setsSessionId() {
    MemorySession session = new MemorySession("test-123");

    String sessionId = session.getSessionId().join();

    assertEquals("test-123", sessionId);
  }

  @Test
  void getItems_emptySession_returnsEmptyList() {
    MemorySession session = new MemorySession("test-session");

    List<AgentInputItem> items = session.getItems(null).join();

    assertTrue(items.isEmpty());
  }

  @Test
  void addItems_storesItems() {
    MemorySession session = new MemorySession("test-session");
    List<AgentInputItem> itemsToAdd =
        List.of(createMessage("Hello", "user"), createMessage("Hi there!", "assistant"));

    session.addItems(itemsToAdd).join();
    List<AgentInputItem> retrievedItems = session.getItems(null).join();

    assertEquals(2, retrievedItems.size());
    assertEquals("Hello", ((RunMessageInputItem) retrievedItems.get(0)).getContent());
    assertEquals("Hi there!", ((RunMessageOutputItem) retrievedItems.get(1)).getContent());
  }

  @Test
  void addItems_nullList_doesNotThrow() {
    MemorySession session = new MemorySession("test-session");

    assertDoesNotThrow(() -> session.addItems(null).join());

    List<AgentInputItem> items = session.getItems(null).join();
    assertTrue(items.isEmpty());
  }

  @Test
  void addItems_emptyList_doesNotThrow() {
    MemorySession session = new MemorySession("test-session");

    assertDoesNotThrow(() -> session.addItems(List.of()).join());

    List<AgentInputItem> items = session.getItems(null).join();
    assertTrue(items.isEmpty());
  }

  @Test
  void getItems_withLimit_returnsRecentItems() {
    MemorySession session = new MemorySession("test-session");
    List<AgentInputItem> itemsToAdd =
        List.of(
            createMessage("Message 1", "user"),
            createMessage("Message 2", "assistant"),
            createMessage("Message 3", "user"),
            createMessage("Message 4", "assistant"),
            createMessage("Message 5", "user"));

    session.addItems(itemsToAdd).join();
    List<AgentInputItem> recent = session.getItems(2.0).join();

    assertEquals(2, recent.size());
    assertEquals("Message 4", ((RunMessageOutputItem) recent.get(0)).getContent());
    assertEquals("Message 5", ((RunMessageInputItem) recent.get(1)).getContent());
  }

  @Test
  void getItems_limitExceedsSize_returnsAllItems() {
    MemorySession session = new MemorySession("test-session");
    List<AgentInputItem> itemsToAdd =
        List.of(createMessage("Message 1", "user"), createMessage("Message 2", "assistant"));

    session.addItems(itemsToAdd).join();
    List<AgentInputItem> items = session.getItems(10.0).join();

    assertEquals(2, items.size());
  }

  @Test
  void getItems_zeroLimit_returnsEmptyList() {
    MemorySession session = new MemorySession("test-session");
    List<AgentInputItem> itemsToAdd =
        List.of(createMessage("Message 1", "user"), createMessage("Message 2", "assistant"));

    session.addItems(itemsToAdd).join();
    List<AgentInputItem> items = session.getItems(0.0).join();

    assertTrue(items.isEmpty());
  }

  @Test
  void getItems_negativeLimit_returnsEmptyList() {
    MemorySession session = new MemorySession("test-session");
    List<AgentInputItem> itemsToAdd = List.of(createMessage("Message 1", "user"));

    session.addItems(itemsToAdd).join();
    List<AgentInputItem> items = session.getItems(-1.0).join();

    assertTrue(items.isEmpty());
  }

  @Test
  void popItem_removesLastItem() {
    MemorySession session = new MemorySession("test-session");
    List<AgentInputItem> itemsToAdd =
        List.of(
            createMessage("First", "user"),
            createMessage("Second", "assistant"),
            createMessage("Third", "user"));

    session.addItems(itemsToAdd).join();
    Optional<AgentInputItem> popped = session.popItem().join();

    assertTrue(popped.isPresent());
    assertEquals("Third", ((RunMessageInputItem) popped.get()).getContent());

    List<AgentInputItem> remaining = session.getItems(null).join();
    assertEquals(2, remaining.size());
    assertEquals("Second", ((RunMessageOutputItem) remaining.get(1)).getContent());
  }

  @Test
  void popItem_emptySession_returnsEmpty() {
    MemorySession session = new MemorySession("test-session");

    Optional<AgentInputItem> popped = session.popItem().join();

    assertFalse(popped.isPresent());
  }

  @Test
  void clearSession_removesAllItems() {
    MemorySession session = new MemorySession("test-session");
    List<AgentInputItem> itemsToAdd =
        List.of(
            createMessage("Message 1", "user"),
            createMessage("Message 2", "assistant"),
            createMessage("Message 3", "user"));

    session.addItems(itemsToAdd).join();
    session.clearSession().join();
    List<AgentInputItem> items = session.getItems(null).join();

    assertTrue(items.isEmpty());
  }

  @Test
  void clearSession_emptySession_doesNotThrow() {
    MemorySession session = new MemorySession("test-session");

    assertDoesNotThrow(() -> session.clearSession().join());
  }

  @Test
  void multipleOperations_maintainCorrectState() {
    MemorySession session = new MemorySession("test-session");

    session.addItems(List.of(createMessage("M1", "user"))).join();
    session.addItems(List.of(createMessage("M2", "assistant"))).join();

    assertEquals(2, session.getItems(null).join().size());

    session.popItem().join();

    assertEquals(1, session.getItems(null).join().size());

    session.addItems(List.of(createMessage("M3", "user"))).join();

    assertEquals(2, session.getItems(null).join().size());

    session.clearSession().join();

    assertTrue(session.getItems(null).join().isEmpty());
  }
}
