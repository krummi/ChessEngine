package is.ru.cadia.ce.test;

import is.ru.cadia.ce.MoveSelector;
import is.ru.cadia.ce.MoveStackItem;
import is.ru.cadia.ce.transposition.TranspositionTable;

import java.util.Random;


public class UnitTest {

    public static void main(String[] args) {

        Random rand = new Random();

        TranspositionTable table = new TranspositionTable();
        for (int i = 0; i < table.size; i++) {
            table.put(i, rand.nextInt(), rand.nextInt(), rand.nextInt(), rand.nextInt());
        }
        while (true) {
            try {
            Thread.sleep(10000);
            } catch (InterruptedException ieox) {
                System.out.println("heheh");
            }
        }


    }


}
