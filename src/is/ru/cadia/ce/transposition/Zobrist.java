package is.ru.cadia.ce.transposition;

import is.ru.cadia.ce.board.Board;
import is.ru.cadia.ce.board.Square;
import is.ru.cadia.ce.other.Constants;

import java.util.Random;

public class Zobrist implements Constants {

    // TODO: Can be fixed, optimized and cleaned up.

    // Constants & variables

    private static final Random random = new Random(32L);

    public static final long[][][] PIECES = new long[NO_OF_PIECES][NO_OF_COLORS][Board.NO_OF_SQUARES];
    public static final long[] EP = new long[Board.NO_OF_SQUARES];
    public static final long[] CASTLING = new long[16];
    public static final long SIDE_TO_MOVE;

    // Functions

    static {

        // Generates random keys for every piece, on each square, of each color:
        for (int a = 0; a < QUEEN; a++) {
            for (int b = 0; b < NO_OF_COLORS; b++) {
                for (int c = 0; c < Board.NO_OF_SQUARES; c++) {
                    PIECES[a][b][c] = Math.abs(random.nextLong());
                }
            }
        }

        // Generates random keys for each possible EP-square:
        for (int a = 0; a < 128; a++) {
            EP[a] = Math.abs(random.nextLong());
        }

        // Generates random keys for each possible castling:
        for (int a = 0; a < 16; a++) {
            CASTLING[a] = Math.abs(random.nextLong());
        }

        SIDE_TO_MOVE = Math.abs(random.nextLong());
    }

    public static long getZobristKey(final Board board) {

        long key = 0L;

        // Every piece on the board:
        for (int a = 0; a < Board.NO_OF_SQUARES; a++) {
            
            int square = board.squares[a];
            if (!Square.isBorder(square) && !Square.isEmpty(square)) {
                int piece = Square.getPiece(square);
                int color = Square.getColor(square);
                key ^= PIECES[piece - 1][color][square];
            }
        }

        key ^= CASTLING[board.castle]; // Castling

        if (board.epSquare != Board.NO_EP) { // EP square
            key ^= EP[board.epSquare];
        }

        if (board.sideToMove == BLACK) { // Side to move (only if black has turn)
            key ^= SIDE_TO_MOVE;
        }

        return key;
    }
}