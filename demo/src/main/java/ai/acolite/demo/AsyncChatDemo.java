package ai.acolite.demo;

import ai.acolite.agentsdk.core.Agent;
import ai.acolite.agentsdk.core.RunConfig;
import ai.acolite.agentsdk.core.RunResult;
import ai.acolite.agentsdk.core.Runner;
import ai.acolite.agentsdk.core.memory.SQLiteSession;
import ai.acolite.agentsdk.core.memory.Session;
import ai.acolite.agentsdk.core.types.TextOutput;
import ai.acolite.agentsdk.core.types.UnknownContext;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.nio.file.Path;

public class AsyncChatDemo {
    public static void main(String[] args) throws Exception {
        Agent<UnknownContext, TextOutput> agent = DemoAgent.create();
        Terminal terminal = TerminalBuilder.builder().system(true).build();
        LineReader reader = LineReaderBuilder.builder().terminal(terminal).build();
        printWelcome(terminal);

        Session session = SQLiteSession.fromFile("async-chat-demo", Path.of("demo-conversations.db"));
        RunConfig config = RunConfig.builder()
                .session(session)
                .maxTurns(20)
                .build();

        runChatLoop(agent, terminal, reader, config);
        terminal.close();
    }

    private static void runChatLoop(Agent<UnknownContext, TextOutput> agent, Terminal terminal,
            LineReader reader, RunConfig config) {
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

                RunResult<UnknownContext, ?> result = Runner.run(agent, userInput, config);
                String response = result.getFinalOutput().toString();

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
