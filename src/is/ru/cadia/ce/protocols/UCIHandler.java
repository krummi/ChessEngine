package is.ru.cadia.ce.protocols;

import is.ru.cadia.ce.Application;
import is.ru.cadia.ce.Move;
import is.ru.cadia.ce.Search;
import is.ru.cadia.ce.board.Board;
import is.ru.cadia.ce.other.Book;
import is.ru.cadia.ce.other.Constants;
import is.ru.cadia.ce.other.Options;

import java.io.IOException;

public class UCIHandler implements ProtocolHandler, Constants {

    // Constants

    // Time controls
    private final static int TC_INFINITE = 1;
    private final static int TC_FIXEDDEPTH = 2;
    private final static int TC_MOVETIME = 4;
    private final static int TC_NODES = 8;
    private final static int TC_TIMELEFT = 16;
    private final static int TC_INC = 32;
    private final static int TC_MOVESTOGO = 64;
    private final static int TC_MATE = 128;

    // Moves to go (used for allocation of time)
    private final static int DEFAULT_MOVES_TO_GO = 28;
    private final static double ALLOCATION_PERCENTAGE = 0.0333;

    // Search strategy
    private final Search search;

    // Variables

    private Board board;                // The board associated with UCI

    // Opening book
    private boolean useBook;            // Are we currently using book to find moves?
    private Book openingBook;           // The opening book we are using.
    private String bookFile;            // The book file we are using (if any)
    private String opening;             // Keeps track of the opening (used by the book)

    // Time controls
    private int timeControls;
    private int fixedDepth;
    private int fixedNoOfNodes;
    private int movesToGo;
    private int moveTime;
    private int[] timeLeft = new int[NO_OF_COLORS];
    private int[] inc = new int[NO_OF_COLORS];

    // Functions

    public UCIHandler() {

        // Initializes the search
        search = new Search(this);

        System.out.printf("Configuration: %s\n", search.getConfiguration());

        // TODO: May be moved away from the constructor.
        useBook = Options.getInstance().getOptionBoolean("OwnBook");
        if (useBook) {
            bookFile = Options.getInstance().getOptionString("Book File");
            opening = "";

            try {
                openingBook = Book.getInstance();
                openingBook.parseBook(bookFile);
            } catch (IOException ioex) {
                System.out.println("Error: Could not open the opening book.");
                ioex.printStackTrace(System.out);
            }
        }
    }

    private void parseTimeControls(String key, int value) {

        // Java seriously needs to support Strings in switch statements (Java 1.7!)

        if (key.equals("movetime")) {

            timeControls |= TC_MOVETIME;
            moveTime = value;

        } else if (key.equals("depth")) {

            timeControls |= TC_FIXEDDEPTH;
            fixedDepth = value;

        } else if (key.equals("nodes")) {

            timeControls |= TC_NODES;
            fixedNoOfNodes = value;

        } else if (key.equals("mate")) {

            timeControls |= TC_INFINITE;
            System.out.println("Mate: NOT Supported!");

        } else if (key.equals("movestogo")) {

            timeControls |= TC_MOVESTOGO;
            movesToGo = value;

        } else if (key.equals("wtime")) {

            timeControls |= TC_TIMELEFT;
            timeLeft[WHITE] = value;

        } else if (key.equals("winc")) {

            timeControls |= TC_INC;
            inc[WHITE] = value;

        } else if (key.equals("btime")) {

            timeControls |= TC_TIMELEFT;
            timeLeft[BLACK] = value;

        } else if (key.equals("binc")) {

            timeControls |= TC_INC;
            inc[BLACK] = value;

        } else {
            assert false : "No time control goes by the name " + key;
        }

    }

    public int getTimeForThisMove(int sideToMove) {

        int time = 0;

        if ((timeControls & TC_MOVETIME) != 0) {

            // 	UCI 'movetime': Search exactly x milliseconds.
            time = moveTime;

        } else if ((timeControls & TC_INFINITE) != 0) {

            // UCI 'infinite': Search until the "stop" command. Do not exit the search without being told so in this mode!
            time = Integer.MAX_VALUE; // c.a. 24 days

        } else if ((timeControls & TC_TIMELEFT) != 0) {

            // Side-to-move has x milliseconds left on the clock.
            // Allocates a total of ALLOCATION_PERCENTAGE of the time that we have left on the clock for this move.
            // I'm guessing there are better ways to handle the time management, but ...
            time = (int) (timeLeft[sideToMove] * ALLOCATION_PERCENTAGE);

        }

        if ((timeControls & TC_INC) != 0) {

            // Side-to-move increment per move in milliseconds
            time += inc[sideToMove];

        }

        if (time > timeLeft[sideToMove]) {
            // Can't imagine this happens often, but just in case.
            time = (int) (timeLeft[sideToMove] * 0.5);
        }

        System.out.printf("Time left: %d. Allocated time: %d\n", timeLeft[sideToMove], time);

        return time;
    }

    public void handle(String message) {

        String[] tokens = message.split(" ");
        String command = tokens[0];

        if (command.equals("uci")) {

            System.out.printf("id name %s %s\n", Application.NAME, Application.VERSION);
            System.out.printf("id author %s\n", Application.AUTHOR);
            System.out.println("uciok");

        } else if (command.equals("isready")) {

            System.out.println("readyok");

        } else if (command.equals("quit")) {

            System.exit(0);

        } else if (command.equals("ucinewgame")) {

            // TODO: this.

        } else if (command.equals("position")) {

            // Parse FEN/startpos:
            int movePosition = -1;
            if (tokens[1].equals("startpos")) {

                // TODO: worst thing ever.
                board = new Board();
                board.initialize();

                movePosition = 3;
            } else {
                // We won't begin with the start position, so we can't use the book.
                useBook = false;

                board = new Board();
                board.initialize(
                        String.format("%s %s %s %s", tokens[2], tokens[3], tokens[4], tokens[5]));

                if (tokens[6].equals("moves")) {
                    movePosition = 7;
                } else if (tokens[8].equals("moves")) {
                    movePosition = 9;
                } else {
                    assert false : "FEN string couldn't be parsed.";
                }
            }

            // Parse the moves:
            if (tokens.length > (movePosition - 1)) {
                if (useBook) {
                    opening = "";
                }
                for (int i = movePosition; i < tokens.length; i++) {
                    int move = Move.fromLAN(tokens[i], board);
                    try {
                        board.make(move);
                        opening += tokens[i];
                    } catch (Exception e) {
                        System.out.println("There is something wrong with this move: " + tokens[i]);
                        e.printStackTrace(System.out);
                    }
                }
            }

        } else if (command.equals("go")) {

            // Parse the time controls for this move
            for (int a = 1; a < tokens.length; a += 2) {

                fixedDepth = 0;
                fixedNoOfNodes = 0;

                if (tokens[a].equals("searchmoves")) {
                    // TODO: handle this differently
                } else if (tokens[a].equals("infinite")) {
                    timeControls |= TC_INFINITE;
                } else if (tokens[a].equals("ponder")) {
                    System.out.println("Ponder: NOT supported!");
                } else {
                    parseTimeControls(tokens[a], Integer.parseInt(tokens[a + 1]));
                }
            }

            // Check if we have a book move available
            int bestMove = Move.MOVE_NONE;
            if (useBook) {
                String bookMove = openingBook.getNextMove(opening);

                if (bookMove == null) {
                    useBook = false;
                } else {
                    bestMove = Move.fromLAN(bookMove, board);
                }
            }

            // We don't have a book move available - go search!
            if (bestMove == Move.MOVE_NONE) {
                bestMove = search.think(board, fixedDepth, fixedNoOfNodes);
            }

            board.make(bestMove);
            System.out.printf("bestmove %s\n", Move.toLAN(bestMove));

        }
    }

}
