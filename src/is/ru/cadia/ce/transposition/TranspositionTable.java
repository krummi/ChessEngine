package is.ru.cadia.ce.transposition;

import is.ru.cadia.ce.Move;
import is.ru.cadia.ce.Search;
import is.ru.cadia.ce.board.Board;
import is.ru.cadia.ce.other.Constants;
import is.ru.cadia.ce.other.Options;

import java.io.*;

public class TranspositionTable implements Constants {

    // Type

    public class HashEntry implements Serializable {
        public long key;
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
    public long overwrites = 0;

    // Functions

    public TranspositionTable() {
        size = Options.getInstance().getOptionInt("Hash");
        clear();
    }

    public void clear() {
        table = new HashEntry[size];
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

        // ALWAYS REPLACE SCHEME
        if (entry == null) {
            table[hashKey] = new HashEntry(key, type, depth, eval, move);
        } else if (entry.key == key && entry.move == Move.MOVE_NONE) {
            entry.move = move;
        } else {
            overwrites++;
            table[hashKey] = new HashEntry(key, type, depth, eval, move);
        }

        // DEPTH PREFERRED --- ERRONEOUS
        /*if (entry == null) {
            table[hashKey] = new HashEntry(key, type, depth, eval, move);
        } else if (entry.depth <= depth) {
            table[hashKey] = new HashEntry(key, type, depth, eval, move);
        } else if (entry.key == key && entry.move == Move.MOVE_NONE) {
            entry.move = move;
        }     */
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

            entry = get(board.key);
            b++;
            a--;
        }

        for (int d = b - 1; d >= 0; d--) {
            board.retract(pv[d]);
        }

        return b;
    }

    public void serialize(String fileName) {
        try {
            FileOutputStream fileOut = new FileOutputStream(fileName + ".ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(table);
            out.close();
            fileOut.close();
        } catch (IOException i) {
            System.out.println(i.getMessage());
            i.printStackTrace();
        }
    }

    public void deserialize(String fileName) {
        table = null;
        try {
            FileInputStream fileIn = new FileInputStream(fileName + ".ser");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            table = (HashEntry[]) in.readObject();
            in.close();
            fileIn.close();
        } catch (IOException i) {
            i.printStackTrace();
        } catch (ClassNotFoundException c) {
            System.out.println("Some class not found....?");
            c.printStackTrace();
        }
    }

}