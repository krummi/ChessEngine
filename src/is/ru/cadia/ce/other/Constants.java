package is.ru.cadia.ce.other;

public interface Constants {

    public static final boolean DEBUG = false;

    public static final int MAX_GAME_LENGTH = 300;
    public static final int MAX_PLY = 100;

    public static final int A1 = 0x22, B1 = 0x23, C1 = 0x24, D1 = 0x25, E1 = 0x26, F1 = 0x27, G1 = 0x28, H1 = 0x29;
    public static final int A2 = 0x31, B2 = 0x32, C2 = 0x33, D2 = 0x34, E2 = 0x35, F2 = 0x36, G2 = 0x37, H2 = 0x38;
    public static final int A3 = 0x40, B3 = 0x41, C3 = 0x42, D3 = 0x43, E3 = 0x44, F3 = 0x45, G3 = 0x46, H3 = 0x47;
    public static final int A4 = 0x4f, B4 = 0x50, C4 = 0x51, D4 = 0x52, E4 = 0x53, F4 = 0x54, G4 = 0x55, H4 = 0x56;
    public static final int A5 = 0x5e, B5 = 0x5f, C5 = 0x60, D5 = 0x61, E5 = 0x62, F5 = 0x63, G5 = 0x64, H5 = 0x65;
    public static final int A6 = 0x6d, B6 = 0x6e, C6 = 0x6f, D6 = 0x70, E6 = 0x71, F6 = 0x72, G6 = 0x73, H6 = 0x74;
    public static final int A7 = 0x7c, B7 = 0x7d, C7 = 0x7e, D7 = 0x7f, E7 = 0x80, F7 = 0x81, G7 = 0x82, H7 = 0x83;
    public static final int A8 = 0x8b, B8 = 0x8c, C8 = 0x8d, D8 = 0x8e, E8 = 0x8f, F8 = 0x90, G8 = 0x91, H8 = 0x92;

    public static final int[] SQUARES_64 = {
            A1, B1, C1, D1, E1, F1, G1, H1,
            A2, B2, C2, D2, E2, F2, G2, H2,
            A3, B3, C3, D3, E3, F3, G3, H3,
            A4, B4, C4, D4, E4, F4, G4, H4,
            A5, B5, C5, D5, E5, F5, G5, H5,
            A6, B6, C6, D6, E6, F6, G6, H6,
            A7, B7, C7, D7, E7, F7, G7, H7,
            A8, B8, C8, D8, E8, F8, G8, H8
    };

    // Colors
    public static final int WHITE = 0;
    public static final int BLACK = 1;
    public static final int NO_OF_COLORS = 2;

    // Piece types
    public static final int PAWN = 1;
    public static final int KNIGHT = 2;
    public static final int KING = 3;
    public static final int BISHOP = 4;
    public static final int ROOK = 5;
    public static final int QUEEN = 6;
    public static final int NO_OF_PIECES = 6;

    // Increments/deltas
    public static final int NW = +14; public static final int NE = +16;
    public static final int SW = -16; public static final int SE = -14;

    public static final int N = +15; public static final int S = -15;
    public static final int E =  +1; public static final int W =  -1; 

    // Game phases
    static final int PHASE_OPENING = 0;
    static final int PHASE_MIDDLE = 1;
    static final int PHASE_END = 2;
}