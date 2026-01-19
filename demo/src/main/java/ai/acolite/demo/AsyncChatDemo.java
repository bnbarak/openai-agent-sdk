package ai.acolite.demo;

import ai.acolite.agent.Agent;
import com.openai.models.ChatCompletionMessageParam;
import com.openai.models.ChatCompletionUserMessageParam;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AsyncChatDemo {
    public static void main(String[] args) throws Exception {
        Agent agent = DemoAgent.create();
        Terminal terminal = TerminalBuilder.builder().system(true).build();
        LineReader reader = LineReaderBuilder.builder().terminal(terminal).build();
        printWelcome(terminal);

        List<ChatCompletionMessageParam> conversationHistory = new ArrayList<>();
        runChatLoop(agent, terminal, reader, conversationHistory);
        terminal.close();
    }

    private static void runChatLoop(Agent agent, Terminal terminal, LineReader reader,
            List<ChatCompletionMessageParam> conversationHistory) {
        while (true) {
            try {
                String userInput = reader.readLine("\n\u001B[32mYou > \u001B[0m");

                if (isExitCommand(userInput)) {
                    terminal.writer().println("\n\u001B[33mGoodbye! 👋\u001B[0m");
                    break;
                }

                if (userInput.trim().isEmpty()) {
                    continue;
                }

                ChatCompletionUserMessageParam userMessage =
                    ChatCompletionUserMessageParam.builder()
                        .content(ChatCompletionUserMessageParam.Content.ofTextContent(userInput))
                        .build();
                conversationHistory.add(userMessage);

                terminal.writer().print("\n\u001B[36mAssistant > \u001B[0m");
                terminal.writer().flush();

                CompletableFuture<String> responseFuture = agent.runAsync(conversationHistory);
                String response = responseFuture.join();

                terminal.writer().println(response);
                terminal.writer().flush();

            } catch (Exception e) {
                terminal.writer().println("\n\u001B[31mError: " + e.getMessage() + "\u001B[0m");
                terminal.writer().flush();
            }
        }
    }

    private static boolean isExitCommand(String input) {
        if (input == null) {
            return true;
        }
        String trimmed = input.trim().toLowerCase();
        return trimmed.equals("exit") || trimmed.equals("quit");
    }

    private static void printWelcome(Terminal terminal) {
        terminal.writer().println("\u001B[1m╔════════════════════════════════════════════════════════════╗\u001B[0m");
        terminal.writer().println("\u001B[1m║        OpenAI Agent SDK - Async Chat Demo                 ║\u001B[0m");
        terminal.writer().println("\u001B[1m╚════════════════════════════════════════════════════════════╝\u001B[0m");
        terminal.writer().println();
        terminal.writer().println("\u001B[33mThis demo uses async mode - responses appear all at once.\u001B[0m");
        terminal.writer().println("\u001B[90mType 'exit' or 'quit' to end the conversation.\u001B[0m");
        terminal.writer().flush();
    }
}
