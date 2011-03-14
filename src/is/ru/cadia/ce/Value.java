package is.ru.cadia.ce;

public class Value {

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
    private static final int VALUE_KING = 10000; /* 1024 * 32 - 1*/

    private static final int PIECE_VALUES[] = {
            0, VALUE_PAWN, VALUE_KNIGHT, VALUE_KING, VALUE_BISHOP, VALUE_ROOK, VALUE_QUEEN
    };

    public static int getPieceValue(int piece) {

        assert piece != 0: "Piece can't be 0";

        return PIECE_VALUES[piece];
    }

}
