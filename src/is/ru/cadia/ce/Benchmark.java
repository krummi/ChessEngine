package is.ru.cadia.ce;

import is.ru.cadia.ce.board.Board;
import is.ru.cadia.ce.protocols.ProtocolHandler;
import is.ru.cadia.ce.test.Testing;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Benchmark implements ProtocolHandler {

    // Constants

    private static final String PARAM_FEN = "file";
    private static final String PARAM_TYPE = "type";
    private static final String PARAM_DEPTH = "depth";
    private static final String PARAM_TIME = "time";

    private static final Pattern SUITE_PATTERN = Pattern.compile("^(.*?) (?:am ([^;]*);)? ?(?:bm ([^;]*);)? .*$");

    // Variables

    private Search search;
    private ArrayList<String> positions = new ArrayList<String>();
    private int seconds;

    // Functions

    public Benchmark(HashMap<String, String> options) {

        assert options.get(PARAM_FEN) != null : "No fen file specified.";
        assert options.get(PARAM_TYPE) != null : "No type specified.";
        assert options.get(PARAM_TYPE).equals("depth")
                || options.get(PARAM_TYPE).equals("perft")
                || options.get(PARAM_TYPE).equals("suite");

        String fileName = options.get(PARAM_FEN);
        String type = options.get(PARAM_TYPE);

        int param = 0;

        if (type.equals("depth") || type.equals("perft")) {

            assert options.get(PARAM_DEPTH) != null;
            param = Integer.parseInt(options.get(PARAM_DEPTH));

        } else if (type.equals("suite")) {

            fileName = "suites/" + fileName; // Suites need to be under the folder "suites"

            assert options.get(PARAM_DEPTH) != null || options.get(PARAM_TIME) != null;

            if (options.get(PARAM_TIME) != null) {
                seconds = Integer.parseInt(options.get(PARAM_TIME));
            } else {
                param = Integer.parseInt(options.get(PARAM_DEPTH));
            }

        }

        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));

            String line;
            while ((line = reader.readLine()) != null) {
                positions.add(line);
            }
        } catch (IOException ioex) {
            ioex.printStackTrace(System.out);
        }

        search = new Search(this);

        if (type.equals("depth")) {
            depthTesting(param);
        } else if (type.equals("perft")) {
            perftTesting(param);
        } else if (type.equals("suite")) {
            suiteTesting(param);
        }
    }

    private void perftTesting(int depth) {

        long sum = 0;
        long before = System.currentTimeMillis();

        for (int i = 0; i < positions.size(); i++) {

            String line = positions.get(i);

            System.out.printf("\nPosition %d/%d: %s\n\n", (i + 1), positions.size(), line);

            Board board = new Board();
            board.initialize(line);

            sum += Testing.getInstance().perft(board, depth, 0);
        }

        long elapsed = System.currentTimeMillis() - before;
        double nodesPerSecond = (double) sum / (elapsed / 1000.0d);

        System.out.printf("\n==========================\n");
        System.out.printf("Nodes searched:\t %d\n", sum);
        System.out.printf("Time taken (ms): %d\n", elapsed);
        System.out.printf("Nodes/second:\t %.1f\n", nodesPerSecond);
    }

    private void depthTesting(int depth) {

        long sum = 0, qsum = 0;
        long before = System.currentTimeMillis();

        for (int i = 0; i < positions.size(); i++) {

            String line = positions.get(i);
            System.out.printf("\nPosition %d/%d: %s\n\n", (i + 1), positions.size(), line);

            Board board = new Board();
            board.initialize(line);

            search.think(board, depth);

            sum += search.nodesSearched;
            qsum += search.qsearched;

            search.transTable.clear();
        }

        printInfo(search, sum, qsum, before, new String[]{});
    }

    public void suiteTesting(int depth) {

        int solved = 0;
        long sum = 0, qsum = 0;
        long before = System.currentTimeMillis();

        for (int i = 0; i < positions.size(); i++) {

            String line = positions.get(i);
            System.out.printf("\nPosition %d/%d: %s\n\n", (i + 1), positions.size(), line);

            // A little bit of regular expression magic.

            Matcher matcher = SUITE_PATTERN.matcher(line);
            if (!matcher.find()) assert false : "Bad suite description: " + line;

            String fenStr = matcher.group(1);
            String amStr = matcher.group(2);
            String bmStr = matcher.group(3);

            ArrayList<String> bestMoves = new ArrayList<String>();
            ArrayList<String> avoidMoves = new ArrayList<String>();

            if (amStr != null) {
                for (String s : amStr.split(" ")) avoidMoves.add(s);
            }

            if (bmStr != null) {
                for (String s : bmStr.split(" ")) bestMoves.add(s);
            }

            Board board = new Board();
            board.initialize(fenStr);

            int bestMove = search.think(board, depth);

            boolean positionSolved = false;

            System.out.println("\nMy best move:\t\t\t" + Move.toSAN(board, bestMove));

            if (bestMoves.size() > 0) {
                System.out.print("Corrent best move(s):\t");
                for (int a = 0; a < bestMoves.size(); a++) {
                    String move = bestMoves.get(a);
                    if (move.equals(Move.toSAN(board, bestMove))) positionSolved = true;
                    if (a == bestMoves.size() - 1) System.out.print(move);
                    else System.out.print(move + ", ");
                }
                System.out.println("");
            }

            if (avoidMoves.size() > 0) {
                System.out.print("Avoid move(s):\t\t\t");
                for (int a = 0; a < avoidMoves.size(); a++) {
                    String move = avoidMoves.get(a);
                    if (move.equals(Move.toSAN(board, bestMove)))
                        positionSolved = false; // Would this ever happen?
                    if (a == avoidMoves.size() - 1) System.out.print(move);
                    else System.out.print(move + ", ");
                }
                System.out.println("");
            }

            if (positionSolved) {
                System.out.println("Correct!");
                solved++;
            }

            sum += search.nodesSearched;
            qsum += search.qsearched;

            search.transTable.clear();
        }

        String[] additionalInfo = {
                String.format("%16s: %d/%d (%.1f%%)", "Solved", solved, positions.size(), ((double) solved / positions.size()) * 100)
        };

        printInfo(search, sum, qsum, before, additionalInfo);
    }

    private void printInfo(Search search, long sum, long qsum, long before, String[] additionalInfo) {

        long elapsed = System.currentTimeMillis() - before;
        double nodesPerSecond = (double) sum / (elapsed / 1000.0d);

        System.out.printf("\n=================\n");
        System.out.printf("%16s: %s\n", "Settings", search.getConfiguration());
        for (String s : additionalInfo) {
            System.out.println(s);
        }
        System.out.printf("%16s: %d (q: %d - %.1f%%)\n", "Nodes searched", sum, qsum, ((double) qsum / sum) * 100);
        System.out.printf("%16s: %d\n", "Time taken (ms)", elapsed);
        System.out.printf("%16s: %.1f\n", "Nodes/second", nodesPerSecond);
        System.out.printf("%16s: %d\n", "Mcprunes", search.mcprunes);
        System.out.printf("%16s: %d\n", "Before MC", search.before);
        System.out.printf("%16s: %d\n", "Doing MC", search.after);
        System.out.printf("%16s: %d\n", "TransFound", search.transFound);
        System.out.printf("%16s: %d\n", "TransNotFound", search.transNotFound);

        System.out.printf("%16s: %d\n", "AR overwrites", search.transTable.alwaysReplaceOW);
        System.out.printf("%16s: %d\n", "Depth overwrites", search.transTable.depthOW);
        System.out.printf("%16s: %d\n", "Overwrites", search.transTable.depthOW + search.transTable.alwaysReplaceOW);

    }

    public void handle(String message) {
        // Nothing here.
    }

    public int getTimeForThisMove(int sideToMove) {
        return seconds * 1000;
    }

    public void sendMessage(String message) {
        System.out.println(message);
    }

}
