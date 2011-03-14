package is.ru.cadia.ce;

public class MoveStack {

    // Variables

    private int size;
    public MoveStackItem[] moves;

    // Functions

    public MoveStack(int capacity) {
        this.moves = new MoveStackItem[capacity];
        for (int i = 0; i < capacity; i++) moves[i] = new MoveStackItem();
    }

    public MoveStackItem pop() {
        return moves[--size];
    }

    public MoveStackItem get(int index) {
        return moves[index];
    }

    public void put(int move) {
        moves[size++].move = move;
    }

    public void setSize(int size) {
        this.size = size;     
    }

    public int getSize() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }
    
}
