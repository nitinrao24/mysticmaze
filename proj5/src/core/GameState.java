package core;

import java.util.ArrayList;

import tileengine.TETile;

public class GameState {
    // Stores everything needed to represent the current game.
    public long seed;
    public TETile[][] world;
    public Position avatarPos;
    public String moves;
    public ArrayList<Position> currentPath;
    public Position currentGoal;
    public FeatureState featureState;
    public ArrayList<Position> coins;

    public GameState(long seed, TETile[][] world, Position avatarPos) {
        this.seed = seed; // seed used to recreate the same world
        this.world = world; // stores the base world tiles
        this.avatarPos = avatarPos; // stores where the avatar currently is
        coins = new ArrayList<>();

        moves = ""; // stores successful moves for save/load
        currentPath = new ArrayList<>(); // stores path preview from mouse click
        currentGoal = null; // no clicked goal at the start

        featureState = new FeatureState(); // placeholder for future ambition features
    }
}
