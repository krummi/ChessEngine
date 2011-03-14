package is.ru.cadia.ce.board;

import is.ru.cadia.ce.Piece;
import is.ru.cadia.ce.other.Constants;

public class Increment implements Constants {

    // Constants

    private static final int INC_OFFSET = 112;



    private static final int[] kingInc = new int[]{  // TODO: fix!
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, NE,N,NW, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, E, 0, W, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, SE,S,SW, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    };

    private static final int[] bishopInc = new int[]{
            NE, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NW,
            0, NE, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NW, 0,
            0, 0, NE, 0, 0, 0, 0, 0, 0, 0, 0, 0, NW, 0, 0,
            0, 0, 0, NE, 0, 0, 0, 0, 0, 0, 0, NW, 0, 0, 0,
            0, 0, 0, 0, NE, 0, 0, 0, 0, 0, NW, 0, 0, 0, 0,
            0, 0, 0, 0, 0, NE, 0, 0, 0, NW, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, NE, 0, NW, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, SE, 0, SW, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, SE, 0, 0, 0, SW, 0, 0, 0, 0, 0,
            0, 0, 0, 0, SE, 0, 0, 0, 0, 0, SW, 0, 0, 0, 0,
            0, 0, 0, SE, 0, 0, 0, 0, 0, 0, 0, SW, 0, 0, 0,
            0, 0, SE, 0, 0, 0, 0, 0, 0, 0, 0, 0, SW, 0, 0,
            0, SE, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, SW, 0,
            SE, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, SW,
    };
       
    private static final int[] rookInc = new int[]{
            0, 0, 0, 0, 0, 0, 0, N, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, N, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, N, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, N, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, N, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, N, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, N, 0, 0, 0, 0, 0, 0, 0,
            E, E, E, E, E, E, E, 0, W, W, W, W, W, W, W,
            0, 0, 0, 0, 0, 0, 0, S, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, S, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, S, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, S, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, S, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, S, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, S, 0, 0, 0, 0, 0, 0, 0,
    };

    private static final int[] queenInc = new int[]{
            NE, 0, 0, 0, 0, 0, 0, N, 0, 0, 0, 0, 0, 0, NW,
            0, NE, 0, 0, 0, 0, 0, N, 0, 0, 0, 0, 0, NW, 0,
            0, 0, NE, 0, 0, 0, 0, N, 0, 0, 0, 0, NW, 0, 0,
            0, 0, 0, NE, 0, 0, 0, N, 0, 0, 0, NW, 0, 0, 0,
            0, 0, 0, 0, NE, 0, 0, N, 0, 0, NW, 0, 0, 0, 0,
            0, 0, 0, 0, 0, NE, 0, N, 0, NW, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, NE, N, NW, 0, 0, 0, 0, 0, 0,
            E, E, E, E, E, E,  E, 0,  W, W, W, W, W, W, W,
            0, 0, 0, 0, 0, 0, SE, S, SW, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, SE, 0, S, 0, SW, 0, 0, 0, 0, 0,
            0, 0, 0, 0, SE, 0, 0, S, 0, 0, SW, 0, 0, 0, 0,
            0, 0, 0, SE, 0, 0, 0, S, 0, 0, 0, SW, 0, 0, 0,
            0, 0, SE, 0, 0, 0, 0, S, 0, 0, 0, 0, SW, 0, 0,
            0, SE, 0, 0, 0, 0, 0, S, 0, 0, 0, 0, 0, SW, 0,
            SE, 0, 0, 0, 0, 0, 0, S, 0, 0, 0, 0, 0, 0, SW,
    };

    private static final int[] knightInc = new int[]{
            0, 0, 0, 0, 0,   0,   0,   0,   0,   0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0,   0,   0,   0,   0,   0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0,   0,   0,   0,   0,   0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0,   0,   0,   0,   0,   0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0,   0,   0,   0,   0,   0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0,   0, +31,   0, +29,   0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, +17,   0,   0,   0, +13, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0,   0,   0,   S,   0,   0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, -13,   0,   0,   0, -17, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0,   0, -29,   0, -31,   0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0,   0,   0,   0,   0,   0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0,   0,   0,   0,   0,   0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0,   0,   0,   0,   0,   0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0,   0,   0,   0,   0,   0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0,   0,   0,   0,   0,   0, 0, 0, 0, 0, 0,
    };

    private static final int[][] PIECE_DELTAS = new int[][]{
            {}, // Nothing
            {}, // Pawns
            {-17, -31, -29, -13, 17, 31, 29, 13}, // Knights
            {NW, N, NE, E, SE, S, SW, W}, // Kings
            {NW, NE, SE, SW}, // Bishops
            {N, E, S, W}, // Rooks
            {NW, N, NE, E, SE, S, SW, W} // Queens
    };

    public static int[][] INCS = new int[][]{
            // 0, pawns, knights kings    bishops    rooks    queens
            {}, {}, knightInc, kingInc, bishopInc, rookInc, queenInc
    };

    // Functions

    public static int[] getPieceDeltas(int piece) {

        assert piece != Piece.NONE && piece != PAWN : "Piece was " + piece;

        return PIECE_DELTAS[piece];
    }

    public static int getIncrement(int deltaSquare) {

        assert deltaSquare >= -INC_OFFSET
                && deltaSquare <= INC_OFFSET : "deltaSquare was " + deltaSquare;

        return queenInc[INC_OFFSET + deltaSquare];
    }

    public static int getPieceIncrement(int piece, int delta) {

        switch (piece) {
            case PAWN:
                return 0;
            case KING:
            case KNIGHT:
            case BISHOP:
            case ROOK:
            case QUEEN:
                return INCS[piece][INC_OFFSET + delta];
        }

        assert false;
        return 0;
    }

}
