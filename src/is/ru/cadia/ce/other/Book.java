package is.ru.cadia.ce.other;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class Book {

    // Constants

    private static Random random = new Random();
    private static Book ourInstance = new Book();

    // Variables

    private ArrayList<String> lines = new ArrayList<String>();

    // Singleton functions

    public static Book getInstance() {
        return ourInstance;
    }

    private Book() {
    }

    // Functions

    public void parseBook(String fileName) throws IOException {

        BufferedReader reader = new BufferedReader(new FileReader(fileName));

        String line;
        while ((line = reader.readLine()) != null) {
            lines.add(line);
        }

    }

    public String getNextMove(String opening) {

        if (opening.equals("")) {
            return lines.get(random.nextInt(lines.size())).substring(0, 4);
        }

        ArrayList<String> available = new ArrayList<String>();
        for (String line : lines) {
            if (line.startsWith(opening) && !line.equals(opening)) {
                available.add(line);
            }
        }
        if( available.isEmpty() ) {
            return null;
        }

        String line = available.get(random.nextInt(available.size()));
        int index;
        for (index = 0; index < opening.length() && line.charAt(index) == opening.charAt(index) ; index++) ;
        return line.substring(index, index + 4);
    }


}
