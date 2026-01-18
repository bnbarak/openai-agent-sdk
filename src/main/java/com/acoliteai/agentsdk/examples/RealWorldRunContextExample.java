package com.acoliteai.agentsdk.examples;

import com.acoliteai.agentsdk.core.RunContext;
import com.acoliteai.agentsdk.core.RunToolApprovalItem;
import com.acoliteai.agentsdk.core.Usage;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * RealWorldRunContextExample demonstrates how RunContext would be used in a production e-commerce
 * application with: - Custom context containing user auth, database connections, and services -
 * Tools that access the context to perform operations - Safety mechanisms using tool approval for
 * sensitive operations - Usage tracking for cost monitoring
 *
 * <p>This example simulates a customer service agent that can: - Look up orders (safe -
 * auto-approved) - Process refunds (sensitive - requires approval) - Cancel orders (sensitive -
 * requires approval)
 */
public class RealWorldRunContextExample {

  public static void main(String[] args) {
    System.out.println("=== Real-World E-Commerce Agent Example ===\n");

    ECommerceContext appContext = createECommerceContext();
    RunContext<ECommerceContext> runContext = new RunContext<>(appContext);

    configureToolApprovals(runContext);

    simulateAgentExecution(runContext);

    printFinalReport(runContext);
  }

  /** Creates the application context with all necessary services and user data. */
  static ECommerceContext createECommerceContext() {
    System.out.println("--- Setting up E-Commerce Context ---");

    ECommerceContext context = new ECommerceContext();
    context.userId = "user_alice_123";
    context.userEmail = "alice@example.com";
    context.authToken = "auth_token_xyz";
    context.orderService = new MockOrderService();
    context.paymentService = new MockPaymentService();
    context.emailService = new MockEmailService();

    System.out.println("Context initialized for user: " + context.userId);
    System.out.println();

    return context;
  }

  /** Configures which tools are auto-approved and which require manual approval. */
  static void configureToolApprovals(RunContext<ECommerceContext> context) {
    System.out.println("--- Configuring Tool Approvals ---");

    RunToolApprovalItem lookupOrderApproval =
        RunToolApprovalItem.builder().toolName("lookup_order").toolCallId("initial").build();
    context.approveTool(lookupOrderApproval, true);
    System.out.println("✓ lookup_order: Auto-approved (read-only, safe)");

    System.out.println("✗ process_refund: Requires manual approval (modifies payment)");
    System.out.println("✗ cancel_order: Requires manual approval (modifies order)");
    System.out.println();
  }

  /** Simulates an agent execution with multiple tool calls and approval decisions. */
  static void simulateAgentExecution(RunContext<ECommerceContext> context) {
    System.out.println("=== Agent Execution Simulation ===\n");

    scenario1_LookupOrder(context);
    scenario2_ProcessRefund(context);
    scenario3_CancelOrder(context);
  }

  /** Scenario 1: Agent looks up an order (auto-approved). */
  static void scenario1_LookupOrder(RunContext<ECommerceContext> context) {
    System.out.println("--- Scenario 1: Customer asks 'What's the status of order #5678?' ---");

    LookupOrderTool tool = new LookupOrderTool();
    LookupOrderInput input = new LookupOrderInput();
    input.orderId = "order_5678";

    Boolean approved = context.isToolApproved("lookup_order", "call_001");
    System.out.println(
        "Tool approval check: " + (approved != null && approved ? "APPROVED" : "PENDING"));

    if (Boolean.TRUE.equals(approved)) {
      Order order = tool.invoke(context, input).join();
      System.out.println("Order found: " + order);

      context.addUsage(
          Usage.builder().inputTokens(50.0).outputTokens(100.0).totalTokens(150.0).build());
      System.out.println("Usage tracked: 150 tokens");
    }

    System.out.println();
  }

  /** Scenario 2: Agent attempts to process a refund (requires approval). */
  static void scenario2_ProcessRefund(RunContext<ECommerceContext> context) {
    System.out.println("--- Scenario 2: Customer asks 'Can I get a refund for order #5678?' ---");

    ProcessRefundTool tool = new ProcessRefundTool();
    ProcessRefundInput input = new ProcessRefundInput();
    input.orderId = "order_5678";
    input.reason = "Product damaged on arrival";

    Boolean approved = context.isToolApproved("process_refund", "call_002");
    System.out.println(
        "Tool approval check: "
            + (approved != null && approved
                ? "APPROVED"
                : approved != null ? "REJECTED" : "PENDING"));

    if (approved == null) {
      System.out.println("\n⚠️  Execution paused - waiting for human approval");
      System.out.println("Agent wants to process refund for order_5678");
      System.out.println("Reason: " + input.reason);
      System.out.println("Amount: $129.99");

      System.out.println("\n[Human operator reviews and approves]");

      RunToolApprovalItem approvalItem =
          RunToolApprovalItem.builder().toolName("process_refund").toolCallId("call_002").build();
      context.approveTool(approvalItem);

      approved = context.isToolApproved("process_refund", "call_002");
      System.out.println("Tool approval: " + (approved ? "APPROVED by operator" : "REJECTED"));
    }

    if (Boolean.TRUE.equals(approved)) {
      String result = tool.invoke(context, input).join();
      System.out.println("Refund result: " + result);

      context.addUsage(
          Usage.builder().inputTokens(75.0).outputTokens(50.0).totalTokens(125.0).build());
      System.out.println("Usage tracked: 125 tokens");
    }

    System.out.println();
  }

  /** Scenario 3: Agent attempts to cancel an order (rejected due to policy). */
  static void scenario3_CancelOrder(RunContext<ECommerceContext> context) {
    System.out.println("--- Scenario 3: Customer asks 'Cancel my order #1234' ---");

    CancelOrderTool tool = new CancelOrderTool();
    CancelOrderInput input = new CancelOrderInput();
    input.orderId = "order_1234";

    Boolean approved = context.isToolApproved("cancel_order", "call_003");
    System.out.println(
        "Tool approval check: "
            + (approved != null && approved
                ? "APPROVED"
                : approved != null ? "REJECTED" : "PENDING"));

    if (approved == null) {
      System.out.println("\n⚠️  Execution paused - waiting for human approval");
      System.out.println("Agent wants to cancel order_1234");

      System.out.println("\n[Human operator reviews and rejects - order already shipped]");

      RunToolApprovalItem approvalItem =
          RunToolApprovalItem.builder().toolName("cancel_order").toolCallId("call_003").build();
      context.rejectTool(approvalItem);

      approved = context.isToolApproved("cancel_order", "call_003");
      System.out.println("Tool approval: " + (approved ? "APPROVED" : "REJECTED by operator"));
    }

    if (Boolean.FALSE.equals(approved)) {
      System.out.println("⛔ Tool call blocked - order cannot be cancelled (already shipped)");
      System.out.println("Agent will inform customer and offer alternatives");

      context.addUsage(
          Usage.builder().inputTokens(50.0).outputTokens(80.0).totalTokens(130.0).build());
      System.out.println("Usage tracked: 130 tokens");
    }

    System.out.println();
  }

  /** Prints final report showing accumulated usage and approval state. */
  static void printFinalReport(RunContext<ECommerceContext> context) {
    System.out.println("=== Final Report ===");
    System.out.println("\nTotal Usage:");
    System.out.println("  Input tokens: " + context.getUsage().getInputTokens());
    System.out.println("  Output tokens: " + context.getUsage().getOutputTokens());
    System.out.println("  Total tokens: " + context.getUsage().getTotalTokens());

    double estimatedCost = context.getUsage().getTotalTokens() * 0.00002;
    System.out.println("  Estimated cost: $" + String.format("%.4f", estimatedCost));

    System.out.println("\nTool Approval Summary:");
    System.out.println("  lookup_order: Permanently approved (3 calls made)");
    System.out.println("  process_refund: 1 call approved, 0 calls rejected");
    System.out.println("  cancel_order: 0 calls approved, 1 call rejected");
  }

  // ==================== Mock E-Commerce Services ====================

  static class ECommerceContext {
    String userId;
    String userEmail;
    String authToken;
    MockOrderService orderService;
    MockPaymentService paymentService;
    MockEmailService emailService;
  }

  static class MockOrderService {
    Order getOrder(String orderId) {
      return new Order(
          orderId, "user_alice_123", "Shipped", new BigDecimal("129.99"), LocalDateTime.now());
    }
  }

  static class MockPaymentService {
    String processRefund(String orderId, BigDecimal amount) {
      return "Refund processed successfully for order " + orderId + " - $" + amount;
    }
  }

  static class MockEmailService {
    void sendEmail(String to, String subject, String body) {
      System.out.println("Email sent to " + to + ": " + subject);
    }
  }

  static class Order {
    String orderId;
    String userId;
    String status;
    BigDecimal totalAmount;
    LocalDateTime orderDate;

    Order(
        String orderId,
        String userId,
        String status,
        BigDecimal totalAmount,
        LocalDateTime orderDate) {
      this.orderId = orderId;
      this.userId = userId;
      this.status = status;
      this.totalAmount = totalAmount;
      this.orderDate = orderDate;
    }

    @Override
    public String toString() {
      return "Order{id='" + orderId + "', status='" + status + "', amount=$" + totalAmount + "}";
    }
  }

  // ==================== Tool Implementations ====================

  static class LookupOrderTool {
    CompletableFuture<Order> invoke(RunContext<ECommerceContext> context, LookupOrderInput input) {
      ECommerceContext appContext = context.getContext();

      if (!appContext.authToken.startsWith("auth_token")) {
        return CompletableFuture.failedFuture(new SecurityException("Invalid auth token"));
      }

      Order order = appContext.orderService.getOrder(input.orderId);

      if (!order.userId.equals(appContext.userId)) {
        return CompletableFuture.failedFuture(
            new SecurityException("Cannot access another user's order"));
      }

      return CompletableFuture.completedFuture(order);
    }
  }

  static class ProcessRefundTool {
    CompletableFuture<String> invoke(
        RunContext<ECommerceContext> context, ProcessRefundInput input) {
      ECommerceContext appContext = context.getContext();

      Order order = appContext.orderService.getOrder(input.orderId);

      if (!order.userId.equals(appContext.userId)) {
        return CompletableFuture.failedFuture(
            new SecurityException("Cannot refund another user's order"));
      }

      String result = appContext.paymentService.processRefund(input.orderId, order.totalAmount);

      appContext.emailService.sendEmail(
          appContext.userEmail,
          "Refund Processed",
          "Your refund of $" + order.totalAmount + " has been processed.");

      return CompletableFuture.completedFuture(result);
    }
  }

  static class CancelOrderTool {
    CompletableFuture<String> invoke(RunContext<ECommerceContext> context, CancelOrderInput input) {
      ECommerceContext appContext = context.getContext();

      Order order = appContext.orderService.getOrder(input.orderId);

      if (!order.userId.equals(appContext.userId)) {
        return CompletableFuture.failedFuture(
            new SecurityException("Cannot cancel another user's order"));
      }

      if ("Shipped".equals(order.status)) {
        return CompletableFuture.failedFuture(
            new IllegalStateException("Cannot cancel shipped order"));
      }

      return CompletableFuture.completedFuture(
          "Order " + input.orderId + " cancelled successfully");
    }
  }

  static class LookupOrderInput {
    String orderId;
  }

  static class ProcessRefundInput {
    String orderId;
    String reason;
  }

  static class CancelOrderInput {
    String orderId;
  }
}
