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
                new Option("Book File", "openings.txt"),

                new Option("Do MultiCut", true),
                new Option("MC Cutoffs", 3),
                new Option("MC Expand", 10),
                new Option("MC Reduction", 2),
                new Option("MC Piece", false),

                new Option("Do NullMove", true),
                new Option("NM Reduction", 2),

                new Option("Do LMR", true),
                new Option("LMR FullDepthMoves", 4),

                new Option("Aspiration Size", 80)
        };

        // Puts the array into the map.
        for (Option option : options) {
            optionsMap.put(option.key, option);
        }
    }

    public void parseOptions(HashMap<String, String> params) {

        Options options = Options.getInstance();

        // Multi-Cut
        if (params.containsKey("mc") && params.get("mc").equals("off")) {
            options.setOption("Do MultiCut", false);
        }
        if (params.containsKey("mc_cutoffs")) {

            options.setOption("MC Cutoffs", params.get("mc_cutoffs"));
        }
        if (params.containsKey("mc_expand")) {
            options.setOption("MC Expand", params.get("mc_expand"));
        }
        if (params.containsKey("mc_reduction")) {
            options.setOption("MC Reduction", params.get("mc_reduction"));
        }
        if (params.containsKey("mc_piece")) {
            options.setOption("MC Piece", params.get("mc_piece").equals("on"));
        }

        // Null moves
        if (params.containsKey("nm") && params.get("nm").equals("off")) {
            options.setOption("Do NullMove", false);
        }
        if (params.containsKey("nm_reduction")) {
            options.setOption("NM Reduction", params.get("nm_reduction"));
        }

        // Late-Move Reductions
        if (params.containsKey("lmr") && params.get("lmr").equals("off")) {
            options.setOption("Do LMR", false);
        }
        if (params.containsKey("lmr_fdm")) {
            options.setOption("LMR FullDepthMoves", params.get("lmr_fdm"));
        }

        // Aspiration size
        if (params.containsKey("aspiration")) {
            options.setOption("Aspiration Size", params.get("aspiration"));
        }
    }

    public int getOptionInt(String key) {
        assert optionsMap.containsKey(key);

        if (optionsMap.get(key).value instanceof String) {
            return Integer.parseInt((String) optionsMap.get(key).value);
        }

        return (Integer) optionsMap.get(key).value;
    }

    public String getOptionString(String key) {
        assert optionsMap.containsKey(key);

        return (String) optionsMap.get(key).value;
    }

    public boolean getOptionBoolean(String key) {
        assert optionsMap.containsKey(key);

        return (Boolean) optionsMap.get(key).value;
    }

    public void setOption(String key, Object value) {
        assert optionsMap.containsKey(key);

        Option option = optionsMap.get(key);
        option.value = value;
        optionsMap.put(key, option);
    }

}
