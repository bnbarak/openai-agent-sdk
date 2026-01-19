package ai.acolite.demo;

import ai.acolite.agentsdk.core.Agent;
import ai.acolite.agentsdk.core.RunConfig;
import ai.acolite.agentsdk.core.Runner;
import ai.acolite.agentsdk.core.StreamedRunResult;
import ai.acolite.agentsdk.core.memory.MemorySession;
import ai.acolite.agentsdk.core.memory.Session;
import ai.acolite.agentsdk.core.shims.ReadableStream;
import ai.acolite.agentsdk.core.shims.ReadableStreamAsyncIterator;
import ai.acolite.agentsdk.core.types.TextOutput;
import ai.acolite.agentsdk.core.types.UnknownContext;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

public class StreamingChatDemo {
    public static void main(String[] args) throws Exception {
        Agent<UnknownContext, TextOutput> agent = DemoAgent.create();
        Terminal terminal = TerminalBuilder.builder().system(true).build();
        LineReader reader = LineReaderBuilder.builder().terminal(terminal).build();
        printWelcome(terminal);

        Session session = new MemorySession("streaming-chat-demo");
        RunConfig config = RunConfig.builder().session(session).build();

        runStreamingChatLoop(agent, terminal, reader, config);
        terminal.close();
    }

    private static void runStreamingChatLoop(Agent<UnknownContext, TextOutput> agent,
            Terminal terminal, LineReader reader, RunConfig config) {
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

                terminal.writer().print("\n\u001B[36mAssistant > \u001B[0m");
                terminal.writer().flush();

                StreamedRunResult<UnknownContext, Agent<UnknownContext, TextOutput>> result =
                    Runner.runStreamed(agent, userInput, config);

                ReadableStream<String> textStream = result.toTextStream();
                ReadableStreamAsyncIterator<String> iterator = textStream.values();

                while (iterator.hasNext()) {
                    String text = iterator.next();
                    terminal.writer().print(text);
                    terminal.writer().flush();
                }

                terminal.writer().println();
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
        terminal.writer().println("\u001B[1m║       OpenAI Agent SDK - Streaming Chat Demo              ║\u001B[0m");
        terminal.writer().println("\u001B[1m╚════════════════════════════════════════════════════════════╝\u001B[0m");
        terminal.writer().println();
        terminal.writer().println("\u001B[33mThis demo uses streaming mode - see responses in real-time!\u001B[0m");
        terminal.writer().println("\u001B[90mType 'exit' or 'quit' to end the conversation.\u001B[0m");
        terminal.writer().flush();
    }
}
