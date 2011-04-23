package is.ru.cadia.ce.transposition;

import java.io.Serializable;

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