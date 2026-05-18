package core;

import edu.princeton.cs.algs4.StdDraw;
import tileengine.TERenderer;
import tileengine.TETile;

import java.awt.*;

public class Main {

    private static final int WIDTH = 60;
    private static final int HEIGHT = 30;

    public static void main(String[] args) {

        // build your own world!
//        TERenderer ter = new TERenderer();
//        ter.initialize(WIDTH, HEIGHT);
//
//        World newWorld = new World(WIDTH, HEIGHT, 4428413757366399643L);
//        TETile[][] world = newWorld.generateWorld();
//
//        StdDraw.clear(new Color(0, 0, 0));
//        ter.drawTiles(world);
//        StdDraw.show();
//        StdDraw.pause(10);

        Game game = new Game();
        game.run();

    }

}
