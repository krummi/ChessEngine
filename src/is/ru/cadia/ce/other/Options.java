package is.ru.cadia.ce.other;

import java.util.HashMap;

public class Options {

    // Types

    class Option {
        public String key;
        public Object value;

        Option(String key, Object value) {
            this.key = key;
            this.value = value;
        }
    }

    // Constants and variables

    private static Options ourInstance = new Options();
    
    private HashMap<String, Option> optionsMap = new HashMap<String, Option>();

    // Functions

    public static Options getInstance() {
        return ourInstance;
    }

    private Options() {

        Option[] options = {
                new Option("Hash", (2 << 18)),
                new Option("OwnBook", true),
                new Option("Book File", "/home/krummi/Desktop/Chess-Engine/openings.txt")
        };

        // Puts the array into the map.
        for (Option option : options) {
            optionsMap.put(option.key, option);
        }
    }

    public int getOptionInt(String key) {
        return (Integer) optionsMap.get(key).value;
    }

    public String getOptionString(String key) {
        return (String) optionsMap.get(key).value;
    }

    public boolean getOptionBoolean(String key) {
        return (Boolean) optionsMap.get(key).value; 
    }

}
