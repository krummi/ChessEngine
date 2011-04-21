package is.ru.cadia.ce;

import is.ru.cadia.ce.board.Board;
import is.ru.cadia.ce.board.Evaluation;
import is.ru.cadia.ce.other.Constants;
import is.ru.cadia.ce.other.Options;
import is.ru.cadia.ce.protocols.ProtocolHandler;
import is.ru.cadia.ce.protocols.UCIHandler;
import is.ru.cadia.ce.test.FENs;
import is.ru.cadia.ce.test.Testing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

public class Application implements Constants, FENs {

    // Constants

    public final static String NAME = "Ziggy";
    public final static String VERSION = "v0.6";
    public final static String AUTHOR = "Hrafn Eiriksson <hrafne08@ru.is>";

    private final BufferedReader reader;

    // Variables
    private Board board;
    private ProtocolHandler handler;

    // Functions

    private Application() {

        reader = new BufferedReader(new InputStreamReader(System.in));

        // Some necessary initialization:
        Evaluation.initialize();

    }

    private void start() throws IOException {

        // Board initialization
        board = new Board();
        board.initialize();

        System.out.printf("Hey there! Welcome to %s %s by %s.\n", NAME, VERSION, AUTHOR);
        System.out.printf("%s> ", NAME.toLowerCase());

        String line;
        while ((line = reader.readLine()) != null) {
            if (handler == null) {
                if (line.equals("uci")) {
                    // Switches to UCI mode!
                    handler = new UCIHandler();
                    handler.handle(line);
                } else if (line.equals("xboard")) {
                    // Not supported - yet.
                } else {
                    handleConsoleMessage(line);
                    System.out.printf("%s> ", NAME.toLowerCase());
                }
            } else {
                handler.handle(line);
            }
        }
    }

    private void handleConsoleMessage(String line) {

        if (line.equals("help") || line.equals("ls")) {

            System.out.println("");
            System.out.println("Command           | Description");
            System.out.println("------------------+------------------------------------------------------------");
            System.out.println("display           | Displays the current status of the board.");
            System.out.println("divide    <depth> | Breaks down the generated moves at depth <depth>.");
            System.out.println("evaluate          | Evaluates the current position.");
            System.out.println("pos       <fen>   | Sets the position of the board according to some fen string.");
            System.out.println("perft     <depth> | Performance test against the current board.");
            System.out.println("perftall          | Performance test against the test-suites.");
            System.out.println("test              | For debugging purposes.");
            System.out.println("uci               | Switch to UCI mode.");
            System.out.println("exit/quit         | Exit this program.");
            System.out.println("");

        } else if (line.equals("perftall")) {

            Testing.getInstance().perftAll();

        } else if (line.startsWith("perft")) {

            int depth = 1;
            try {
                depth = Integer.parseInt(line.substring("perft".length() + 1));
            } catch (Exception e) { /* doesn't matter */ }

            //System.out.println("Key before: " + board.key);
            long before = System.currentTimeMillis();
            long nodes = Testing.getInstance().perft(board, depth, 0);
            long delta = System.currentTimeMillis() - before;
            //System.out.println("Key after: " + board.key);

            System.out.printf("Found %d nodes in %d ms!\n", nodes, delta);

        } else if (line.startsWith("test")) {

            Testing.getInstance().testSomething(board);

        } else if (line.equals("display")) {

            board.print();

        } else if (line.startsWith("divide")) {

            int depth;
            try {
                depth = Integer.parseInt(line.substring("divide".length() + 1));
                Testing.getInstance().divide(board, depth);
            } catch (Exception e) {
                Testing.getInstance().divide(board, 1);
            }

        } else if (line.equals("evaluate")) {

            Evaluation.evaluate(board, true);

        } else if (line.startsWith("pos")) {

            // TODO: board.reset(); ?
            board = new Board();
            board.initialize(line.substring("pos".length() + 1));

        } else if (line.startsWith("pv")) {

            Search search = new Search(new UCIHandler());

            int depth;
            try {
                depth = Integer.parseInt(line.substring("pv".length() + 1));
                System.out.println("make this move: " + Move.toLAN(search.think(board, depth)));
            } catch (Exception e) {
                System.out.println("make this move: " + Move.toLAN(search.think(board, 3)));
            }

        } else if (line.equals("exit") || line.equals("quit")) {

            System.exit(0);

        } else {
            System.out.println("Unknown command, type 'help' to see a list of available commands.");
        }
    }

    private HashMap<String, String> parseParams(String[] args) {

        HashMap<String, String> params = new HashMap<String, String>();

        for (String s : args) {
            String[] tokens = s.split("=");

            assert tokens.length == 1 || tokens.length == 2;

            if (tokens.length == 1) {
                params.put(tokens[0].toLowerCase(), "true");
            } else if (tokens.length == 2) {
                if (tokens[0].equals("file")) {
                    // Do NOT put the filename in lowercase!
                    params.put(tokens[0].toLowerCase(), tokens[1]);
                } else {
                    params.put(tokens[0].toLowerCase(), tokens[1].toLowerCase());
                }
            }
        }

        return params;
    }

    public static void main(String[] args) {

        Application app = new Application();

        // Parses the parameters for this application
        HashMap<String, String> params = app.parseParams(args);

        // Configures the options according to the parameters
        Options.getInstance().parseOptions(params);

        if (params.containsKey("bench")) {

            Benchmark bench = new Benchmark(params);

        } else {

            try {
                app.start();
            } catch (Exception ex) {
                System.out.println("Error: An application exception occurred!");
                ex.printStackTrace(System.out);
            }

        }
    }

}