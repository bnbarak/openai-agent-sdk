package ai.acolite.demo;

import ai.acolite.agent.Agent;
import ai.acolite.types.AgentStreamEvent;
import com.openai.models.ChatCompletionMessageParam;
import com.openai.models.ChatCompletionUserMessageParam;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class StreamingChatDemo {
    public static void main(String[] args) throws Exception {
        Agent agent = DemoAgent.create();
        Terminal terminal = TerminalBuilder.builder().system(true).build();
        LineReader reader = LineReaderBuilder.builder().terminal(terminal).build();
        printWelcome(terminal);

        List<ChatCompletionMessageParam> conversationHistory = new ArrayList<>();
        runStreamingChatLoop(agent, terminal, reader, conversationHistory);
        terminal.close();
    }

    private static void runStreamingChatLoop(Agent agent, Terminal terminal, LineReader reader,
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

                Stream<AgentStreamEvent> eventStream = agent.runStreaming(conversationHistory);
                processStreamEvents(eventStream, terminal);

            } catch (Exception e) {
                terminal.writer().println("\n\u001B[31mError: " + e.getMessage() + "\u001B[0m");
                terminal.writer().flush();
            }
        }
    }

    private static void processStreamEvents(Stream<AgentStreamEvent> eventStream, Terminal terminal) {
        eventStream.forEach(event -> {
            switch (event.type()) {
                case TEXT_DELTA:
                    String delta = event.textDelta();
                    if (delta != null && !delta.isEmpty()) {
                        terminal.writer().print(delta);
                        terminal.writer().flush();
                    }
                    break;
                case TOOL_CALL_START:
                    terminal.writer().print("\n\u001B[90m[Calling tool: " + event.toolCall().name() + "]\u001B[0m\n");
                    terminal.writer().flush();
                    break;
                case TOOL_CALL_RESULT:
                    terminal.writer().print("\u001B[90m[Tool completed]\u001B[0m\n");
                    terminal.writer().flush();
                    break;
                case AGENT_END:
                    terminal.writer().println();
                    terminal.writer().flush();
                    break;
                case ERROR:
                    terminal.writer().println("\n\u001B[31mError: " + event.error() + "\u001B[0m");
                    terminal.writer().flush();
                    break;
                default:
                    break;
            }
        });
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
        terminal.writer().println("\u001B[1m║       OpenAI Agent SDK - Streaming Chat Demo              ║\u001B[0m");
        terminal.writer().println("\u001B[1m╚════════════════════════════════════════════════════════════╝\u001B[0m");
        terminal.writer().println();
        terminal.writer().println("\u001B[33mThis demo uses streaming mode - see responses in real-time!\u001B[0m");
        terminal.writer().println("\u001B[90mType 'exit' or 'quit' to end the conversation.\u001B[0m");
        terminal.writer().flush();
    }
}
