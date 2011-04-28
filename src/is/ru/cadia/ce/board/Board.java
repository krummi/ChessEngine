package is.ru.cadia.ce.board;

import is.ru.cadia.ce.*;
import is.ru.cadia.ce.other.Constants;
import is.ru.cadia.ce.transposition.Zobrist;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Board implements Constants {

    // Types

    public class EvaluationInfo {
        public int material[];
        public int pawnMaterial[];
        public int openMidPositioning[];
        public int endPositioning[];
        public int gamePhase;

        private EvaluationInfo() {
            this.material = new int[]{0, 0}; // White, Black;
            this.pawnMaterial = new int[]{0, 0};
            this.openMidPositioning = new int[]{0, 0}; // Sum of piece squares for opening/middle.
            this.endPositioning = new int[]{0, 0}; // Sum of piece square for the end game.
            this.gamePhase = 0;
        }
    }

    public class PieceList {

        // Constants

        private final int NO_INDEX = -1;

        // Variables

        public int[] counter;
        private int[] iteratorIndex;
        public int[][] squares;

        // Functions

        private PieceList(int maximumNoOfPieces) {

            counter = new int[]{0, 0};
            iteratorIndex = new int[]{0, 0};
            squares = new int[NO_OF_COLORS][maximumNoOfPieces];

            for (int a = 0; a < maximumNoOfPieces; a++) {
                squares[WHITE][a] = NO_INDEX;
                squares[BLACK][a] = NO_INDEX;
            }
        }

        public void add(int color, int square) {

            // Updates the indicis table:
            indicis[square] = counter[color];

            // Updates the piece-list:
            squares[color][counter[color]] = square;
            counter[color]++;
        }

        public void remove(int color, int index) {

            counter[color]--;

            // Nullify the indicis table at index:
            indicis[squares[color][index]] = NO_INDEX;

            // Gets the last piece-entry in this piece list and puts it at index.
            int square = squares[color][counter[color]];
            squares[color][index] = square;
            indicis[square] = index;
        }

        public int get(int color, int index) {
            return squares[color][index];
        }

        public int getNext(int color) {

            if (counter[color] == iteratorIndex[color]) {
                iteratorIndex[color] = 0;
                return Square.NONE;
            }

            return squares[color][iteratorIndex[color]++];
        }
    }


    // Constants

    public static final int NO_OF_RANKS = 12; // 12 x 15
    public static final int NO_OF_FILES = 15; // 12 x 15

    public static final int NO_OF_SQUARES = NO_OF_RANKS * NO_OF_FILES; // 180

    // Borders
    public static final int UPPER_BORDER_SIZE = 2;
    public static final int LOWER_BORDER_SIZE = 2;
    public static final int LEFT_BORDER_SIZE = 4;
    public static final int RIGHT_BORDER_SIZE = 3;

    // EP
    public static final int NO_EP = 0;

    // Castling
    public static int[] castleMask; // CastleMask (idea stolen from Fruit)

    private static final int WHITE_CASTLE_SHORT = 1; // Kingside (FEN: K)
    private static final int WHITE_CASTLE_LONG = 2; // Queenside (FEN: Q)
    private static final int BLACK_CASTLE_SHORT = 4; // Kingside (FEN: k)
    private static final int BLACK_CASTLE_LONG = 8; // Queenside (FEN: q)

    // The FEN regular expression pattern
    private static final Pattern FEN_PATTERN = Pattern.compile(
            "^([a-zA-Z1-8/]*) ([w|b]) (-|[KQkq]{1,4}) (-|[a-h][3|6]) ?(\\d+)? ?(?:\\d+)?.*$");

    // Declares the initial board state according to a FEN
    private static final String INITIAL_FEN =
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    // Variables
    public int[] squares;           // 15x8 board representation
    public int[] indicis;           // Indexes to the piece-lists
    public int sideToMove;          // Who's turn is it?
    public int castle;              // Castling availability (bits)
    public int epSquare;            // Is there some EP-flagged square?
    public int gamePly;             // Half-move clock
    public long key;                // The hash of this board state

    // Zobrist key history
    private long[] keyHistory = new long[5000];
    private int keyHistoryPtr = 0;

    // History stack
    private UndoInfo[] history;
    private int historyPtr = 0;

    // Piece-lists
    public PieceList pawns = new PieceList(8);
    public PieceList knights = new PieceList(2 + 8);
    public PieceList bishops = new PieceList(2 + 8);
    public PieceList rooks = new PieceList(2 + 8);
    public PieceList queens = new PieceList(1 + 8);
    public PieceList king = new PieceList(1);

    public EvaluationInfo info;

    // Instance functions

    public void initialize() {
        initialize(INITIAL_FEN);
    }

    public void initialize(String fen) {

        // Initializes all the piece entries for the board.
        squares = new int[NO_OF_SQUARES];
        indicis = new int[NO_OF_SQUARES];

        // Puts a border around the board and fills the actual board with "empty squares":
        for (int a = 0; a < NO_OF_SQUARES; a++) {
            squares[a] = Square.BORDER;
        }
        for (int a = 0; a < SQUARES_64.length; a++) {
            squares[SQUARES_64[a]] = Square.EMPTY;
        }

        // Fills the history with UndoInfo objects
        history = new UndoInfo[MAX_GAME_LENGTH];
        for (int a = 0; a < MAX_GAME_LENGTH; a++) {
            history[a] = new UndoInfo();
        }

        // Attach a EvaluationInfo to this object which is used by the evaluation-mechanism.
        info = new EvaluationInfo();

        // Initialize the board, according to a FEN:
        parseFEN(fen);

        // Set the zobrist key (hash-key) for this board position:
        key = Zobrist.getZobristKey(this);
    }

    public void make(int move) {

        // Initialize the UndoInfo instance we are going to use.
        UndoInfo undoInfo = history[historyPtr++];

        undoInfo.epSquare = epSquare;
        undoInfo.castle = castle;
        undoInfo.key = key;
        undoInfo.capture = false;
        undoInfo.gamePly = gamePly;

        // Get values for this move.
        int type = Move.getType(move);
        int from = Move.getFrom(move);
        int to = Move.getTo(move);
        int piece = Square.getPiece(squares[from]);

        // Updates the half-move clock.
        gamePly++;
        if (piece == PAWN) gamePly = 0;

        // Updates the key history stack.
        keyHistory[keyHistoryPtr++] = key;

        // Clears the "from" square.
        clearSquare(from, true);

        // Normal capture:
        if (Move.isNormalCapture(type)) {
            undoInfo.capture = true;
            undoInfo.capturedPiece = Square.getPiece(squares[to]);
            undoInfo.capturedSquare = to;
            clearSquare(to, true);

            gamePly = 0; // Reset the half-move clock.
        }

        // It's a EP capture:
        else if (Move.isEPCapture(type)) {
            undoInfo.capture = true;
            undoInfo.capturedPiece = PAWN;
            undoInfo.capturedSquare = epSquare + (sideToMove == WHITE ? S : N);
            clearSquare(undoInfo.capturedSquare, true);

            gamePly = 0; // Reset the half-move clock.
        }

        // Promotion:
        if (Move.isPromotion(type)) {
            piece = Move.getPromotion(move);
        }

        // The piece arrives at its "to" square
        fillSquare(to, sideToMove, piece, true);

        // En-passant move:
        if (epSquare != NO_EP) {
            key ^= Zobrist.EP[epSquare]; // Undoes the previous EP square from the hash-key.
            epSquare = NO_EP;
        }

        if (type == Move.MOVE_EP_FLAG) {
            epSquare = to + (sideToMove == WHITE ? S : N);
            key ^= Zobrist.EP[epSquare]; // Adds the new EP square to the hash-key.
        }

        // Castling
        if (Move.isCastle(type)) {
            int clear = -1, fill = -1;
            if (to == G1) {
                clear = H1;
                fill = F1;
            } else if (to == C1) {
                clear = A1;
                fill = D1;
            } else if (to == G8) {
                clear = H8;
                fill = F8;
            } else if (to == C8) {
                clear = A8;
                fill = D8;
            } else {
                assert false : "Should not happen.";
            }

            // Do the actual castling:
            fillSquare(fill, sideToMove, ROOK, true);
            clearSquare(clear, true);
        }

        if (anyCastleRights()) {

            // Takes a copy of the castle rights:
            int castleBak = castle;

            castle = castle & castleMask[to] & castleMask[from];

            if (castleBak != castle) {
                // Undoes the current castling rights from the hash-key.
                key ^= Zobrist.CASTLING[castleBak];

                // Adds the new castle to the hash key.
                key ^= Zobrist.CASTLING[castle];
            }
        }

        // Toggle the side to move
        sideToMove = oppColor(sideToMove);

        key ^= Zobrist.SIDE_TO_MOVE;
    }

    public void retract(int move) {

        // Get values for this move
        int type = Move.getType(move);
        int from = Move.getFrom(move);
        int to = Move.getTo(move);
        int piece = Square.getPiece(squares[to]);

        // Toggle the side to move:
        sideToMove = oppColor(sideToMove);

        // Retrieve all of the stuff from history:
        UndoInfo undoInfo = history[--historyPtr];

        // Updates the key history stack pointer.
        keyHistoryPtr --;

        // Reverse the promotion.
        if (Move.isPromotion(type)) {
            piece = PAWN;
        }

        // Puts the piece back to its initial location (from) and clears the "to"-square:
        clearSquare(to, false);
        fillSquare(from, sideToMove, piece, false);

        // Undo a capture.
        if (undoInfo.capture) {
            fillSquare(undoInfo.capturedSquare, oppColor(sideToMove), undoInfo.capturedPiece, false);
        }

        // Undo a castling
        else if (Move.isCastle(type)) {
            int clear = -1, fill = -1;
            if (to == G1) {
                clear = H1;
                fill = F1;
            } else if (to == C1) {
                clear = A1;
                fill = D1;
            } else if (to == G8) {
                clear = H8;
                fill = F8;
            } else if (to == C8) {
                clear = A8;
                fill = D8;
            } else {
                assert false : "Else case should not happen.";
            }

            // Do the actual castling:
            fillSquare(clear, sideToMove, ROOK, false);
            clearSquare(fill, false);
        }

        // Resets the necessary variables.
        epSquare = undoInfo.epSquare;
        castle = undoInfo.castle;
        key = undoInfo.key;
        gamePly = undoInfo.gamePly;
    }

    public void makeNullMove() {

        // Updates the key history stack.
        keyHistory[keyHistoryPtr++] = key;

        // Hash the EP square.
        if (epSquare != NO_EP) {
            key ^= Zobrist.EP[epSquare]; // Undoes the previous EP square from the hash-key.
            epSquare = NO_EP;
        }

        // Toggle the side to move
        sideToMove = oppColor(sideToMove);
        key ^= Zobrist.SIDE_TO_MOVE;

        // Update the half-move counter
        gamePly ++;
    }

    public void retractNullMove(int oldEpSquare) {

        // Updates the key history stack.
        keyHistoryPtr--;

        // Hash the EP square.
        if (oldEpSquare != NO_EP) {
            key ^= Zobrist.EP[oldEpSquare]; // Undoes the previous EP square from the hash-key.
            epSquare = oldEpSquare;
        }

        // Toggle the side to move
        sideToMove = oppColor(sideToMove);
        key ^= Zobrist.SIDE_TO_MOVE;

        // Update the half-move counter
        gamePly --;
    }

    public int[] getPiecesInMVOrder(int color) {

        int[] pieces = new int[17];
        int index = 0;
        int square;

        // King
        pieces[index++] = king.get(color, 0);

        // Queens
        while ((square = queens.getNext(color)) != Square.NONE) {
            pieces[index++] = square;
        }

        // Rooks
        while ((square = rooks.getNext(color)) != Square.NONE) {
            pieces[index++] = square;
        }

        // Bishops
        while ((square = bishops.getNext(color)) != Square.NONE) {
            pieces[index++] = square;
        }

        // Knights
        while ((square = knights.getNext(color)) != Square.NONE) {
            pieces[index++] = square;
        }

        // Fill the last one with Square.NONE:
        pieces[index] = Square.NONE;

        return pieces;
    }

    public boolean isAttacked(int square, int byColor) {

        // Pawns:
        int[] attackDeltas = (byColor == WHITE ? new int[]{SW, SE} : new int[]{NW, NE});
        for (int dir : attackDeltas) {
            int target = squares[square + dir];
            if (!Square.isBorder(target) && Square.isPieceAndColor(target, PAWN, byColor)) {
                return true;
            }
        }

        // Knights:
        attackDeltas = Increment.getPieceDeltas(KNIGHT);
        for (int dir : attackDeltas) {
            int target = squares[square + dir];
            if (!Square.isBorder(target) && Square.isPieceAndColor(target, KNIGHT, byColor)) {
                return true;
            }
        }

        // Kings:
        attackDeltas = Increment.getPieceDeltas(KING);
        for (int dir : attackDeltas) {
            int target = squares[square + dir];
            if (!Square.isBorder(target) && Square.isPieceAndColor(target, KING, byColor)) {
                return true;
            }
        }

        // Diagonally:
        attackDeltas = Increment.getPieceDeltas(BISHOP);
        for (int inc : attackDeltas) {
            for (int a = 1; ; a++) {
                int target = squares[square + inc * a];

                if (Square.isBorder(target)) {
                    break;
                }
                if (Square.isEmpty(target)) {
                    continue;
                }
                if (Square.isOfColor(target, byColor)
                        && (Square.isPiece(target, BISHOP) || Square.isPiece(target, QUEEN))) {
                    return true;
                } else {
                    break;
                }
            }
        }

        // Sideways:
        attackDeltas = Increment.getPieceDeltas(ROOK);
        for (int inc : attackDeltas) {
            for (int a = 1; ; a++) {
                int target = squares[square + inc * a];

                if (Square.isBorder(target)) {
                    break;
                }
                if (Square.isEmpty(target)) {
                    continue;
                }
                if (Square.isOfColor(target, byColor)
                        && (Square.isPiece(target, ROOK) || Square.isPiece(target, QUEEN))) {
                    return true;
                } else {
                    break;
                }
            }
        }

        return false;
    }

    public boolean isCheck() {
        return isAttacked(king.get(sideToMove, 0), oppColor(sideToMove));
    }

    public boolean isMate() {

        boolean isCheck = isCheck();
        if (!isCheck) return false;

        MoveSelector selector = new MoveSelector();
        selector.initialize(this, Move.MOVE_NONE, isCheck, false);

        int counter = 0, move;
        while ((move = selector.getNextMove()) != Move.MOVE_NONE) counter ++;

        return counter == 0;
    }

    public boolean isValidMove(int move, boolean isInCheck) {

        int type = Move.getType(move);
        int indexTo = Move.getTo(move);
        int indexFrom = Move.getFrom(move);
        int indexKing = king.get(sideToMove, 0);
        int piece = Square.getPiece(squares[indexFrom]);
        int inc = Increment.getIncrement(indexKing - indexFrom);

        assert Move.isOk(move);
        assert indexFrom != Move.getTo(move) : "to != from!";

        // We need to handle king moves, ep-captures and checks specifically:
        if (piece == KING || type == Move.MOVE_EP_CAPTURE || isInCheck) {
            make(move);
            boolean checked = isAttacked(king.get(oppColor(sideToMove), 0), sideToMove);
            retract(move);
            return (!checked);
        }

        if (inc == 0) {
            // No relation between the from and the king square!
            return true;
        }
        if (inc == Increment.getIncrement(indexKing - indexTo)) {
            // The piece moved within it's line - this could not have led to a invalid move.
            return true;
        }

        int target;

        for (target = indexKing + inc; Square.isEmpty(squares[target]); target += inc) ;
        if (target != indexFrom) {
            // There is a piece in the way between the king and the from square = valid move.
            return true;
        }

        for (target += inc; Square.isEmpty(squares[target]); target += inc) ;
        if (Square.isOfColor(squares[target], sideToMove)) {
            // A side-to-move piece was found - valid move!
            return true;
        }

        int attackPiece = Square.getPiece(squares[target]);
        return Increment.getPieceIncrement(attackPiece, indexKing - target) == 0;
    }

    public boolean isDraw() {

        // Fifty-move rule

        if (gamePly >= 100) return true; // TODO: fix when gamePly == 100.

        // "Threefold" repetition

        for (int i = 4; i <= gamePly; i += 2) {
            if (keyHistory[keyHistoryPtr - i] == key) {
                //System.out.println("DRAW detected by threefold repetition.");
                return true;
            }
        }

        return false;
    }

    public boolean whiteCanCastleLong() {
        return (castle & WHITE_CASTLE_LONG) != 0;
    }

    public boolean whiteCanCastleShort() {
        return (castle & WHITE_CASTLE_SHORT) != 0;
    }

    public boolean blackCanCastleLong() {
        return (castle & BLACK_CASTLE_LONG) != 0;
    }

    public boolean blackCanCastleShort() {
        return (castle & BLACK_CASTLE_SHORT) != 0;
    }

    public boolean anyCastleRights() {
        return castle != 0;
    }

    private void fillSquare(int square, int color, int piece, boolean rehash) {

        int sq = Square.createSquare(color, piece);

        if (rehash) {
            // Adds this piece on this square to the hash-key:
            key ^= Zobrist.PIECES[piece - 1][color][square];
        }

        // TODO: This does not need to be here.

        // Increase this sides material (whether it's pawn material or not):
        if (piece == PAWN) {
            info.pawnMaterial[color] += Value.getPieceValue(PAWN);
        } else if (piece != KING) {
            info.material[color] += Value.getPieceValue(piece);
            info.gamePhase += Value.getPhaseValue(piece);
        }

        // Increase this sides piece-square-sum-score:
        if (color == WHITE) {
            int revSquare = SquareTables.REVERSE_TABLE[square];
            info.openMidPositioning[color] += Evaluation.OPEN_MID_POSITIONING[piece - 1][color][revSquare];
            info.endPositioning[color] += Evaluation.END_POSITIONING[piece - 1][color][revSquare];
        } else {
            info.openMidPositioning[color] += Evaluation.OPEN_MID_POSITIONING[piece - 1][color][square];
            info.endPositioning[color] += Evaluation.END_POSITIONING[piece - 1][color][square];
        }

        // TODO: End - this does not need to be here.

        // Update the indicis table:
        switch (piece) {
        case PAWN:
            pawns.add(color, square);
            break;
        case KNIGHT:
            knights.add(color, square);
            break;
        case KING:
            king.add(color, square);
            break;
        case BISHOP:
            bishops.add(color, square);
            break;
        case ROOK:
            rooks.add(color, square);
            break;
        case QUEEN:
            queens.add(color, square);
            break;
        }

        // Update the square:
        squares[square] = sq;
    }

    private void clearSquare(int square, boolean rehash) {

        int color = Square.getColor(squares[square]);
        int piece = Square.getPiece(squares[square]);

        if (rehash) {
            // Undoes this piece on this square from the hash-key:
            key ^= Zobrist.PIECES[piece - 1][color][square];
        }

        // Decrease this sides material (whether it's pawn material or not):
        if (piece == PAWN) {
            info.pawnMaterial[color] -= Value.getPieceValue(PAWN);
        } else if (piece != KING) {
            info.material[color] -= Value.getPieceValue(piece);
            info.gamePhase -= Value.getPhaseValue(piece);
        }

        // Decrease this sides piece-square-sum-score:
        if (color == WHITE) {
            int revSquare = SquareTables.REVERSE_TABLE[square];
            info.openMidPositioning[color] -= Evaluation.OPEN_MID_POSITIONING[piece - 1][color][revSquare];
            info.endPositioning[color] -= Evaluation.END_POSITIONING[piece - 1][color][revSquare];
        } else {
            info.openMidPositioning[color] -= Evaluation.OPEN_MID_POSITIONING[piece - 1][color][square];
            info.endPositioning[color] -= Evaluation.END_POSITIONING[piece - 1][color][square];
        }



        // Updates the indicis table:
        int index = indicis[square];
        switch (piece) {
        case PAWN:
            pawns.remove(color, index);
            break;
        case KNIGHT:
            knights.remove(color, index);
            break;
        case KING:
            king.remove(color, index);
            break;
        case BISHOP:
            bishops.remove(color, index);
            break;
        case ROOK:
            rooks.remove(color, index);
            break;
        case QUEEN:
            queens.remove(color, index);
            break;
        }

        // Nullify the square:
        squares[square] = Square.EMPTY;
    }

    private void parseFEN(String fen) {

        // Parses the FEN components using a regular expression.
        Matcher matcher = FEN_PATTERN.matcher(fen);

        if (!matcher.find()) {
            assert false : "Bad FEN string: " + fen;
        }

        // (1) Piece placement

        int i = 0;
        for (int row = 7, column = 0; ; i++) {
            char c = fen.charAt(i);

            if (c == ' ') {
                break;
            } else if (c == '/') {
                row--;
                column = 0;
                continue;
            } else if (Character.isDigit(c)) {
                column += Character.getNumericValue(c);
                continue;
            }

            int color = Character.isUpperCase(c) ? WHITE : BLACK;
            c = Character.toLowerCase(c);
            int square = ((UPPER_BORDER_SIZE + row) * NO_OF_FILES) + (LEFT_BORDER_SIZE + column);

            switch (c) {
            case 'p':
                fillSquare(square, color, PAWN, false);
                break;
            case 'n':
                fillSquare(square, color, KNIGHT, false);
                break;
            case 'b':
                fillSquare(square, color, BISHOP, false);
                break;
            case 'r':
                fillSquare(square, color, ROOK, false);
                break;
            case 'q':
                fillSquare(square, color, QUEEN, false);
                break;
            case 'k':
                fillSquare(square, color, KING, false);
                break;
            }
            column++;
        }

        // (2) Active color.
        i++;
        sideToMove = fen.charAt(i) == 'w' ? WHITE : BLACK;

        // (3) Castling availability.
        for (i += 2; ; i++) {
            char c = fen.charAt(i);

            if (c == ' ') {
                break;
            }
            switch (c) {
            case 'K':
                castle |= WHITE_CASTLE_SHORT;
                break;
            case 'Q':
                castle |= WHITE_CASTLE_LONG;
                break;
            case 'k':
                castle |= BLACK_CASTLE_SHORT;
                break;
            case 'q':
                castle |= BLACK_CASTLE_LONG;
                break;
            }
        }

        // (4) En passant target square in AGN.
        if (fen.charAt(i + 1) != '-') {
            epSquare = Square.squareNameToIndex(fen.charAt(i + 1), fen.charAt(i + 2));
        } else {
            epSquare = NO_EP;
        }

        // (5) Half-move clock
        gamePly = (matcher.group(5) == null ? 0 : Integer.parseInt(matcher.group(5)));

    }


    // For debugging purposes

    public String castlingToString() {

        StringBuilder builder = new StringBuilder();

        if (whiteCanCastleShort()) {
            builder.append('K');
        }
        if (whiteCanCastleLong()) {
            builder.append('Q');
        }
        if (blackCanCastleShort()) {
            builder.append('k');
        }
        if (blackCanCastleLong()) {
            builder.append('q');
        }

        return (builder.length() > 0 ? builder.toString() : "-");
    }

    public void print() {

        System.out.println("Castle: " + castlingToString());
        System.out.println("Turn:   " + (sideToMove == WHITE ? "White" : "Black"));
        System.out.print("  a b c d e f g h\n8 ");

        for (int a = SQUARES_64.length - 8, b = 7; a >= 0; a -= 8) {
            for (int c = 0; c < 8; c++) {
                if (Square.isEmpty(squares[SQUARES_64[a + c]])) {
                    System.out.print(". ");
                } else {
                    System.out.print(Square.squareToChar(squares[SQUARES_64[a + c]]) + " ");
                }
            }
            if (b != 0) {
                System.out.print("\n" + b-- + " ");
            }
        }

        System.out.println("");
    }

    // Static functions

    public static int oppColor(int color) {
        return color ^ 1;
    }

    static {

        // Sets up the castle mask array:
        castleMask = new int[NO_OF_SQUARES];

        for (int a = 0; a < NO_OF_SQUARES; a++) {
            castleMask[a] = 0xFF;
        }

        castleMask[A1] = ~(WHITE_CASTLE_LONG);
        castleMask[E1] = ~(WHITE_CASTLE_LONG | WHITE_CASTLE_SHORT);
        castleMask[H1] = ~(WHITE_CASTLE_SHORT);
        castleMask[A8] = ~(BLACK_CASTLE_LONG);
        castleMask[E8] = ~(BLACK_CASTLE_LONG | BLACK_CASTLE_SHORT);
        castleMask[H8] = ~(BLACK_CASTLE_SHORT);

    }

}