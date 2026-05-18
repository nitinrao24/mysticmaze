package core;

import edu.princeton.cs.algs4.StdDraw;
import tileengine.TERenderer;
import tileengine.TETile;
import tileengine.Tileset;

import java.awt.*;
import java.lang.reflect.Array;
import java.net.URI;
import java.util.ArrayList;
import java.util.Random;

public class Game {
    // Handles menu, seed input, game loop, rendering, movement, HUD, and mouse clicks.
    private static final int WIDTH = 60;
    private static final int HEIGHT = 35;
    private static final int HUD_HEIGHT = 3;
    private static final int NUM_COINS = 10; // number of coins placed in each world
    private static final long DIALOGUE_LINE_DELAY = 2000; // delay between each dialogue line

    // NPC
    private Position npcPos;
    private boolean inDialogue;
    private int dialogueStage;
    private long dialogueStageStartTime; // timer
    private boolean rickRollOpened;


    private TERenderer renderer;
    private SaveManager saveManager;
    private Pathfinder pathfinder;

    private GameState state;
    private boolean waitingForQAfterColon; // used to detect :Q

    public Game() {
        renderer = new TERenderer();
        saveManager = new SaveManager();
        pathfinder = new Pathfinder();
        waitingForQAfterColon = false;
    }

    public void run() {
        renderer.initialize(WIDTH, HEIGHT + HUD_HEIGHT);

        // TO BE DONE BY LEO
        // Task 1: main menu goes here
        // Leo should call startNewGame(seed), loadGame(), or System.exit(0)
        mainMenu();

        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char key = Character.toLowerCase(StdDraw.nextKeyTyped());

                if (key == 'n') {
                    seedInput();
                } else if (key == 'l') {
                    loadGame();
                    mainMenu(); // redraw menu only if load fails and returns
                } else if (key == 'q') {
                    System.exit(0);
                }
            }

            StdDraw.pause(20);
        }
    }

    // this helper function displays the main menu
    private void mainMenu() {
        // show the menu

        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);

        StdDraw.setFont(new Font("Times New Roman", Font.BOLD, 67));
        StdDraw.text(30, 25, "The Ultimate Game");

        StdDraw.setFont(new Font("Times New Roman", Font.BOLD, 28));
        StdDraw.text(30, 22, "Made with Love by Leo Du & Nitin Rao :)");

        StdDraw.setFont(new Font("Times New Roman", Font.PLAIN, 22));
        StdDraw.text(30, 15, "New Game (N)");
        StdDraw.text(30, 13, "Load Game (L)");
        StdDraw.text(30, 11, "Quit (Q)");

        StdDraw.show();

    }

    private void seedInput() {

        String seed = "";

        drawSeedScreen(seed); // draw once before the loop

        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char key = Character.toLowerCase(StdDraw.nextKeyTyped());

                if (Character.isDigit(key)) {
                    seed += key;
                    drawSeedScreen(seed); // only redraw when seed changes
                } else if (key == 's' && !seed.isEmpty()) {
                    long seedNumber = Long.parseLong(seed);
                    startNewGame(seedNumber);
                    return;
                }
            }

            StdDraw.pause(20);
        }
    }

    private void drawSeedScreen(String seed) {  // draws the actual seed screen
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);

        StdDraw.setFont(new Font("Times New Roman", Font.BOLD, 67));
        StdDraw.text(30, 25, "The Ultimate Game");

        StdDraw.setFont(new Font("Times New Roman", Font.PLAIN, 30));
        StdDraw.text(30, 20, "Enter seed followed by S");

        StdDraw.setPenColor(Color.YELLOW);
        StdDraw.setFont(new Font("Times New Roman", Font.PLAIN, 25));
        StdDraw.text(30, 17, seed);

        StdDraw.show();

    }

    private void startNewGame(long seed) {
        World worldGen = new World(WIDTH, HEIGHT, seed); // make world generator
        TETile[][] world = worldGen.generateWorld(); // generate world from seed

        Position start = findStart(world); // deterministic avatar start
        state = new GameState(seed, world, start); // store all game data

        initializePrimaryFeature();
        initializeSecondaryFeature(); // sets up coins for this world

        gameLoop();
    }

    private void showNoSavePopup() {
        while (true) {
            drawNoSavePopup();

            if (StdDraw.isMousePressed()) {  // if mouse is pressed
                double mouseX = StdDraw.mouseX();  // mouse x-axis
                double mouseY = StdDraw.mouseY();  // mouse y-axis

                if (clickedXButton(mouseX, mouseY)) {  // check the mouse press coord.
                    waitForMouseRelease();
                    return;
                }
            }

            StdDraw.pause(20);
        }

    }   private void drawNoSavePopup() {
        StdDraw.clear(Color.BLACK);

        // Draw background menu
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.setFont(new Font("Times New Roman", Font.BOLD, 50));
        StdDraw.text(30, 28, "The Ultimate Game");

        // Popup box
        StdDraw.setPenColor(Color.DARK_GRAY);
        StdDraw.filledRectangle(30, 18, 18, 6);

        StdDraw.setPenColor(Color.WHITE);
        StdDraw.rectangle(30, 18, 18, 6);

        // Message
        StdDraw.setFont(new Font("Times New Roman", Font.PLAIN, 24));
        StdDraw.text(30, 18, "No saved files yet.");

        // X button in top-right of popup
        StdDraw.setPenColor(Color.RED);
        StdDraw.filledRectangle(30 + 18, 23, 1, 1);

        StdDraw.setPenColor(Color.WHITE);
        StdDraw.setFont(new Font("Times New Roman", Font.BOLD, 20));
        StdDraw.text(30 + 18, 23, "X");

        StdDraw.show();
    }

    private boolean clickedXButton(double mouseX, double mouseY) {
        double xCenter = 30 + 18;
        double yCenter = 23;

        return mouseX > xCenter - 5 && mouseX <= xCenter + 5
                && mouseY >= yCenter - 5 && mouseY <= yCenter + 5;
    }

    private void waitForMouseRelease() { // add time between each click
        while (StdDraw.isMousePressed()) {
            StdDraw.pause(10);
        }
    }

    private void loadGame() {

        SaveManager.SavedGame saved = saveManager.load(); // read saved data

        if (saved == null) {  // if there is no saved file yet
            showNoSavePopup();

        } else {  // if there is a saved file
            World worldGen = new World(WIDTH, HEIGHT, saved.seed); // rebuild same world
            TETile[][] world = worldGen.generateWorld();

            state = new GameState(saved.seed, world, findStart(world));

            initializePrimaryFeature();
            initializeSecondaryFeature(); // place original coins before replaying moves

            for (char c : saved.moves.toCharArray()) {
                moveAvatar(c, false); // replay moves without saving them again
            }

            state.moves = saved.moves; // restore exact move string
            state.featureState = saved.featureState; // restore saved coin counters

            loadSecondaryFeature();
            loadPrimaryFeature();

            gameLoop();
        }
    }

    private void gameLoop() {
        while (true) {
            handleKeyboard();
            handleMouse();

            updatePrimaryFeature();
            updateSecondaryFeature();

            render();

            StdDraw.pause(20); // small pause so the loop does not run too fast
        }
    }

    private void handleKeyboard() {
        while (StdDraw.hasNextKeyTyped()) {
            char key = Character.toLowerCase(StdDraw.nextKeyTyped());

            if (inDialogue) {
                handleDialogueInput(key);
                return;
            }

            if (waitingForQAfterColon) {
                if (key == 'q') {
                    savePrimaryFeature();
                    saveSecondaryFeature();
                    saveManager.save(state); // save before quitting
                    System.exit(0);
                }

                waitingForQAfterColon = false; // any non-q key cancels :Q
            }

            if (key == ':') {
                waitingForQAfterColon = true; // next key might be q
            } else if ("wasd".indexOf(key) >= 0) {
                moveAvatar(key, true); // try to move avatar
                state.currentPath.clear(); // old path no longer matters
                state.currentGoal = null;
            } else if (key == 'e') {  // e for dialogue with npc
                if (isNextToNPC()) {
                    inDialogue = true;
                    dialogueStage = 0; // reset dialogue stage to menu
                    dialogueStageStartTime = System.currentTimeMillis(); // start timer
                }
            }
        }
    }

    private void closeDialogue() {
        inDialogue = false;
        dialogueStage = 0;
        dialogueStageStartTime = 0;
        rickRollOpened = false;
    }

    private void handleDialogueInput(char key) {
        if (dialogueStage == 0) {
            if (key == '1') {
                dialogueStage = 1;
                dialogueStageStartTime = System.currentTimeMillis(); // reset time after pressing key
            } else if (key == '2') {
                dialogueStage = 2;
                dialogueStageStartTime = System.currentTimeMillis(); // reset time after pressing key
            } else if (key == '3') {
                dialogueStage = 3;
                dialogueStageStartTime = System.currentTimeMillis(); // reset time after pressing key
                openWDTFSVideo();
            } else if (key == '4' || key == 'q') {
                closeDialogue();
            }
        } else if (dialogueStage == 1) {
            if (key == '1') {
                dialogueStage = 4;
                dialogueStageStartTime = System.currentTimeMillis();
                rickRollOpened = false;
            } else if (key == 'e') {
                dialogueStage = 0;
                dialogueStageStartTime = System.currentTimeMillis();
            } else if (key == 'q') {
                closeDialogue();
            }
        } else {
            if (key == 'e') {
                dialogueStage = 0; // go back to choice menu
            } else if (key == 'q') {
                closeDialogue();
            }
        }

    }

    private void openWDTFSVideo() {
        try {
            Desktop.getDesktop().browse(new URI("https://www.youtube.com/watch?v=jofNR_WkoCE"));
        } catch (Exception e) {
            System.out.println("Could not open video.");
        }
    }

    private void openRickRollVideo() {
        try {
            Desktop.getDesktop().browse(new URI("https://www.youtube.com/watch?v=AuKR2fQbMBk"));
        } catch (Exception e) {
            System.out.println("Could not open video.");
        }
    }

    private void moveAvatar(char key, boolean record) {
        int dx = 0;
        int dy = 0;

        if (key == 'w') {
            dy = 1; // move up
        } else if (key == 's') {
            dy = -1; // move down
        } else if (key == 'a') {
            dx = -1; // move left
        } else if (key == 'd') {
            dx = 1; // move right
        }

        Position next = state.avatarPos.shift(dx, dy); // where avatar wants to go

        if (canMove(next)) {
            state.avatarPos = next; // only move if tile is valid

            if (record) {
                state.moves += key; // record successful move for save/load
            }

            collectCoinIfPresent(); // collect coin if the avatar stepped on one

            onAvatarMovedPrimaryFeature();
            onAvatarMovedSecondaryFeature();
        }
    }

    private boolean canMove(Position p) {
        return p.x >= 0 && p.x < WIDTH
                && p.y >= 0 && p.y < HEIGHT
                && state.world[p.x][p.y].equals(Tileset.FLOOR) // only floors are walkable
                && !p.equals(npcPos);  // this feature makes NPC act like a wall so players can't go through
    }

    private void handleMouse() {
        if (!StdDraw.isMousePressed()) {
            return; // no click to handle
        }

        int mouseX = (int) StdDraw.mouseX();
        int mouseY = (int) StdDraw.mouseY();

        if (mouseX < 0 || mouseX >= WIDTH || mouseY < 0 || mouseY >= HEIGHT) {
            waitForMouseRelease(); // avoid repeated bad clicks
            return; // ignore clicks outside the world
        }

        Position clicked = new Position(mouseX, mouseY);

        if (state.currentGoal != null
                && state.currentGoal.equals(clicked)
                && !state.currentPath.isEmpty()) {
            animatePath(); // second click on same valid tile moves avatar
        } else {
            ArrayList<Position> path = pathfinder.shortestPath(state.world, state.avatarPos, clicked);

            if (!path.isEmpty()) {
                state.currentPath = path; // store path so render can show it
                state.currentGoal = clicked; // remember clicked tile for second click
                animatePath();
            } else {
                state.currentPath.clear(); // no valid path, so show nothing
                state.currentGoal = null;
            }
        }

        waitForMouseRelease(); // prevents one click from registering many times
    }

    private void animatePath() {
        ArrayList<Position> pathCopy = new ArrayList<>(state.currentPath);

        for (Position nextStep : pathCopy) {
            char moveChar = directionFromTo(state.avatarPos, nextStep);

            state.avatarPos = nextStep; // actually move avatar to next path tile
            state.moves += moveChar; // save movement for load/replay

            collectCoinIfPresent(); // collect coins during path movement too

            onAvatarMovedPrimaryFeature();
            onAvatarMovedSecondaryFeature();

            render();
            StdDraw.pause(80); // slow enough to visibly animate
        }

        state.currentPath.clear(); // remove path after movement finishes
        state.currentGoal = null;
    }

    private char directionFromTo(Position from, Position to) {
        if (to.x == from.x && to.y == from.y + 1) {
            return 'w';
        } else if (to.x == from.x && to.y == from.y - 1) {
            return 's';
        } else if (to.x == from.x - 1 && to.y == from.y) {
            return 'a';
        } else if (to.x == from.x + 1 && to.y == from.y) {
            return 'd';
        }

        return ' '; // fallback should never happen for a valid BFS path
    }

    private void render() {
        TETile[][] display = copy(state.world);

        renderSecondaryFeature(display);

        for (Position p : state.currentPath) {
            display[p.x][p.y] = Tileset.GRASS;
        }

        display[state.avatarPos.x][state.avatarPos.y] = Tileset.AVATAR;

        renderPrimaryFeature(display);

        renderer.drawTiles(display);
        renderHUD();
        renderDialogueBox();

        StdDraw.show();
    }

    // ==========================================
    // Draws out each line of the NPC's dialogue
    // ==========================================

    // with elapsed time
    private void drawDelayedLines(long elapsed, double x, double startY, double gap, String[] lines) {
        for (int i = 0; i < lines.length; i++) {  // loops through the lines in String[] lines of each dialogue stage
            if (elapsed >= i * DIALOGUE_LINE_DELAY) {
                StdDraw.textLeft(x, startY - i * gap, lines[i]);  // calculate position of each line
            }
        }
    }

    // instant draw
    private void drawLines(double x, double startY, double gap, String[] lines) {
        for (int i = 0; i < lines.length; i++) {
            StdDraw.textLeft(x, startY - i * gap, lines[i]);  // calculate position of each line
        }
    }

    // ============================================
    //                END OF SECTION
    // ============================================


    private void renderDialogueBox() {
        if (!inDialogue) {
            return;
        }

        // Dialogue box background
        StdDraw.setPenColor(Color.BLACK);
        StdDraw.filledRectangle(WIDTH / 2.0, 6, WIDTH / 2.0 - 3, 5);

        // Dialogue box border
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.rectangle(WIDTH / 2.0, 6, WIDTH / 2.0 - 3, 5);

        // Speaker name
        StdDraw.setFont(new Font("Times New Roman", Font.BOLD, 18));
        StdDraw.textLeft(5, 9.5, "Mysterious NPC:");

        // Dialogue text
        StdDraw.setFont(new Font("Times New Roman", Font.PLAIN, 16));

        long elapsed = System.currentTimeMillis() - dialogueStageStartTime;  // cue time for the next dialogue line

        String[] lines;

        if (dialogueStage == 0) {
            lines = new String[]{
                    "Hello traveler. What do you want to ask?",
                    "1. Who are you?",
                    "2. Any life advice?",
                    "3. What does the fox say?",
                    "4. Goodbye."
            };
            drawLines(5, 8.0, 1.3, lines);

        } else if (dialogueStage == 1) {
            lines = new String[]{
                    "Ur mom.",
                    "Just kidding. I'm just a random guy in this game waiting for people to talk to me.",
                    "",
                    "1. Wow, that was not funny >:("
            };
            drawDelayedLines(elapsed, 5, 8.0, 1.3, lines);

        } else if (dialogueStage == 2) {
            lines = new String[]{
                    "Set your alarm to a motivational speech,",
                    "so you wake up confused and inspired."
            };
            drawDelayedLines(elapsed, 5, 8.0, 1.3, lines);

        } else if (dialogueStage == 3) {
            lines = new String[] {
                    "Ring-ding-ding-ding-dingeringeding!",
                    "Or maybe... it says nothing. The fox keeps its secrets ;)"
            };
            drawDelayedLines(elapsed, 5, 8.0, 1.3, lines);

        } else if (dialogueStage == 4) {
            lines = new String[] {
                    "Oh... my bad bro.",
                    "I have something for you though..."
            };
            drawDelayedLines(elapsed, 5, 8.0, 1.3, lines);

            if (elapsed >= 5000 && !rickRollOpened) {
                rickRollOpened = true;
                openRickRollVideo();
        }

    }
        }

    private void renderHUD() {
        // TO BE DONE BY LEO
        // Task 3: show tile description under mouse


        double x = StdDraw.mouseX();
        double y = StdDraw.mouseY();

        String HUDtext = "Tile: ";

        if (x < WIDTH && x >= 0 && y < HEIGHT && y >= 0) {
            TETile tile = state.world[(int) x][(int) y];
            HUDtext = HUDtext + tile.description();

            // check if mouse is on avatar
            if ((int) x == state.avatarPos.x && (int) y == state.avatarPos.y) {
                HUDtext = "Tile: avatar";
            }

        }

        StdDraw.setPenColor(Color.BLACK);
        StdDraw.filledRectangle(WIDTH / 2.0, HEIGHT + 1.5, WIDTH / 2.0, HUD_HEIGHT / 2.0);

        StdDraw.setPenColor(Color.WHITE);
        StdDraw.setFont(new Font("Times New Roman", Font.BOLD, 15));
        StdDraw.textLeft(1, HEIGHT + 1, HUDtext);
        StdDraw.textRight(WIDTH - 1, HEIGHT + 1,
                "Coins: " + state.featureState.coinsCollected + "/" + state.featureState.totalCoins);

    }

    private TETile[][] copy(TETile[][] world) {
        TETile[][] copy = new TETile[world.length][world[0].length];

        for (int x = 0; x < world.length; x++) {
            for (int y = 0; y < world[0].length; y++) {
                copy[x][y] = world[x][y];
            }
        }

        return copy;
    }

    private Position findStart(TETile[][] world) {
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                if (world[x][y].equals(Tileset.FLOOR)) {
                    return new Position(x, y); // first floor tile found
                }
            }
        }

        throw new RuntimeException("No floor found for avatar start.");
    }





    // =================================================================
    //
    //                               TASK 6
    //
    // =================================================================







    private void initializePrimaryFeature() {
        // Task 6: primary ambition feature setup goes here

        int floorCount = 0;

        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                Position p = new Position(x, y);

                // check the tile is a floor and not the avatar's position
                if (isGoodNPCSpot(p)) {
                    floorCount++;
                }
            }
        }

        if (floorCount == 0) {
            throw new RuntimeException("No floor is found for the avatar to start.");
        }
        // generate a random number within floorCount for the NPC spawn point
        Random r = new Random(state.seed);
        int npcSpawnpoint = r.nextInt(floorCount);

        Position chosen = null;
        int seen = 0;

        for (int y = 0; y < HEIGHT && chosen == null; y++) {
            for (int x = 0; x < WIDTH && chosen == null; x++) {
                Position p = new Position(x, y);  // create object p for position

                // check the tile is a floor and not the avatar's position
                if (isGoodNPCSpot(p)) {
                    if (seen == npcSpawnpoint) {
                        chosen = p;
                    }
                    seen++;
                }
            }
        }

        // Store the NPC feature state
        npcPos = chosen;
        inDialogue = false;
    }

    private boolean isFloor(int x, int y) {  // check whether the position is a floor tile
        return x >= 0 && x < WIDTH
                && y >= 0 && y < HEIGHT
                && state.world[x][y].equals(Tileset.FLOOR);
    }
    private boolean isGoodNPCSpot(Position p) {
        if (!isFloor(p.x, p.y)) {  // if it is not floor
            return false;
        }

        if (p.equals(state.avatarPos)) {  // if it is where the player is at
            return false;
        }

        if (state.coins.contains(p)) {  // if it is where a coin is at
            return false;
        }

        return isFloor(p.x + 1, p.y)
                && isFloor(p.x - 1, p.y)
                && isFloor(p.x, p.y + 1)
                && isFloor(p.x, p.y - 1)
                && isFloor(p.x + 1, p.y + 1)
                && isFloor(p.x + 1, p.y - 1)
                && isFloor(p.x - 1, p.y + 1)
                && isFloor(p.x - 1, p.y - 1);
    }

    private void initializeSecondaryFeature() {
        placeCoins(); // secondary feature: place coins in the current world
    }

    private void updatePrimaryFeature() {
        // Task 6: primary feature updates go here
    }

    private void updateSecondaryFeature() {
        // Coins do not need to update every frame.
    }

    private void onAvatarMovedPrimaryFeature() {
        // Task 6: primary feature reacts to avatar movement here
    }

    private void onAvatarMovedSecondaryFeature() {
        // Coin collection happens directly in moveAvatar and animatePath.
    }

    private void renderPrimaryFeature(TETile[][] display) {
        // Task 6: draw primary feature here

        if (npcPos != null) {
            display[npcPos.x][npcPos.y] = Tileset.MOUNTAIN;
        }

    }

    private void renderSecondaryFeature(TETile[][] display) {
        for (Position coin : state.coins) {
            display[coin.x][coin.y] = Tileset.FLOWER; // coins appear as flowers
        }
    }

    private boolean isNextToNPC() {
        if (npcPos == null) {
            return false;
        }

        int dx = Math.abs(state.avatarPos.x - npcPos.x);
        int dy = Math.abs(state.avatarPos.y - npcPos.y);

        return dx + dy == 1 || (dx == 1 && dy == 1);

    }

    private void savePrimaryFeature() {
        // Task 6: prepare primary feature data before save
    }

    private void saveSecondaryFeature() {
        // Coin progress is saved through moves + FeatureState.
    }

    private void loadPrimaryFeature() {
        // Task 6: restore primary feature after load
    }

    private void loadSecondaryFeature() {
        // Coins are restored by deterministic placement and replayed moves.
    }

    private void placeCoins() {
        state.coins.clear(); // clear old coins before placing new ones

        Random coinRandom = new Random(state.seed + 9999); // same seed means same coin locations

        while (state.coins.size() < NUM_COINS) {
            int x = coinRandom.nextInt(WIDTH);
            int y = coinRandom.nextInt(HEIGHT);

            Position possibleCoin = new Position(x, y);

            if (state.world[x][y].equals(Tileset.FLOOR)
                    && !possibleCoin.equals(state.avatarPos)
                    && !state.coins.contains(possibleCoin)) {
                state.coins.add(possibleCoin); // only place coins on valid floor tiles
            }
        }

        state.featureState.totalCoins = NUM_COINS;
        state.featureState.coinsCollected = 0;
    }

    private void collectCoinIfPresent() {
        for (int i = 0; i < state.coins.size(); i++) {
            Position coin = state.coins.get(i);

            if (state.avatarPos.equals(coin)) {
                state.coins.remove(i); // remove collected coin from the world
                state.featureState.coinsCollected++; // update progress count

                if (state.featureState.coinsCollected == state.featureState.totalCoins) {
                    allCoinsCollected();
                }

                return;
            }
        }
    }

    private void allCoinsCollected() {
        // Returning to the menu matches the coin feature requirement.
        run();
    }

}
