package is.ru.cadia.ce.board;

import is.ru.cadia.ce.Move;
import is.ru.cadia.ce.Piece;
import is.ru.cadia.ce.Value;
import is.ru.cadia.ce.other.Constants;

public class SEE implements Constants {

    // Singleton stuff

    private static final SEE ourInstance = new SEE();

    public static SEE getInstance() {
        return ourInstance;
    }

    // Types

    public class AttackerStack {

        private final static int MAX_ATTACKERS = 16;

        private int size;
        private int[] attackers;

        public AttackerStack() {
            this.size = 0;
            attackers = new int[MAX_ATTACKERS];
        }

        /**
         * Pushes a attacker on top of the stack.
         *
         * @param attacker the attacking square.
         */
        public void push(int attacker) {
            attackers[size++] = attacker;
        }

        /**
         * Pushes a attacker down the stack until it reaches it place (according to MV-piece order).
         *
         * @param attacker the attacking square.
         * @param piece    the type of the piece that is attacking. 
         * @param board    the board on which the attacking square resides.
         */
        public void pushInPlace(int attacker, int piece, Board board) {
            int valueOfAttacker = Value.getPieceValue(piece);
            int index;
            for (index = size - 1;
                 index >= 0 && Value.getPieceValue(Square.getPiece(board.squares[attackers[index]])) < valueOfAttacker;
                 index--) {
                attackers[index + 1] = attackers[index];
            }
            attackers[index + 1] = attacker;
            size++;
        }

        public int pop() {
            return attackers[--size];             
        }

        public void clear() {
            this.size = 0;
        }

        public int getSize() {
            return this.size;
        }

        public boolean isEmpty() {
            return this.size == 0;
        }

        public void print() {
            for (int a = 0; a < this.size; a++ ) {
                System.out.print(Square.getSquareName(attackers[a]) + " ");
            }
            System.out.println("");
        }

    }

    // Variables

    private AttackerStack[] stacks;
    private Board board;
    
    // Functions

    private SEE() {

        // Initializes stacks for both sides:
        stacks = new AttackerStack[NO_OF_COLORS];
        stacks[WHITE] = new AttackerStack();
        stacks[BLACK] = new AttackerStack();
    }

    public int see(int move, Board board) {

        // Initializes stuff:
        this.board = board;

        int attacker = board.sideToMove;
        int defender = Board.oppColor(attacker);
        int type = Move.getType(move);
        int from = Move.getFrom(move);
        int to = Move.getTo(move);
        int piece = Square.getPiece(board.squares[from]);

        // Promotion
        if (Move.isPromotion(type)) {
            piece = Move.getPromotion(move);
        }

        // Get the value of the initial capture
        int value = 0;

        if (Move.isNormalCapture(type)) {
            int victim = Square.getPiece(board.squares[to]);
            value += Value.getPieceValue(victim);
        } else if (Move.isEPCapture(type)) {
            value += Value.getPieceValue(PAWN);
        } else {
            assert false : "Should not happen!";
        }

        int valueOnSquare = Value.getPieceValue(piece);

        if (Move.isPromotion(type)) {
            valueOnSquare += Value.getPieceValue(QUEEN); /* Promotions are worth a queen */
        }

        // Builds the defender list.
        stacks[defender].clear();
        addAttackers(defender, to, from);
        if (stacks[defender].isEmpty()) {
            return value;
        }

        // Builds the attacker list.
        stacks[attacker].clear();
        addAttackers(attacker, to, from);

        // Add hidden attackers
        addHidden(attacker, to, from);

        // Simulate!
        int sideToMove = defender;

        while (true) {
            // Checks whether there are more attackers/defenders
            if (stacks[sideToMove].isEmpty()) {
                break;
            }

            // Gets the least valued attacker.
            int square = stacks[sideToMove].pop();
            int attPiece = Square.getPiece(board.squares[square]);

            // Adds the hidden attackers behind him (if any)
            addHidden(sideToMove, to, square);

            // Initializes the value for this attack:
            int thisAttackValue = valueOnSquare;

            // Checks for a promotion:
            if (attPiece == PAWN && Square.isPromote(square)) {
                attPiece = QUEEN;
            }

            if (sideToMove == attacker) {
                value += thisAttackValue;
            } else {
                value -= thisAttackValue;
            }

            valueOnSquare = Value.getPieceValue(attPiece);
            sideToMove = Board.oppColor(sideToMove);
        }

        return value;
    }

    private void addHidden(int color, int target, int square) {

        // TODO: Optimize, compact.
        int inc = Increment.getIncrement(square - target);

        int next;
        for (next = square - inc; Square.isEmpty(board.squares[next]); next -= inc) ;
        int sq = board.squares[next];

        if (Square.isBorder(sq)) {
            return;
        }

        int piece = Square.getPiece(board.squares[next]);

        if (piece == KING || piece == KNIGHT || piece == PAWN) {
            return;
        }
        
        if (Increment.getPieceIncrement(piece, next - target) != 0
                && Square.getColor(board.squares[next]) == color) {
            //stacks[color].print();
            stacks[color].pushInPlace(next, piece, board);
            //stacks[color].print();
        }

    }

    private void addAttackers(int color, int target, int excludedSquare) {

        // This function pushes the attackers found on top of the stack in MV (Move-value) order:
        // e.g. king -> queens -> rooks and so on so we do NOT have to worry about AttackerStacks
        // pushIntoPlace() until we begin to add hidden attackers. 

        int[] squares = board.getPiecesInMVOrder(color);
        int square;

        for (int index = 0; (square = squares[index]) != Square.NONE; index++) {

            if (square == excludedSquare) {
                continue;
            }

            int inc, next;
            int piece = Square.getPiece(board.squares[square]);

            if ((inc = Increment.getPieceIncrement(piece, square - target)) != 0) {
                for (next = square + inc; Square.isEmpty(board.squares[next]); next += inc) ;
                if (next == target) {
                    //System.out.printf("[added] piece: %s | square: %s (%d) | color: %s | inc: %d\n",
                    //        Piece.getPieceName(piece), Square.getSquareName(square), square, (color == WHITE ? "W" : "B"), inc);
                    stacks[color].push(square);
                }
            }

        }

        // Pawns

        int[] incs = (color == BLACK ? new int[]{NW, NE} : new int[]{SE, SW});
        for (int inc : incs) {
            square = target + inc;

            if (square == excludedSquare) {
                continue;
            }

            if (Square.isPieceAndColor(board.squares[square], PAWN, color)) {
                assert !Square.isBorder(board.squares[square]);
                //System.out.printf("[pawn ] piece: p | square: %s (%d) | color: %s\n", Square.getSquareName(square), square, (color == WHITE ? "W" : "B"));
                stacks[color].push(square);
            }
        }
    }

}