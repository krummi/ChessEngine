package is.ru.cadia.ce.test;

import is.ru.cadia.ce.MoveSelector;
import is.ru.cadia.ce.MoveStackItem;


public class UnitTest {

    public static void main(String[] args) {


        MoveStackItem item = new MoveStackItem();
        item.move = 1;
        item.score = 10;
        MoveStackItem item2 = new MoveStackItem();
        item2.move = 2;
        item2.score = 50;
        MoveStackItem item3 = new MoveStackItem();
        item3.move = 3;
        item3.score = 30;
        MoveStackItem item4 = new MoveStackItem();
        item4.move = 4;
        item4.score = 40;
        MoveStackItem item5 = new MoveStackItem();
        item5.move = 5;
        item5.score = 20;

        MoveStackItem[] stack = new MoveStackItem[]{item, item2, item3, item4, item5};

        MoveSelector.insertionSort(stack, 1, 4);

        System.out.println("");


    }


}
