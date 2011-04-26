package is.ru.cadia.ce;

import is.ru.cadia.ce.board.Board;
import is.ru.cadia.ce.board.MoveGenerator;
import is.ru.cadia.ce.board.SEE;

public class MoveSelector {

    // Types

    enum GenerationPhase {
        PV_MOVE,
        CAPTURES,
        NON_CAPTURES,
        ALL,
        NONE
    }

    // Constants

    public static final GenerationPhase[] PV_SEARCH = {
            GenerationPhase.PV_MOVE,
            GenerationPhase.CAPTURES,
            GenerationPhase.NON_CAPTURES,
            GenerationPhase.NONE
    };

    public static final GenerationPhase[] NO_PV_SEARCH = {
            GenerationPhase.CAPTURES,
            GenerationPhase.NON_CAPTURES,
            GenerationPhase.NONE
    };

    public static final GenerationPhase[] PERFT_PHASES = {
            GenerationPhase.ALL,
            GenerationPhase.NONE
    };

    public static final GenerationPhase[] QSEARCH = {
            GenerationPhase.CAPTURES,
            GenerationPhase.NONE
    };

    // TODO: Make qsearch only generate good captures.
    // TODO: Make evasion phase generate only evasions!

    // Variables

    private SEE see;
    private MoveGenerator generator;
    private MoveStack moveStack;

    private Board board;
    private int pvMove;
    private boolean isCheck;
    private int generationPhase;

    private GenerationPhase[] phaseType;

    // Functions

    public MoveSelector() {
        this.generator = MoveGenerator.getInstance();
        this.see = SEE.getInstance();
        this.moveStack = new MoveStack(200);
    }

    public void initialize(Board board, int pvMove, boolean isCheck, boolean qsearch) {

        this.board = board;
        this.pvMove = pvMove;
        this.isCheck = isCheck;
        this.generationPhase = 0;
        this.moveStack.setSize(0);

        if (qsearch) {
            this.phaseType = QSEARCH;
        } else if (pvMove == Move.MOVE_NONE) {
            this.phaseType = NO_PV_SEARCH;
        } else {
            this.phaseType = PV_SEARCH;
        }
    }

    public int getNextMove() {

        // Check if we have some _valid_ move ready:
        while (!moveStack.isEmpty()) {
            MoveStackItem item = moveStack.pop();
            if (board.isValidMove(item.move, isCheck) && item.move != pvMove) {
                return item.move;
            }
        }

        do {
            int noOfMoves;
            // Goes to the next phase:
            switch (phaseType[generationPhase]) {

                case PV_MOVE:
                    if (pvMove != Move.MOVE_NONE && pvMove != 0 && board.isValidMove(pvMove, isCheck)) {
                        generationPhase++;
                        return pvMove;
                    }
                    break;

                case ALL:
                    // Only for debugging purposes
                    generator.generatePseudoMoves(board, moveStack, isCheck);
                    break;

                case CAPTURES:
                    noOfMoves = generator.generateCaptureMoves(board, moveStack);
                    if (noOfMoves > 0) {
                        sortCaptureMoves(board, noOfMoves);
                    }
                    break;

                case NON_CAPTURES:
                    generator.generateNonCaptureMoves(board, moveStack, isCheck);
                    // TODO: rate non captures.
                    break;

                case NONE:
                    return Move.MOVE_NONE;

                default:
                    assert false : "Default case.";
            }

            generationPhase++;

        } while (moveStack.isEmpty());

        return getNextMove();
    }

    public void sortCaptureMoves(Board board, int noOfMoves) {

        // Score the captures, according to a SEE score:
        for (int index = moveStack.getSize() - 1, count = 0; count < noOfMoves; count++, index--) {
            MoveStackItem item = moveStack.get(index);
            item.score = see.see(item.move, board);
        }

        insertionSort(moveStack.moves, moveStack.getSize() - noOfMoves, noOfMoves);
    }

    public static void insertionSort(Comparable[] list, int begin, int end) {
        for (int i = begin + 1; i < end; i++) {
            Comparable key = list[i];
            int position = i;

            //shift larger values to the right
            while (position > 0 && key.compareTo(list[position - 1]) < 0) {
                list[position] = list[position - 1];
                position--;
            }
            list[position] = key;
        }
    }

}