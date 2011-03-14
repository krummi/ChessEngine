package is.ru.cadia.ce;

import is.ru.cadia.ce.board.Board;
import is.ru.cadia.ce.board.Square;
import is.ru.cadia.ce.other.Constants;

public class Move implements Constants {

    /*
     * Move representation
     * 
     * 0-2:   Type (3 bits)
     * 3-10:   From square (8 bits)
     * 11-18: To square  (8 bits)
     * 19-21: Promotion (3 bits)
     * 22-32: Free for use - yet (10 bits)
     */

    // Constants

    // Move Types
    public static final int MOVE_NONE = -1;
    public static final int MOVE_NORMAL = 0;
    public static final int MOVE_CAPTURE = 1;
    public static final int MOVE_CASTLE = 2;
    public static final int MOVE_EP_FLAG = 3;
    public static final int MOVE_EP_CAPTURE = 4;
    public static final int MOVE_PROMOTION = 5;
    public static final int MOVE_PROMOTION_CAPTURE = 6;

    private final static int FROM_SHIFT = 3;
    private final static int TO_SHIFT = 11; /*3 + 7*/
    private final static int PROMOTION_SHIFT = 19; /*3 + 7 + 7*/


    // Functions

    public static int createMove(int type, int from, int to, int promotion) {
        return type | (from << FROM_SHIFT) | (to << TO_SHIFT) | (promotion << PROMOTION_SHIFT);
    }

    public static int getType(int move) {
        return (move & 7);
    }

    public static int getFrom(int move) {
        return (move >> FROM_SHIFT) & 255;
    }

    public static int getTo(int move) {
        return (move >> TO_SHIFT) & 255;
    }

    public static int getPromotion(int move) {
        return (move >> PROMOTION_SHIFT) & 7;
    }

    // Move flags

    public static boolean isPromotion(int type) {
        return type == MOVE_PROMOTION || type == MOVE_PROMOTION_CAPTURE;
    }

    public static boolean isCastle(int type) {
        return type == MOVE_CASTLE;
    }

    public static boolean isNormalCapture(int type) {    // TODO: Someday this might be fixed.
        return type == MOVE_CAPTURE || type == MOVE_PROMOTION_CAPTURE;
    }

    public static boolean isEPCapture(int type) {
        return type == MOVE_EP_CAPTURE;
    }

    public static boolean isCapture(int type) {
        return type == MOVE_CAPTURE || type == MOVE_PROMOTION_CAPTURE || type == MOVE_EP_CAPTURE;
    }

    // Short algebraic notation

    public static String toSAN(Board board, int move) {

        assert move != Move.MOVE_NONE : "Cannot be a null move.";

        int type = Move.getType(move);
        int from = Move.getFrom(move);
        int to = Move.getTo(move);
        int piece = Square.getPiece(board.squares[from]);

        StringBuilder sb = new StringBuilder();

        if (Move.isCastle(type)) {

            assert piece == KING;
            assert to == C1 || to == G1 || to == C8 || to == G8;

            // Determine whether it is a long or short castle.
            if (to == C1 || to == C8) sb.append("O-O-O");
            else sb.append("O-O");

        } else {

            if (piece != PAWN) {
                sb.append(Piece.getPieceName(piece, true));

                // TODO: ambiguity stuff!
            }
            if (Move.isCapture(type)) {
                if (piece == PAWN) sb.append(Square.fileToChar(Square.getFile(from)));
                sb.append("x");
            }
            sb.append(Square.getSquareName(to).toLowerCase());
            if (Move.isPromotion(type)) {
                sb.append("=");
                String[] promotionTypes = {"", "", "N", "", "B", "R", "Q"};
                sb.append(promotionTypes[Move.getPromotion(move)]);
            }
        }

        // TODO: # for mate (we need board.isMate() for this)
        board.make(move);
        if (board.isCheck()) sb.append("+");
        board.retract(move);

        return sb.toString();
    }

    // Long algebraic notation

    public static String toLAN(int move) {

        String[] promotionTypes = {"", "", "n", "", "b", "r", "q"};
        return String.format(
                "%s%s%s",
                Square.getSquareName(getFrom(move)).toLowerCase(),
                Square.getSquareName(getTo(move)).toLowerCase(),
                promotionTypes[getPromotion(move)]);
    }

    public static int fromLAN(String notation, Board board) {

        int type = MOVE_NORMAL;
        int from = Square.squareNameToIndex(notation.charAt(0), notation.charAt(1));
        int to = Square.squareNameToIndex(notation.charAt(2), notation.charAt(3));
        int piece = Square.getPiece(board.squares[from]);

        // If a piece was on the "to"-square, it means this was a capture move.
        if (!Square.isEmpty(board.squares[to])) {
            type = MOVE_CAPTURE;
        }

        // Castling:
        if (piece == KING && (
                (from == E1 && (to == C1 || to == G1)) || (from == E8 && (to == C8 || to == G8)))) {
            type = MOVE_CASTLE;
        }

        // En-passant flag:
        if (piece == PAWN && Math.abs(from - to) == (N * 2)) {
            type = MOVE_EP_FLAG;
        }

        // En-passant capture:
        if (piece == PAWN && type != MOVE_CAPTURE &&
                (Math.abs(from - to) == NE || Math.abs(from - to) == NW)) {  // HACK.
            type = MOVE_EP_CAPTURE;
        }

        // Promotions:
        int promotion = Piece.NONE;
        if (notation.length() == 5) {
            type = (type == MOVE_CAPTURE ? MOVE_PROMOTION_CAPTURE : MOVE_PROMOTION);

            switch (notation.charAt(4)) {
                case 'n':
                    promotion = KNIGHT;
                    break;
                case 'b':
                    promotion = BISHOP;
                    break;
                case 'r':
                    promotion = ROOK;
                    break;
                case 'q':
                    promotion = QUEEN;
                    break;
                default:
                    assert false : "Default case.";
            }
        }

        return Move.createMove(type, from, to, promotion);
    }

}
