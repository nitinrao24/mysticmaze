package core;

import java.io.File;
import java.io.PrintWriter;
import java.util.Scanner;

public class SaveManager {
    // Saves and loads seed, movement history, and feature state.
    private static final String SAVE_FILE = "save.txt";

    public void save(GameState state) {
        try {
            PrintWriter writer = new PrintWriter(SAVE_FILE);

            writer.println(state.seed); // save seed so we can rebuild the same world
            writer.println(state.moves); // save moves so avatar position can be replayed
            writer.println(state.featureState.serialize()); // save future feature data

            writer.close();
        } catch (Exception e) {
            throw new RuntimeException("Save failed.");
        }
    }

    public SavedGame load() {
        File file = new File(SAVE_FILE);

        if (!file.exists()) {  // if file doesn't exist already
            return null;  // the null will be read by loadGame; doesn't crash the game.
        }

        try {
            Scanner scanner = new Scanner(new File(SAVE_FILE));

            long seed = Long.parseLong(scanner.nextLine()); // first line is the seed
            String moves = scanner.hasNextLine() ? scanner.nextLine() : ""; // second line is move history

            FeatureState fs = scanner.hasNextLine()
                    ? FeatureState.deserialize(scanner.nextLine()) // third line is feature state
                    : new FeatureState(); // default feature state if not found

            scanner.close();

            return new SavedGame(seed, moves, fs); // package loaded data together
        } catch (Exception e) {
            throw new RuntimeException("Load failed.");
        }
    }

    public static class SavedGame {
        public long seed;
        public String moves;
        public FeatureState featureState;

        public SavedGame(long seed, String moves, FeatureState fs) {
            this.seed = seed;
            this.moves = moves;
            this.featureState = fs;
        }
    }
}
