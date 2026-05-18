package core;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import tileengine.TETile;
import tileengine.Tileset;

public class Pathfinder {
    // Uses BFS to find the shortest path from avatar to clicked tile.
    private static final int[][] directions = {
            {0, 1},    // up
            {1, 0},    // right
            {0, -1},   // down
            {-1, 0}    // left
    };

    public ArrayList<Position> shortestPath(TETile[][] world, Position start, Position goal) {
        ArrayList<Position> empty = new ArrayList<>();

        if (!inBounds(world, goal.x, goal.y) || !isFloor(world, goal.x, goal.y)) {
            return empty; // no path if goal is outside or not walkable
        }

        ArrayDeque<Position> queue = new ArrayDeque<>(); // BFS queue
        HashSet<Position> visited = new HashSet<>(); // tracks spots we already checked
        HashMap<Position, Position> parent = new HashMap<>(); // remembers how we reached each tile

        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            Position curr = queue.removeFirst();

            if (curr.equals(goal)) {
                return buildPath(parent, start, goal); // BFS found the shortest path
            }

            for (int[] d : directions) {
                Position next = curr.shift(d[0], d[1]); // check a neighboring tile

                if (inBounds(world, next.x, next.y)
                        && isFloor(world, next.x, next.y)
                        && !visited.contains(next)) {

                    visited.add(next); // mark so we do not visit again
                    parent.put(next, curr); // remember where this tile came from
                    queue.addLast(next); // add it to the BFS search
                }
            }
        }

        return empty; // no path was found
    }

    private ArrayList<Position> buildPath(HashMap<Position, Position> parent,
                                          Position start,
                                          Position goal) {
        ArrayList<Position> path = new ArrayList<>();
        Position curr = goal;

        while (!curr.equals(start)) {
            path.add(curr); // add each tile while walking backward
            curr = parent.get(curr); // move one step closer to the start
        }

        Collections.reverse(path); // reverse so path goes start to goal
        return path;
    }

    private boolean isFloor(TETile[][] world, int x, int y) {
        return world[x][y].equals(Tileset.FLOOR); // avatar can only walk on floor
    }

    private boolean inBounds(TETile[][] world, int x, int y) {
        return x >= 0 && x < world.length && y >= 0 && y < world[0].length;
    }
}
