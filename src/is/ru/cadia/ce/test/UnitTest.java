package is.ru.cadia.ce.test;

import java.util.Random;


public class UnitTest {

    public static void main(String[] args) {

        Random rand = new Random();

        NewTranspositionTable table = new NewTranspositionTable();
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
