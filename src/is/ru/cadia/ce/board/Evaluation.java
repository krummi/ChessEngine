package is.ru.cadia.ce.board;

import is.ru.cadia.ce.SquareTables;
import is.ru.cadia.ce.other.Constants;

public class Evaluation implements Constants, SquareTables {

    // Constants

    // Initializes the piece-square scoring tables.
    public static int OPEN_MID_POSITIONING[][][] =
            new int[NO_OF_PIECES][NO_OF_COLORS][Board.NO_OF_SQUARES];
    
    public static int END_POSITIONING[][][] =
            new int[NO_OF_PIECES][NO_OF_COLORS][Board.NO_OF_SQUARES];

    // Functions

    public static void initialize() {

        // Initializes the opening/mid piece-square-scoring table
        int[][] openingMiddleTables = {{},
                PAWNS_OPEN_MID, KNIGHTS_OPEN_MID, KINGS_OPEN_MID,
                BISHOPS_OPEN_MID, ROOKS_OPEN_MID, QUEENS_OPEN_MID};

        for (int piece = PAWN; piece < openingMiddleTables.length; piece++) {
            // TODO: Reverse the table for BLACK!
            for (int color = 0; color < NO_OF_COLORS; color++) {
                for (int square = (Board.NO_OF_FILES * Board.UPPER_BORDER_SIZE) + Board.LEFT_BORDER_SIZE, index = 0;
                     square < Board.NO_OF_SQUARES - (Board.LOWER_BORDER_SIZE * Board.NO_OF_FILES);
                     square += Board.NO_OF_FILES, index++) {

                    System.arraycopy(openingMiddleTables[piece], index * 8, OPEN_MID_POSITIONING[piece - 1][color], square, 8);

                }
            }
        }

        // Initializes the opening/mid piece-square-scoring table
        int[][] endTables = {{},
                PAWNS_END, KNIGHTS_END, KINGS_END, BISHOPS_END, ROOKS_END, QUEENS_END};
        for (int piece = PAWN; piece < endTables.length; piece++) {
            // TODO: Reverse the table for BLACK!
            for (int color = 0; color < NO_OF_COLORS; color++) {
                for (int square = (Board.NO_OF_FILES * Board.UPPER_BORDER_SIZE) + Board.LEFT_BORDER_SIZE, index = 0;
                     square < Board.NO_OF_SQUARES - (Board.LOWER_BORDER_SIZE * Board.NO_OF_FILES);
                     square += Board.NO_OF_FILES, index++) {

                    System.arraycopy(openingMiddleTables[piece], index * 8, END_POSITIONING[piece - 1][color], square, 8);

                }
            }
        }
    }

    public static int evaluate(Board board, boolean debug) {

        // Calculates the material balance:
        int whiteMaterial = board.info.material[WHITE] + board.info.pawnMaterial[WHITE];
        int blackMaterial = board.info.material[BLACK] + board.info.pawnMaterial[BLACK];
        int material = whiteMaterial - blackMaterial;

        // Calculate the positioning balance (for the opening/middle-game and the end-game):
        int whitePosOpenMid = board.info.openMidPositioning[WHITE];
        int blackPosOpenMid = board.info.openMidPositioning[BLACK];
        int posOpenMid = whitePosOpenMid - blackPosOpenMid;

        int whitePosEnd = board.info.endPositioning[WHITE];
        int blackPosEnd = board.info.endPositioning[BLACK];
        int posEnd = whitePosEnd - blackPosEnd;

        int evaluation = material + posOpenMid + posEnd;
        if (board.sideToMove == BLACK) { evaluation *= -1; } // TODO: refine.

        if (debug) {
            System.out.printf("-----------------+-------+-------+---------+\n");
            System.out.printf("Eval. method     | White | Black | Balance |\n");
            System.out.printf("-----------------+-------+-------+---------+\n");
            System.out.printf("Material         |%6d |%6d |%8d |\n", whiteMaterial, blackMaterial, material);
            System.out.printf("Pos (open/mid)   |%6d |%6d |%8d |\n", whitePosOpenMid, blackPosOpenMid, posOpenMid);
            System.out.printf("Pos (end)        |%6d |%6d |%8d |\n", whitePosEnd, blackPosEnd, posEnd);
            System.out.printf(">>> Final score:  %d\n", evaluation);
        }

        // TODO: Add some kind of phase-1 lazy evaluation here?

        // PHASE 2:
        //byte[] WB = genBoardTable(board, WHITE);
        //byte[] BB = genBoardTable(board, BLACK);

        return evaluation;
    }

    /*public static byte[] genBoardTable(Board board, int color) {

        byte[] table = new byte[128];

        //for( )

        for (int i = 0; i < 128; i++) {
            if (table[i] != 0) {
                System.out.printf(
                        "Square %s: Attackers: %d\n", Square.SQUARE_NAMES[i], (table[i] & 7));
            }
        }

        return table;
    }

    public static void genMovesForPawn(PieceEntry piece, byte[] table, int color) {

        int[] deltas = (color == WHITE ? new int[]{NW, NE} : new int[]{SW, SE});
        for (int i : deltas) {
            int square = piece.square + i;

            if (Board.isSquare(square)) {
                continue;
            }

            table[square]++;
            table[square] |= PAWN_BIT;
        }
    }

    public static void genMovesForKnight(PieceEntry piece, byte[] table, int color) {

        int[] deltas = PIECE_DELTAS[KNIGHT];
        for (int i : deltas) {
            int square = piece.square + i;

            if (Board.isSquare(square)) {
                continue;
            }

            table[square]++;
            table[square] |= MINOR_BIT;
        }
    } */
}
