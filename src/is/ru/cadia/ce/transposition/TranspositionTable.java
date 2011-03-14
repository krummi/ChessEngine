package is.ru.cadia.ce.transposition;

import is.ru.cadia.ce.Move;
import is.ru.cadia.ce.Search;
import is.ru.cadia.ce.board.Board;
import is.ru.cadia.ce.board.Evaluation;
import is.ru.cadia.ce.other.Constants;
import is.ru.cadia.ce.other.Options;

public class TranspositionTable implements Constants {

    // Types

    public class HashEntry {
        public long key;

        // TODO: FIX THE SIZE.

        public int type;
        public int depth;
        public int eval;

        public int move;

        HashEntry(long key, int type, int depth, int eval, int move) {
            this.key = key;
            this.type = type;
            this.depth = depth;
            this.eval = eval;
            this.move = move;
        }
    }

    // Variables

    public int size;
    public HashEntry[] table;

    // Functions

    public TranspositionTable() {
        size = Options.getInstance().getOptionInt("Hash");
    }

    public void initialize() {
        table = new HashEntry[size];
    }

    public void clear() {
        initialize();
    }

    public HashEntry get(long key) {

        int hashKey = (int) (key % size);
        HashEntry entry = table[hashKey];

        if (entry != null && entry.key == key) {
            return entry;
        }

        return null;
    }

    public void put(long key, int type, int depth, int eval, int move) {

        int hashKey = (int) (key % size);

        HashEntry entry = table[hashKey];

        if (entry == null) { // TODO: do we need to do all of this?
            table[hashKey] = new HashEntry(key, type, depth, eval, move);
        } else if (entry.depth <= depth) {
            table[hashKey] = new HashEntry(key, type, depth, eval, move);
        } else if (entry.key == key && entry.move == Move.MOVE_NONE) {
            entry.move = move;
        }
    }

    public void putLeaf(long key, int eval, int alpha, int beta) {

        if (eval >= beta) {
            put(key, Search.HASH_BETA, 0, eval, Move.MOVE_NONE);
        } else if (eval <= alpha) {
            put(key, Search.HASH_ALPHA, 0, eval, Move.MOVE_NONE);
        } else {
            put(key, Search.HASH_EXACT, 0, eval, Move.MOVE_NONE);
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
            if (DEBUG) {
                Evaluation.evaluate(board, true);
            }
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