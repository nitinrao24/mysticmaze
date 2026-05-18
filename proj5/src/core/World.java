package core;

import tileengine.TETile;
import tileengine.Tileset;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class World {

    // build your own world!

    private final int width;
    private final int height;
    private final long seed;
    private final TETile[][] world;
    private final ArrayList<Room> rooms;
    private final Random rand;

    public World(int width, int height, long seed) {
        this.width = width;
        this.height = height;
        this.seed = seed;
        this.world = new TETile[width][height];
        this.rooms = new ArrayList<>();
        this.rand = new Random(seed);
    }

    public TETile[][] generateWorld() {
        fillwithNothing(); // fill everything with empty tiles
        generateRooms(); // generate rooms 
        connectRooms(); // connect rooms with hallways
        addMoreConnections(); // add extra connections between rooms for variety and more interesting layouts
        addWalls(); // add walls around all the floor tiles (rooms and hallways)
        return world;
    }

    // The following method to be done by Leo to fill the world with nothing tiles

    private void fillwithNothing() {
        for (int yAxis = 0; yAxis < height; yAxis++) {  // Nested for loop.
            for (int xAxis = 0; xAxis < width; xAxis++) {  // Goes through each x and y coordinate and fill with NOTHING.
                world[xAxis][yAxis] = Tileset.NOTHING;
            }
        }
    }

    // The following method generates rooms randomly

    private void generateRooms() {
        int targetRooms = randomInt(16, 25); // target number of rooms to generate
        int attempts = 0; // number of attempts to place rooms
        int maxAttempts = targetRooms * 20; // maximum number of attempts to place rooms to avoid infinite loops
        while (attempts < maxAttempts && rooms.size() < targetRooms) { // try to place rooms until we reach the target or exceed max attempts
            attempts++;
            int roomWidth = randomInt(5, 10); // random room size
            int roomHeight = randomInt(5, 9); // random room size
            int x = randomInt(1, width - roomWidth - 1); // random x coordinate for the room, ensuring it fits within the world boundaries
            int y = randomInt(1, height - roomHeight - 1); // random y coordinate for the room, ensuring it fits within the world boundaries
            Room candidateRoom = new Room(x, y, roomWidth, roomHeight);
            if (canPlaceRoom(candidateRoom)) { // only place the room if it does not overlap over others
                carveRoom(candidateRoom); // "draw" the room by filling in floor tiles
                rooms.add(candidateRoom); // store the room in the list of rooms for reference when connecting rooms later
            } 
        }
    }

    // The following method checks if a room is valid and does not overlap and is not too close to any existing rooms.

    private boolean canPlaceRoom (Room candidate) {
        // prevents the rooms from touching the edges of the world to avoid out of bounds errors when adding walls later and also just for aesthetics
        if (candidate.getLeft() <= 0 || candidate.getRight() >= width - 1 || candidate.getBottom() <= 0 || candidate.getTop() >= height - 1) {
            return false;
        }
        // check the candidate room against all existing rooms to make sure it does not overlap or is not too close (within 1 tile) of any of them
        for (Room room : rooms) { 
            if (candidate.overlaps(room, 4)) {
                return false;
            }
        }
        return true; // if the room passed all checks, it is valid and can be placed
    }

    // The following method carves out a room by filling area with FLOOR tiles
    // The following method is to be done by Leo and will be used in the generateRooms method after a valid room is found.
    private void carveRoom(Room room) {
        int roomY = room.getY();
        int roomX = room.getX();
        int roomHeight = room.getHeight();
        int roomWidth = room.getWidth();

        // Nested for loop through each tile within room size at the room's coordinate and fill with floor.
        for (int yAxis = roomY; yAxis < roomY +roomHeight; yAxis++) {
            for (int xAxis = roomX; xAxis < roomX + roomWidth; xAxis++) {
                world[xAxis][yAxis] = Tileset.FLOOR;
            }
        }
    }

    // The following method connects all rooms together in a chain-like way ensuring there is full connectivity between all rooms.

    private void connectRooms() {
        if (rooms.size() < 2) {
            return; // if there are less than 2 rooms, we don't need to connect anything
        }
        // shuffle the rooms to create more variety and randomness
        ArrayList<Room> shuffledRooms = new ArrayList<>(rooms);
        Collections.shuffle(shuffledRooms, rand);

        // connectedRooms will store the rooms that are already apart of the plan
        ArrayList<Room> connectedRooms = new ArrayList<>();
        // should start with the first room already connected
        connectedRooms.add(shuffledRooms.get(0));

        // connect each room to the next one in the shuffled list to make sure there is full connectivity between all rooms
        for (int i = 1; i < shuffledRooms.size(); i++) {
            Room newRoom = shuffledRooms.get(i);
            Room existingRoom = connectedRooms.get(rand.nextInt(connectedRooms.size())); // pick an existing connected room at random
            connectTwoRooms(existingRoom, newRoom); // connect new room into the existing
            connectedRooms.add(newRoom); // now this room is apart of the connected rooms
        }
    }

    // The following method adds extra connections to avoid just linear layouts and make the world more interesting.

    private void addMoreConnections() {
        if (rooms.size() < 4) {
            return; // if there are less than 4 rooms, we don't need to add extra connections since the main connections already connect everything together and there won't be much variety to add
        }

        int extraConnections = randomInt(0, 2); // random number of extra connections to add
        for (int i = 0; i < extraConnections; i++) {
            Room roomA = rooms.get(rand.nextInt(rooms.size())); // randomly select a room
            Room roomB = rooms.get(rand.nextInt(rooms.size())); // randomly select another room
            if (roomA != roomB) { // make sure we are not connecting the same room to itself
                connectTwoRooms(roomA, roomB); // connect the two rooms with a hallway
            }
        }
    }

    // The following method connects two rooms with an L-shaped hallway.

    private void connectTwoRooms(Room roomA, Room roomB) {
        int x1 = roomA.getCenterX();
        int y1 = roomA.getCenterY();
        int x2 = roomB.getCenterX();
        int y2 = roomB.getCenterY();

        if (rand.nextBoolean()) { // randomly decide whether to go horizontal then vertical or vertical then horizontal for variety
            carveHorizontalHallway(x1, x2, y1); // carve the horizontal part of the hallway
            carveVerticalHallway(y1, y2, x2); // carve the vertical part of the hallway
        } else {
            carveVerticalHallway(y1, y2, x1); // carve the vertical part of the hallway
            carveHorizontalHallway(x1, x2, y2); // carve the horizontal part of the hallway
        }
    }

    // The following method creates a horizontal hallway between two x coordinates at a given y coordinate.
    // The following method is to be done by Leo and can look to carveVerticalHallway for reference.
    private void carveHorizontalHallway(int x1, int x2, int y) {
        int start = Math.min(x1, x2);
        int end = Math.max(x1, x2);
        for (int x = start; x <= end; x++) {
            if (inBounds(x, y)) {
                world[x][y] = Tileset.FLOOR;
            }
        }
    }

    // The following method creates a vertical hallway between two y coordinates at a given x coordinate.

    private void carveVerticalHallway(int y1, int y2, int x) {
        int start = Math.min(y1, y2);
        int end = Math.max(y1, y2);
        for (int y = start; y <= end; y++) {
            if (inBounds(x, y)) {
                world[x][y] = Tileset.FLOOR;
            }
        }
    }

    // The following method adds walls around all floor tiles 

    private void addWalls() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (world[x][y] == Tileset.FLOOR) {
                    addWallsAroundTile(x, y);
                }
            }
        }
    }

    // The following method will surround a floor tile with wall tiles if it is needed.

    private void addWallsAroundTile(int x, int y) {
        for (int diffx = -1; diffx <= 1; diffx++) {
            for (int diffy = -1; diffy <= 1; diffy++) {
                int newX = x + diffx;
                int newY = y + diffy;
                if (inBounds(newX, newY) && world[newX][newY] == Tileset.NOTHING) {
                    world[newX][newY] = Tileset.WALL;
                }
            }
        }
    }

    // The following method checks if a coordinate is within the bounds of the world.
    // This is a helper method and to be done by Leo.
    private boolean inBounds(int x, int y) {
        return x < width && y < height && x >= 0 && y >= 0; // if x and y is within the borders and >= 0, it's true. Else, false.
    }

    // The following method generates a random integer between min and max inclusive using the world's random number generator.
    // This is a helper method and to be done by Leo.
    private int randomInt(int min, int max) {
        return rand.nextInt(min, max + 1);
    }


}
