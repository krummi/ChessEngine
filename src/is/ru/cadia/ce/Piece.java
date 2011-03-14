package is.ru.cadia.ce;

public class Piece {

    // Constants and variables

    private static final String PIECE_NAMES = " pnkbrq PNKBRQ";

    public static final int NONE = 0;

    // Functions

    public static boolean isSlidingPiece(int piece) {
        return (piece & 4) != 0;
    }

    public static char getPieceName(int piece, boolean upperCase) {
        return PIECE_NAMES.charAt(piece + (upperCase ? 7 : 0));
    }

}
