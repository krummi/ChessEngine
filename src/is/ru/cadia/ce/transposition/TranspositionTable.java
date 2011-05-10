package is.ru.cadia.ce.transposition;

import is.ru.cadia.ce.Move;
import is.ru.cadia.ce.Search;
import is.ru.cadia.ce.board.Board;
import is.ru.cadia.ce.other.Constants;
import is.ru.cadia.ce.other.Options;

public class TranspositionTable implements Constants {

    // Type

    /**
     * An object of this type should take 8+4+4+4+4 (fields) + 8 (header) = 32 bytes. But the
     * JVM is pretty crazy.
     */
    public class HashEntry {
        public long key; // 8
        public int type; // 4
        public int depth; // 4
        public int eval; // 4
        public int move; // 4

        HashEntry(long key, int type, int depth, int eval, int move) {
            this.key = key;
            this.type = type;
            this.depth = depth;
            this.eval = eval;
            this.move = move;
        }
    }

    // Constants

    private final int NO_OF_TABLES = 2; // Has to be a multiple of 2.

    // Variables

    // TODO: fix the naming of things.

    public int size;
    public HashEntry[] table;
    public long alwaysReplaceOW = 0;
    public long depthOW = 0;

    // Functions

    public TranspositionTable() {
        size = Options.getInstance().getOptionInt("Hash") / NO_OF_TABLES;
        clear();
    }

    public void clear() {
        table = new HashEntry[size * NO_OF_TABLES];
    }

    public HashEntry get(long key) {

        int hashKey = (int) (key % size);

        for (int i = 0; i < NO_OF_TABLES; i++) {
            HashEntry entry = table[i * size + hashKey];
            if (entry != null && entry.key == key) {
                return entry;
            }
        }

        return null;
    }

    public void put(long key, int type, int depth, int eval, int move) {

        // The transposition table is divided into two sections; a depth one (0 .. (size/2)-1)
        // and a always-replace one (size ..

        int hashKey = (int) (key % size);
        HashEntry entry = table[hashKey];

        if (entry == null) {

            // The depth entry is empty.
            table[hashKey] = new HashEntry(key, type, depth, eval, move);

        } else if (entry.depth <= depth) {

            // The depth is lower; replace.
            table[hashKey] = new HashEntry(key, type, depth, eval, move);
            depthOW++;

        } else if (entry.key == key && entry.move == Move.MOVE_NONE) {

            // An entry was found with this key, but it did not contain any move.
            entry.move = move;

        } else {

            // Put the entry into the always-replace spot.
            table[hashKey + size] = new HashEntry(key, type, depth, eval, move);
            alwaysReplaceOW++;

        }
    }

    public void putLeaf(long key, int eval, int alpha, int beta) {

        if (eval >= beta) {
            put(key, Search.SCORE_CUT, 0, eval, Move.MOVE_NONE);
        } else if (eval <= alpha) {
            put(key, Search.SCORE_ALL, 0, eval, Move.MOVE_NONE);
        } else {
            put(key, Search.SCORE_EXACT, 0, eval, Move.MOVE_NONE);
        }

    }

    public int extractPV(Board board, int[] pv) {

        int a = 20, b = 0;
        HashEntry entry = get(board.key);

        while (a > 0) {
            if (entry == null || entry.move == Move.MOVE_NONE) {
                break;
            }
            pv[b] = entry.move;
            board.make(entry.move);

            entry = get(board.key);
            b++;
            a--;
        }

        for (int d = b - 1; d >= 0; d--) {
            board.retract(pv[d]);
        }

        return b;
    }

}