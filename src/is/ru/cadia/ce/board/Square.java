package is.ru.cadia.ce.board;

import is.ru.cadia.ce.Piece;
import is.ru.cadia.ce.other.Constants;

public class Square implements Constants {

    /*
     * Square representation
     * 
     * 0-2: piece (3 bits)
     * 3-4: color (2 bits)
     */

    // Constants

    // Square names
    private static final String[] SQUARE_NAMES = {
            "", "", "", "", "A1", "B1", "C1", "D1", "E1", "F1", "G1", "H1", "", "", "",
            "", "", "", "", "A2", "B2", "C2", "D2", "E2", "F2", "G2", "H2", "", "", "",
            "", "", "", "", "A3", "B3", "C3", "D3", "E3", "F3", "G3", "H3", "", "", "",
            "", "", "", "", "A4", "B4", "C4", "D4", "E4", "F4", "G4", "H4", "", "", "",
            "", "", "", "", "A5", "B5", "C5", "D5", "E5", "F5", "G5", "H5", "", "", "",
            "", "", "", "", "A6", "B6", "C6", "D6", "E6", "F6", "G6", "H6", "", "", "",
            "", "", "", "", "A7", "B7", "C7", "D7", "E7", "F7", "G7", "H7", "", "", "",
            "", "", "", "", "A8", "B8", "C8", "D8", "E8", "F8", "G8", "H8", "", "", "",
    };

    // Rank and file tables

    private static final int[] RANKS = {
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0,
            0, 0, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 0, 0, 0,
            0, 0, 0, 0, 3, 3, 3, 3, 3, 3, 3, 3, 0, 0, 0,
            0, 0, 0, 0, 4, 4, 4, 4, 4, 4, 4, 4, 0, 0, 0,
            0, 0, 0, 0, 5, 5, 5, 5, 5, 5, 5, 5, 0, 0, 0,
            0, 0, 0, 0, 6, 6, 6, 6, 6, 6, 6, 6, 0, 0, 0,
            0, 0, 0, 0, 7, 7, 7, 7, 7, 7, 7, 7, 0, 0, 0,
            0, 0, 0, 0, 8, 8, 8, 8, 8, 8, 8, 8, 0, 0, 0
    };

    private static final int[] FILES = {
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 0, 0, 0,
            0, 0, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 0, 0, 0,
            0, 0, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 0, 0, 0,
            0, 0, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 0, 0, 0,
            0, 0, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 0, 0, 0,
            0, 0, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 0, 0, 0,
            0, 0, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 0, 0, 0,
            0, 0, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 0, 0, 0
    };

    // Ranks and (files)
    public static final int RANK_1 = 1;
    public static final int RANK_2 = 2;
    public static final int RANK_7 = 7;
    public static final int RANK_8 = 8;

    /*static final int FILE_A = 1; final int FILE_B = 2; final int FILE_C = 3; final int FILE_D = 4;
    static final int FILE_E = 5; final int FILE_F = 6; final int FILE_G = 7; final int FILE_H = 8;*/

    public static final int NONE = -1;
    public static final int WHITE = 0x0;
    public static final int BLACK = 0x1;
    public static final int EMPTY = 0x20;
    public static final int BORDER = 0x21;

    // Functions

    public static int createSquare(int color, int piece) {
        return piece | (color << 3);
    }

    public static int getColor(int square) {
        return (square >> 3);
    }

    public static int getPiece(int square) {
        return square & 7;
    }

    public static boolean isOfColor(int square, int color) {
        return getColor(square) == color;
    }

    public static boolean isPiece(int square, int piece) {
        return getPiece(square) == piece;
    }

    public static boolean isPieceAndColor(int square, int piece, int color) {
        return isPiece(square, piece) && isOfColor(square, color);
    }

    public static boolean isEmpty(int square) {
        return square == EMPTY;
    }

    public static boolean isBorder(int square) {
        return square == BORDER;
    }

    public static boolean isPromote(int square) {
        return (getRank(square) == RANK_8 || getRank(square) == RANK_1);
    }

    public static int getRank(int square) {
        return RANKS[square];
    }

    public static int getFile(int square) {
        return FILES[square];
    }

    public static char fileToChar(int file) {
        return (char) ('a' + file - 1);
    }

    public static int squareNameToIndex(char squareFile, char squareRank) {

        int file = Character.toLowerCase(squareFile) - 'a';
        int rank = (Character.getNumericValue(squareRank) - 1);

        return ((Board.UPPER_BORDER_SIZE + rank) * Board.NO_OF_FILES) +
                (Board.LEFT_BORDER_SIZE + file);
    }

    public static String getSquareName(int square) {

        int indexToArray = square - (Board.UPPER_BORDER_SIZE * Board.NO_OF_FILES);
        return SQUARE_NAMES[indexToArray];
    }

    // For debugging purposes

    public static char squareToChar(int square) {

        int color = getColor(square);
        int piece = getPiece(square);
        char pieceName = Piece.getPieceName(piece, false);

        assert piece >= PAWN && piece <= QUEEN : "PieceIndex was " + piece;

        return (color == WHITE ? Character.toUpperCase(pieceName) : pieceName);
    }
}