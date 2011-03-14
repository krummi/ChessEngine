package is.ru.cadia.ce;

public class MoveStackItem implements Comparable {
    public int move;
    public int score;

    public int compareTo(Object o) {
        int otherScore = ((MoveStackItem) o).score;
        if (this.score > otherScore) return 1;
        else if (this.score < otherScore) return -1;
        return 0;
    }

    @Override
    public String toString() {
        return "MoveStackItem{" +
                "move=" + move +
                ", score=" + score +
                '}';
    }
}
