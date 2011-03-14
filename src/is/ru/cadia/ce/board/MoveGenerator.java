package is.ru.cadia.ce.board;

import is.ru.cadia.ce.Move;
import is.ru.cadia.ce.MoveStack;
import is.ru.cadia.ce.Piece;
import is.ru.cadia.ce.other.Constants;

public class MoveGenerator implements Constants {

    // Constants

    private static final MoveGenerator ourInstance = new MoveGenerator();


    // Variables

    private int counter = 0;


    // Singleton functions

    public static MoveGenerator getInstance() {
        return ourInstance;
    }

    private MoveGenerator() {}


    // Public functions

    public int generatePseudoMoves(Board board, MoveStack moveStack, boolean inCheck) {

        int sum = 0;
        int sideToMove = board.sideToMove;
        int square;

        // Pawns

        while ((square = board.pawns.getNext(sideToMove)) != Square.NONE) {
            sum += generatePawnNonCaptures(board, moveStack, square, sideToMove);
            sum += generatePawnCaptures(board, moveStack, square, sideToMove);
        }

        // Knights

        while ((square = board.knights.getNext(sideToMove)) != Square.NONE) {
            sum += generateNonCapturesForNonSlider(board, moveStack, KNIGHT, square);
            sum += generateCapturesForNonSlider(board, moveStack, KNIGHT, square);
        }

        // Bishops

        while ((square = board.bishops.getNext(sideToMove)) != Square.NONE) {
            sum += generateNonCapturesForSlider(board, moveStack, BISHOP, square);
            sum += generateCapturesForSlider(board, moveStack, BISHOP, square);
        }

        // Rooks

        while ((square = board.rooks.getNext(sideToMove)) != Square.NONE) {
            sum += generateNonCapturesForSlider(board, moveStack, ROOK, square);
            sum += generateCapturesForSlider(board, moveStack, ROOK, square);
        }

        // Queens

        while ((square = board.queens.getNext(sideToMove)) != Square.NONE) {
            sum += generateNonCapturesForSlider(board, moveStack, QUEEN, square);
            sum += generateCapturesForSlider(board, moveStack, QUEEN, square);
        }

        // King

        sum += generateNonCapturesForNonSlider(board, moveStack, KING, board.king.get(sideToMove, 0));
        sum += generateCapturesForNonSlider(board, moveStack, KING, board.king.get(sideToMove, 0));

        // Castle moves (if we are not in check)

        if (!inCheck) {
            sum += generateCastleMoves(board, moveStack);
        }

        return sum;
    }

    public int generateCaptureMoves(Board board, MoveStack moveStack) {

        int sum = 0;
        int sideToMove = board.sideToMove;
        int square;

        // Pawns

        while ((square = board.pawns.getNext(sideToMove)) != Square.NONE) {
            sum += generatePawnCaptures(board, moveStack, square, sideToMove);
        }

        // Knights

        while ((square = board.knights.getNext(sideToMove)) != Square.NONE) {
            sum += generateCapturesForNonSlider(board, moveStack, KNIGHT, square);
        }

        // Bishops

        while ((square = board.bishops.getNext(sideToMove)) != Square.NONE) {
            sum += generateCapturesForSlider(board, moveStack, BISHOP, square);
        }

        // Rooks

        while ((square = board.rooks.getNext(sideToMove)) != Square.NONE) {
            sum += generateCapturesForSlider(board, moveStack, ROOK, square);
        }

        // Queens

        while ((square = board.queens.getNext(sideToMove)) != Square.NONE) {
            sum += generateCapturesForSlider(board, moveStack, QUEEN, square);
        }

        // King

        sum += generateCapturesForNonSlider(board, moveStack, KING, board.king.get(sideToMove, 0));

        return sum;
    }

    public int generateNonCaptureMoves(Board board, MoveStack moveStack, boolean isCheck) {

        int sum = 0;
        int sideToMove = board.sideToMove;
        int square;

        // Pawns

        while ((square = board.pawns.getNext(sideToMove)) != Square.NONE) {
            sum += generatePawnNonCaptures(board, moveStack, square, sideToMove);
        }

        // Knights

        while ((square = board.knights.getNext(sideToMove)) != Square.NONE) {
            sum += generateNonCapturesForNonSlider(board, moveStack, KNIGHT, square);
        }

        // Bishops

        while ((square = board.bishops.getNext(sideToMove)) != Square.NONE) {
            sum += generateNonCapturesForSlider(board, moveStack, BISHOP, square);
        }

        // Rooks

        while ((square = board.rooks.getNext(sideToMove)) != Square.NONE) {
            sum += generateNonCapturesForSlider(board, moveStack, ROOK, square);
        }

        // Queens

        while ((square = board.queens.getNext(sideToMove)) != Square.NONE) {
            sum += generateNonCapturesForSlider(board, moveStack, QUEEN, square);
        }

        // King

        sum += generateNonCapturesForNonSlider(board, moveStack, KING, board.king.get(sideToMove, 0));

        // Castle moves

        if (!isCheck) {
            sum += generateCastleMoves(board, moveStack);
        }

        return sum;
    }


    // Private functions

    private int generateNonCapturesForSlider(Board board, MoveStack moveStack, int piece, int from) {

        counter = 0;

        for (int inc : Increment.getPieceDeltas(piece)) {
            for (int to = from + inc; Square.isEmpty(board.squares[to]); to += inc) {
                addMove(moveStack, Move.MOVE_NORMAL, from, to, Piece.NONE);
            }
        }

        return counter;
    }

    private int generateNonCapturesForNonSlider(Board board, MoveStack moveStack, int piece, int from) {

        counter = 0;

        for (int inc : Increment.getPieceDeltas(piece)) {
            int to = from + inc;
            if (Square.isEmpty(board.squares[to])) {
                addMove(moveStack, Move.MOVE_NORMAL, from, to, Piece.NONE);
            }
        }

        return counter;
    }

    private int generateCapturesForSlider(Board board, MoveStack moveStack, int piece, int from) {

        counter = 0;

        for (int inc : Increment.getPieceDeltas(piece)) {
            int to;
            for (to = from + inc; Square.isEmpty(board.squares[to]); to += inc) ;
            if (Square.isOfColor(board.squares[to], Board.oppColor(board.sideToMove))) {
                addMove(moveStack, Move.MOVE_CAPTURE, from, to, Piece.NONE);
            }
        }

        return counter;
    }

    private int generateCapturesForNonSlider(Board board, MoveStack moveStack, int piece, int from) {

        counter = 0;

        for (int inc : Increment.getPieceDeltas(piece)) {
            int to = from + inc;
            if (Square.isOfColor(board.squares[to], Board.oppColor(board.sideToMove))) {
                addMove(moveStack, Move.MOVE_CAPTURE, from, to, Piece.NONE);
            }
        }

        return counter;
    }

    private int generatePawnCaptures(Board board, MoveStack moveStack, int square, int color) {

        counter = 0;

        // Capture moves (White: NW, NE) (Black: SW, SE).
        int[] deltas = (color == WHITE ? new int[]{NW, NE} : new int[]{SE, SW});

        for (int inc : deltas) {
            int target = square + inc;

            if (!Square.isBorder(board.squares[target])
                    && Square.isOfColor(board.squares[target], Board.oppColor(color))) {
                addPawnMove(board, moveStack, Move.MOVE_CAPTURE, square, target);
            }
            if (board.epSquare != Board.NO_EP && target == board.epSquare) {
                addMove(moveStack, Move.MOVE_EP_CAPTURE, square, target, Piece.NONE);
            }
        }

        return counter;
    }

    private int generatePawnNonCaptures(Board board, MoveStack moveStack, int square, int color) {

        // Symmetry!
        counter = 0;
        int dir, initialRank;
        if (color == WHITE) {
            dir = N;
            initialRank = Square.RANK_2;
        } else {
            dir = S;
            initialRank = Square.RANK_7;
        }

        int oneAhead = square + dir;

        if (Square.isEmpty(board.squares[oneAhead])) {
            // One ahead
            addPawnMove(board, moveStack, Move.MOVE_NORMAL, square, oneAhead);

            // Two ahead
            if (Square.getRank(square) == initialRank) {
                int twoAhead = oneAhead + dir;

                if (Square.isEmpty(board.squares[twoAhead])) {
                    addPawnMove(board, moveStack, Move.MOVE_EP_FLAG, square, twoAhead);
                }
            }
        }

        return counter;
    }

    private int generateCastleMoves(Board board, MoveStack moveStack) {

        counter = 0;
        int color = board.sideToMove;
        int[] squares = board.squares;

        if (color == WHITE) {
            if (board.whiteCanCastleShort()
                    && Square.isEmpty(squares[F1])
                    && Square.isEmpty(squares[G1])
                    && !board.isAttacked(F1, Board.oppColor(color))) {

                addMove(moveStack, Move.MOVE_CASTLE, E1, G1, Piece.NONE);

            }
            if (board.whiteCanCastleLong()
                    && Square.isEmpty(squares[B1])
                    && Square.isEmpty(squares[C1])
                    && Square.isEmpty(squares[D1])
                    && !board.isAttacked(D1, Board.oppColor(color))) {

                addMove(moveStack, Move.MOVE_CASTLE, E1, C1, Piece.NONE);

            }
        } else {
            if (board.blackCanCastleShort()
                    && Square.isEmpty(squares[F8])
                    && Square.isEmpty(squares[G8])
                    && !board.isAttacked(F8, Board.oppColor(color))) {

                addMove(moveStack, Move.MOVE_CASTLE, E8, G8, Piece.NONE);

            }
            if (board.blackCanCastleLong()
                    && Square.isEmpty(squares[B8])
                    && Square.isEmpty(squares[C8])
                    && Square.isEmpty(squares[D8])
                    && !board.isAttacked(D8, Board.oppColor(color))) {

                addMove(moveStack, Move.MOVE_CASTLE, E8, C8, Piece.NONE);

            }
        }

        return counter;
    }


    // Add move functions

    private void addPawnMove(Board board, MoveStack moveStack, int type, int from, int to) {

        // Promotion?
        if (Square.getRank(to) == Square.RANK_1 || Square.getRank(to) == Square.RANK_8) {

            if (!Square.isEmpty(board.squares[to])) {
                type = Move.MOVE_PROMOTION_CAPTURE;
            } else {
                type = Move.MOVE_PROMOTION;
            }

            // 4x promotions available (knight, bishop, rook, queen)!
            addMove(moveStack, type, from, to, KNIGHT);
            addMove(moveStack, type, from, to, BISHOP);
            addMove(moveStack, type, from, to, ROOK);
            addMove(moveStack, type, from, to, QUEEN);
        } else {
            addMove(moveStack, type, from, to, Piece.NONE);
        }
    }

    private void addMove(MoveStack moveStack, int type, int from, int to, int promotion) {
        counter++;
        moveStack.put(Move.createMove(type, from, to, promotion));
    }

}