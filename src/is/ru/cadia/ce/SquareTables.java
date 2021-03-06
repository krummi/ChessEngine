package is.ru.cadia.ce;

import is.ru.cadia.ce.other.Constants;

/**
 * Piece score-ing for certain squares. (Due to the lack of chess skills this is more or less
 * stolen from Mediocre.)
 */
public interface SquareTables extends Constants {

    // Reverse/flip table:

    public static final int[] REVERSE_TABLE = {
            0, 0, 0, 0,  0,  0,  0,  0,  0,  0,  0,  0, 0, 0, 0,
            0, 0, 0, 0,  0,  0,  0,  0,  0,  0,  0,  0, 0, 0, 0,
            0, 0, 0, 0, A8, B8, C8, D8, E8, F8, G8, H8, 0, 0, 0,
            0, 0, 0, 0, A7, B7, C7, D7, E7, F7, G7, H7, 0, 0, 0,
            0, 0, 0, 0, A6, B6, C6, D6, E6, F6, G6, H6, 0, 0, 0,
            0, 0, 0, 0, A5, B5, C5, D5, E5, F5, G5, H5, 0, 0, 0,
            0, 0, 0, 0, A4, B4, C4, D4, E4, F4, G4, H4, 0, 0, 0,
            0, 0, 0, 0, A3, B3, C3, D3, E3, F3, G3, H3, 0, 0, 0,
            0, 0, 0, 0, A2, B2, C2, D2, E2, F2, G2, H2, 0, 0, 0,
            0, 0, 0, 0, A1, B1, C1, D1, E1, F1, G1, H1, 0, 0, 0
    };

    // Pawns:

    public static final int[] PAWNS_OPEN_MID = {
            -15, -5, 0,  5,  5, 0, -5, -15,
            -15, -5, 0,  5,  5, 0, -5, -15,
            -15, -5, 0,  5,  5, 0, -5, -15,
            -15, -5, 0, 15, 15, 0, -5, -15,
            -15, -5, 0, 25, 25, 0, -5, -15,
            -15, -5, 0, 15, 15, 0, -5, -15,
            -15, -5, 0,  5,  5, 0, -5, -15,
            -15, -5, 0,  5,  5, 0, -5, -15
    };

    /*
    public static final int[] PAWNS_END = {
            150, 150, 150, 150, 150, 150, 150, 150,
            100, 100, 100, 100, 100, 100, 100, 100,
            90,  90,  90,  90,  90,  90,  90,  90,
            70,  70,  70,  70,  70,  70,  70,  70,
            50,  50,  50,  50,  50,  50,  50,  50,
            20,  20,  20,  20,  20,  20,  20,  20,
           -10, -10, -10, -10, -10, -10, -10, -10,
            0,   0,   0,   0,   0,   0,   0,   0
    };*/

    public static final int[] PAWNS_END = {
            150, 150, 150, 150, 150, 150, 150, 150,
            50,  50,  50,  50,  50,  50,  50,  50,
            20,  20,  20,  20,  20,  20,  20,  20,
            10,  10,  10,  10,  10,  10,  10,  10,
            5,   5,   5,   5,   5,   5,   5,   5,
            0,   0,   0,   0,   0,   0,   0,   0,
            0,   0,   0,   0,   0,   0,   0,   0,
            0,   0,   0,   0,   0,   0,   0,   0
    };
    /*
    public static final int[] PAWNS_END = {
            0,   0,   0,   0,   0,   0,   0,   0,
            0,   0,   0,   0,   0,   0,   0,   0,
            0,   0,   0,   0,   0,   0,   0,   0,
            0,   0,   0,   0,   0,   0,   0,   0,
            0,   0,   0,   0,   0,   0,   0,   0,
            0,   0,   0,   0,   0,   0,   0,   0,
            0,   0,   0,   0,   0,   0,   0,   0,
            0,   0,   0,   0,   0,   0,   0,   0
    };*/

    // Knights:
    
    public static final int[] KNIGHTS_OPEN_MID = {
            -135, -25, -15, -10, -10, -15, -25, -135,
             -20, -10,   0,   5,   5,   0, -10,  -20,
              -5,   5,  15,  20,  20,  15,   5,   -5,
              -5,   5,  15,  20,  20,  15,   5,   -5,
             -10,   0,  10,  15,  15,  10,   0,  -10,
             -20, -10,   0,   5,   5,   0, -10,  -20,
             -35, -25, -15, -10, -10, -15, -25,  -35,
             -50, -40, -30, -25, -25, -30, -40,  -50
    };

    public static final int[] KNIGHTS_END = {
            -10, -5, -5, -5, -5, -5, -5, -10,
             -5,  0,  0,  0,  0,  0,  0,  -5,
             -5,  0,  5,  5,  5,  5,  0,  -5,
             -5,  0,  5, 10, 10,  5,  0,  -5,
             -5,  0,  5, 10, 10,  5,  0,  -5,
             -5,  0,  5,  5,  5,  5,  0,  -5,
             -5,  0,  0,  0,  0,  0,  0,  -5,
            -10, -5, -5, -5, -5, -5, -5, -10
    };

    // Bishops:

    public static final int[] BISHOPS_OPEN_MID = {
            -8,  -8,  -6,  -4,  -4,  -6,  -8,  -8,
            -5,   0,  -2,   0,   0,  -2,   0,  -5,
            -6,  -2,   4,   2,   2,   4,  -2,  -6,
            -4,   0,   2,  10,  10,   2,   0,  -4,
            -4,   0,   2,  10,  10,   2,   0,  -4,
            -6,  -2,   4,   2,   2,   4,  -2,  -6,
            -5,   0,  -5,   0,   0,  -5,   0,  -5,
            -20, -15, -15, -13, -13, -15, -15, -20
    };

    public static final int[] BISHOPS_END = {
            -18, -12, -9, -6, -6, -9, -12, -18,
            -12,  -6, -3,  0,  0, -3,  -6, -12,
             -9,  -3,  0,  3,  3,  0,  -3,  -9,
             -6,   0,  3,  6,  6,  3,   0,  -6,
             -6,   0,  3,  6,  6,  3,   0,  -6,
             -9,  -3,  0,  3,  3,  0,  -3,  -9,
            -12,  -6, -3,  0,  0, -3,  -6, -12,
            -18, -12, -9, -6, -6, -9, -12, -18
    };

    // Rooks:

    public static final int[] ROOKS_OPEN_MID = {
            -6, -3, 0, 3, 3, 0, -3, -6,
            -6, -3, 0, 3, 3, 0, -3, -6,
            -6, -3, 0, 3, 3, 0, -3, -6,
            -6, -3, 0, 3, 3, 0, -3, -6,
            -6, -3, 0, 3, 3, 0, -3, -6,
            -6, -3, 0, 3, 3, 0, -3, -6,
            -6, -3, 0, 3, 3, 0, -3, -6,
            -6, -3, 0, 3, 3, 0, -3, -6
    };

    public static final int[] ROOKS_END = {
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0
    };

    public static final int[] QUEENS_OPEN_MID = {
              0,   0,   0,   0,   0,   0,   0,   0,
              0,   0,   0,   0,   0,   0,   0,   0,
              0,   0,   0,   0,   0,   0,   0,   0,
              0,   0,   0,   0,   0,   0,   0,   0,
              0,   0,   0,   0,   0,   0,   0,   0,
              0,   0,   0,   0,   0,   0,   0,   0,
              0,   0,   0,   0,   0,   0,   0,   0,
            -10, -10, -10, -10, -10, -10, -10, -10
    };

    // Queens:

    public static final int[] QUEENS_END = {
            -24, -16, -12, -8, -8, -12, -16, -24,
            -16,  -8,  -4,  0,  0,  -4,  -8, -16,
            -12,  -4,   0,  4,  4,   0,  -4, -12,
             -8,   0,   4,  8,  8,   4,   0,  -8,
             -8,   0,   4,  8,  8,   4,   0,  -8,
            -12,  -4,   0,  4,  4,   0,  -4, -12,
            -16,  -8,  -4,  0,  0,  -4,  -8, -16,
            -24, -16, -12, -8, -8, -12, -16, -24
    };

    // Kings:

    public static final int[] KINGS_OPEN_MID = {
            -50, -50, -50, -50, -50, -50, -50, -50,
            -50, -50, -50, -50, -50, -50, -50, -50,
            -40, -50, -50, -50, -50, -50, -50, -40,
            -30, -40, -40, -40, -40, -40, -40, -30,
            -15, -25, -40, -40, -40, -40, -25, -15,
            -10, -20, -20, -25, -25, -20, -20, -10,
             10,  15,   0,   0,   0,   0,  15,  10,
             10,  20,   0,   0,   0,  10,  20,  10
    };

    public static final int[] KINGS_END = {
            -20, -15, -10, -10, -10, -10, -15, -20,
            -15,  -5,   0,   0,   0,   0,  -5, -15,
            -10,   0,   5,   5,   5,   5,   0, -10,
            -10,   0,   5,  10,  10,   5,   0, -10,
            -10,   0,   5,  10,  10,   5,   0, -10,
            -10,   0,   5,   5,   5,   5,   0, -10,
            -15,  -5,   0,   0,   0,   0,  -5, -15,
            -20, -15, -10, -10, -10, -10, -10, -20
    };
    
}