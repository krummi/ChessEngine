package is.ru.cadia.ce;

import is.ru.cadia.ce.other.Constants;

public class Value implements Constants {

    // Constants

    // General values used in search algorithms
    public static final int DRAW = 0;
    public static final int MATE = 30000;
    public static final int INFINITY = 30001;

    // Piece values within the evaluation
    private static final int VALUE_PAWN = 100;
    private static final int VALUE_KNIGHT = 300;
    private static final int VALUE_BISHOP = 325;
    private static final int VALUE_ROOK = 500;
    private static final int VALUE_QUEEN = 950;
    private static final int VALUE_KING = 10000;

    // Phase values
    private static final int PHASE_PAWN = 0;
    private static final int PHASE_KNIGHT = 1;
    private static final int PHASE_BISHOP = 1;
    private static final int PHASE_ROOK = 2;
    private static final int PHASE_QUEEN = 4;
    public static final int INITIAL_PHASE =
            PHASE_PAWN * 16 + PHASE_KNIGHT * 4 + PHASE_BISHOP * 4 + PHASE_ROOK * 4 + PHASE_QUEEN * 2; // 24

    // Functions

    private static final int PIECE_VALUES[] = {
            0, VALUE_PAWN, VALUE_KNIGHT, VALUE_KING, VALUE_BISHOP, VALUE_ROOK, VALUE_QUEEN
    };

    private static final int PHASE_VALUES[] = {
            0, PHASE_PAWN, PHASE_KNIGHT, 0, PHASE_BISHOP, PHASE_ROOK, PHASE_QUEEN
    };

    public static int getPieceValue(int piece) {
        assert piece != 0: "Piece can't be 0";
        return PIECE_VALUES[piece];
    }

    public static int getPhaseValue(int piece) {
        assert piece != 0 && piece != KING : "Can't be 0 nor king.";
        return PHASE_VALUES[piece];
    }

}
