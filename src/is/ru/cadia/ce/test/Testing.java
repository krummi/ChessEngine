package is.ru.cadia.ce.test;

import is.ru.cadia.ce.Move;
import is.ru.cadia.ce.MoveSelector;
import is.ru.cadia.ce.MoveStack;
import is.ru.cadia.ce.board.Board;
import is.ru.cadia.ce.board.MoveGenerator;
import is.ru.cadia.ce.board.SEE;
import is.ru.cadia.ce.other.Constants;

import java.util.Vector;

public class Testing implements Constants {

    // Types

    class RootMove {
        public long childrens;
        public int move;
    }

    class PerformanceTest {
        final String position;
        final long[] answers;

        PerformanceTest(String position, long[] answers) {
            this.position = position;
            this.answers = answers;
        }
    }

    // Constants

    private static final Testing ourInstance = new Testing();

    // Variables

    private MoveSelector[] selectors;
    private MoveGenerator generator;
    private MoveStack moveStack;

    private final Vector<PerformanceTest> tests = new Vector<PerformanceTest>();

    // Singleton functions

    private Testing() {

        // TODO: Beginning of AbstractSearch

        // Initializes the instance
        generator = MoveGenerator.getInstance();
        moveStack = new MoveStack(4800);

        // Initializes the PlyInfo instances that will be used.
        selectors = new MoveSelector[MAX_PLY];

        for (int a = 0; a < MAX_PLY; a++) {
            selectors[a] = new MoveSelector();
        }

        // TODO: End of AbstractSearch
    }

    public static Testing getInstance() {
        return ourInstance;
    }

    // Functions

    private void addPerformanceTests() {

        long[] answers;

        answers = new long[]{20, 400, 8902, 197281, 4865609, 119060324};
        tests.add(new PerformanceTest("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq -", answers));

        answers = new long[]{48, 2039, 97862, 4085603, 193690690};
        tests.add(new PerformanceTest("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -", answers));

        answers = new long[]{14, 191, 2812, 43238, 674624, 11030083, 178633661, -1};
        tests.add(new PerformanceTest("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - -", answers));

        answers = new long[]{50, 279, -1};
        tests.add(new PerformanceTest("8/3K4/2p5/p2b2r1/5k2/8/8/1q6 b - -", answers));

        answers = new long[]{5, 117, 3293, 67197, 1881089, 38633283, -1};
        tests.add(new PerformanceTest("8/7p/p5pb/4k3/P1pPn3/8/P5PP/1rB2RK1 b - d3", answers));

        answers = new long[]{50, 279, -1};
        tests.add(new PerformanceTest("8/3K4/2p5/p2b2r1/5k2/8/8/1q6 b - -", answers));

        answers = new long[]{-1, -1, -1, -1, 11139762, -1};
        tests.add(new PerformanceTest("rnbqkb1r/ppppp1pp/7n/4Pp2/8/8/PPPP1PPP/RNBQKBNR w KQkq f6", answers));

        answers = new long[]{-1, -1, -1, -1, -1, 11030083, 178633661, -1};
        tests.add(new PerformanceTest("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - -", answers));

        answers = new long[]{24, 496, 9483, 182838, 3605103, 71179139, -1};
        tests.add(new PerformanceTest("n1n5/PPPk4/8/8/8/8/4Kppp/5N1N b - -", answers));

    }

    public void perftAll() {

        // Adds the tests:
        addPerformanceTests();

        long time = System.currentTimeMillis();
        int ok = 0, failure = 0;

        // Tests them!
        for (PerformanceTest test : tests) {
            System.out.printf("\n[Testing] pos: %s\n", test.position);
            for (int a = 1; a < test.answers.length; a++) {

                if (test.answers[a - 1] == -1) { continue; }

                moveStack.setSize(0);
                Board board = new Board();
                board.initialize(test.position);
                //System.out.println("Zobrist before: " + board.key);
                long before = System.currentTimeMillis();
                long nodes = perft(board, a, 0);
                long deltaTime = System.currentTimeMillis() - before;
                //System.out.println("Zobrist after : " + board.key);

                if (nodes == test.answers[a - 1]) {
                    System.out.printf("Perft(%d): %d nodes in time: %d ms [ans: %d] OK\n",
                            a, nodes, deltaTime, test.answers[a - 1]);
                    ok++;
                } else {
                    System.out.printf("Perft(%d): %d nodes in time: %d ms [ans: %d] FAILURE\n",
                            a, nodes, deltaTime, test.answers[a - 1]);
                    failure++;
                }
            }
        }

        long deltaTime = System.currentTimeMillis() - time;
        System.out.printf(
                "\n[Finished testing] ok: %d, failed: %d in %d ms!\n", ok, failure, deltaTime);
    }

    public long perft(Board board, int depth, int ply) {

        long nodes = 0;
        MoveSelector selector = selectors[ply];
        selector.initialize(board, Move.MOVE_NONE, board.isCheck(), false);

        if (depth == 1) {
            while (selector.getNextMove() != Move.MOVE_NONE) { nodes++; }
            return nodes;
        }

        int move;
        while ((move = selector.getNextMove()) != Move.MOVE_NONE) {
            board.make(move);
            nodes += perft(board, depth - 1, ply + 1);
            board.retract(move);
        }

        return nodes;
    }

    public long perft2(Board board, int depth) {

        if (depth == 0) {
            return 1;
        }

        long nodes = 0;
        boolean isCheck = board.isCheck();
        int noOfMoves = generator.generatePseudoMoves(board, moveStack, isCheck);

        for (int i = 0; i < noOfMoves; i++) {
            int move = moveStack.pop().move;

            if (board.isValidMove(move, isCheck)) {
                board.make(move);
                nodes += perft2(board, depth - 1);
                board.retract(move);
            }
        }

        return nodes;
    }

    public void divide(Board board, int depth) {

        int noOfMoves = generator.generatePseudoMoves(board, moveStack, board.isCheck());
        Vector<RootMove> rootMoves = new Vector<RootMove>(noOfMoves);
        for (int i = 0; i < noOfMoves; i++) {
            int move = moveStack.pop().move;
            RootMove rootMove = new RootMove();
            rootMove.move = move;
            board.make(move);
            rootMove.childrens = miniMax(board, depth - 1);
            board.retract(move);
            rootMoves.add(rootMove);
        }

        long a = 0;
        for (int i = 0; i < rootMoves.size(); i++) {
            a += rootMoves.get(i).childrens;
            System.out.printf("%s %d\n", Move.toLAN(rootMoves.get(i).move),
                    rootMoves.get(i).childrens);
        }
        System.out.println("# of childrens: " + a);
    }

    private long miniMax(Board board, int depth) {

        if (depth == 0)
            return 1;

        long nodes = 0;
        boolean isCheck = board.isCheck();
        int noOfMoves = generator.generatePseudoMoves(board, moveStack, isCheck);

        for (int i = 0; i < noOfMoves; i++) {
            int move = moveStack.pop().move;
            if (board.isValidMove(move, isCheck)) {
                board.make(move);
                nodes += miniMax(board, depth - 1);
                board.retract(move);
            }
        }

        return nodes;
    }

    public void testSomething(Board board) {

        SEE see = SEE.getInstance();

        boolean isCheck = board.isCheck();
        int noOfMoves = generator.generatePseudoMoves(board, moveStack, isCheck);

        for (int i = 0; i < noOfMoves; i++) {
            int move = moveStack.pop().move;
            if (Move.isNormalCapture(Move.getType(move))) {
                System.out.printf("SEE for %s is %d!\n", Move.toLAN(move), see.see(move, board));
            }
        }
    }

    public static void main(String[] args) {

        /*
        int rows = 12;
        int cols = 15;

        for( int a = 0; a < rows; a++ ) {
            for( int b = 0; b < cols; b++ ) {
                System.out.printf("%3d ", (a * cols) + b );
            }
            System.out.println("");
        }
        */
    }

}