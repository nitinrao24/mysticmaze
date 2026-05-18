package core;

public class FeatureState {
    public int score;
    public int health;
    public int coinsCollected;
    public int totalCoins;

    public FeatureState() {
        score = 0;
        health = 5;
        coinsCollected = 0;
        totalCoins = 0;
    }

    public String serialize() {
        return score + "," + health + "," + coinsCollected + "," + totalCoins;
    }

    public static FeatureState deserialize(String line) {
        FeatureState fs = new FeatureState();

        if (line == null || line.length() == 0) {
            return fs;
        }

        String[] parts = line.split(",");

        fs.score = Integer.parseInt(parts[0]);
        fs.health = Integer.parseInt(parts[1]);

        if (parts.length >= 4) {
            fs.coinsCollected = Integer.parseInt(parts[2]);
            fs.totalCoins = Integer.parseInt(parts[3]);
        }

        return fs;
    }
}
