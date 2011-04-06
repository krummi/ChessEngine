package is.ru.cadia.ce;

import is.ru.cadia.ce.board.Board;
import is.ru.cadia.ce.protocols.ProtocolHandler;
import is.ru.cadia.ce.test.Testing;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

public class Benchmark implements ProtocolHandler {

    // Constants

    private static final String PARAM_FEN = "file";
    private static final String PARAM_TYPE = "type";
    private static final String PARAM_DEPTH = "depth";
    private static final String PARAM_TIME = "time";

    // Variables

    private ArrayList<String> positions = new ArrayList<String>();
    private int seconds;

    // Functions

    public Benchmark(HashMap<String, String> options) {

        assert options.get(PARAM_FEN) != null : "No fen file specified."; // TODO: default file?
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

            fileName = "suites/" + fileName;

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
            ioex.printStackTrace(System.err);
        }

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
        Search search = new Search(null);

        long sum = 0, qsum = 0;
        long before = System.currentTimeMillis();

        for (int i = 0; i < positions.size(); i++) {

            String line = positions.get(i);
            System.out.printf("\nPosition %d/%d: %s\n\n", (i + 1), positions.size(), line);

            Board board = new Board();
            board.initialize(line);

            search.think(board, depth, 0);

            sum += search.nodesSearched;
            qsum += search.qsearched;

            search.transTable.clear();
        }

        printInfo(search, sum, qsum, before, new String[]{});
    }

    public void suiteTesting(int depth) {

        Search search = new Search(this);

        int solved = 0;
        long sum = 0, qsum = 0;
        long before = System.currentTimeMillis();

        for (int i = 0; i < positions.size(); i++) {

            String line = positions.get(i);

            System.out.printf("\nPosition %d/%d: %s\n\n", (i + 1), positions.size(), line);

            String[] tokens = line.split(" ");
            int indexOfBM = -1, indexOfAM = -1;
            for (int a = 0; a < tokens.length; a++) {
                String s = tokens[a];
                if (s.equals("bm")) {
                    indexOfBM = a;
                } else if (s.equals("am")) {
                    indexOfAM = a;
                }
            }
            if (indexOfBM == -1 && indexOfAM == -1) {
                System.err.printf("Line %d does not contain a 'bm' section.\n", (i + 1));
                break;
            }

            ArrayList<String> avoidMoves = new ArrayList<String>();
            ArrayList<String> bestMoves = new ArrayList<String>();

            if (indexOfBM > -1) {

                for (int a = indexOfBM + 1; ; a++) {
                    String move = tokens[a];
                    if (move.endsWith(";")) {
                        bestMoves.add(move.substring(0, move.length() - 1));
                        break;
                    }
                    bestMoves.add(move);
                }

            }

            // TODO: Horrendous code duplication.

            if (indexOfAM > -1) {
                for (int a = indexOfAM + 1; ; a++) {
                    String move = tokens[a];
                    if (move.endsWith(";")) {
                        avoidMoves.add(move.substring(0, move.length() - 1));
                        break;
                    }
                    bestMoves.add(move);
                }
            }

            Board board = new Board();
            board.initialize(line);

            int bestMove = search.think(board, depth, 0);

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
        System.out.printf("%16s: MC=%s, NM=%s, LMR=%s\n", "Settings",
                (Search.DO_MULTI_CUT ? "on" : "off"), (Search.DO_NULL_MOVES ? "on" : "off"), (Search.DO_LMR ? "on" : "off"));
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
        System.out.printf("%16s: %d\n", "TransLessThan", search.transLessDepth);
        System.out.printf("%16s: %d\n", "TransNotFound", search.transNotFound);
        System.out.printf("%16s: %d\n", "TransExact", search.transExact);
        System.out.printf("%16s: %d\n", "TransAlpha", search.transAlpha);
        System.out.printf("%16s: %d", "TransBeta", search.transBeta);

    }

    public void handle(String message) {
        // Nothing here.
    }

    public int getTimeForThisMove(int sideToMove) {
        return seconds * 1000;
    }
}
