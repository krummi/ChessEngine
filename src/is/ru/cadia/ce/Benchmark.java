package is.ru.cadia.ce;

import is.ru.cadia.ce.board.Board;
import is.ru.cadia.ce.protocols.ProtocolHandler;
import is.ru.cadia.ce.test.Testing;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Benchmark implements ProtocolHandler {

    // Constants

    private static final String PARAM_FEN = "fenfile";
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

        long sumOfCounter = 0;
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

        long elapsed = System.currentTimeMillis() - before;
        double nodesPerSecond = (double) sum / (elapsed / 1000.0d);

        System.out.printf("\n==========================\n");
        System.out.printf("Stuff coutner :\t %d\n", sumOfCounter);
        System.out.printf("Nodes searched:\t %d (q: %d - %.1f%%)\n", sum, qsum, ((double) qsum / sum) * 100);
        System.out.printf("Time taken (ms): %d\n", elapsed);
        System.out.printf("Nodes/second:\t %.1f\n", nodesPerSecond);

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
            int indexOfBM = -1;
            for (int a = 0; a < tokens.length; a++) {
                String s = tokens[a];
                if (s.equals("bm")) {
                    indexOfBM = a;
                    break;
                }
            }
            if (indexOfBM == -1) {
                System.err.printf("Line %d does not contain a 'bm' section.\n", (i + 1));
                break;
            }

            ArrayList<String> bestMoves = new ArrayList<String>();

            for (int a = indexOfBM + 1; ; a++) {
                String move = tokens[a];
                if (move.endsWith(";")) {
                    bestMoves.add(move.substring(0, move.length() - 1));
                    break;
                }
                bestMoves.add(move);
            }

            Board board = new Board();
            board.initialize(line);

            int bestMove = search.think(board, depth, 0);

            boolean positionSolved = false;

            System.out.println("\nMy best move:\t\t\t" + Move.toSAN(board, bestMove));
            System.out.print("Corrent best move(s):\t");
            for (int a = 0; a < bestMoves.size(); a++) {
                String move = bestMoves.get(a);
                if (move.equals(Move.toSAN(board, bestMove))) positionSolved = true;
                if (a == bestMoves.size() - 1) System.out.print(move);
                else System.out.print(move + ", ");
            }

            System.out.println("");

            if (positionSolved) {
                System.out.println("Correct!");
                solved++;
            }

            sum += search.nodesSearched;
            qsum += search.qsearched;

            search.transTable.clear();
        }

        long elapsed = System.currentTimeMillis() - before;
        double nodesPerSecond = (double) sum / (elapsed / 1000.0d);

        System.out.printf("\n==========================\n");
        System.out.printf("Solved:\t\t\t %d/%d (%.1f%%)\n", solved, positions.size(), ((double) solved / positions.size()) * 100);
        System.out.printf("Nodes searched:\t %d (q: %d - %.1f%%)\n", sum, qsum, ((double) qsum / sum) * 100);
        System.out.printf("Time taken (ms): %d\n", elapsed);
        System.out.printf("Nodes/second:\t %.1f\n", nodesPerSecond);
    }

    public void handle(String message) {
        // Nothing here.
    }

    public int getTimeForThisMove(int sideToMove) {
        return seconds * 1000;
    }
}
