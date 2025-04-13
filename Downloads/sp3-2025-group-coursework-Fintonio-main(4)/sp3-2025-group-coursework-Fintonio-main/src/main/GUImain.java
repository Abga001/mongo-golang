package main;

import game.GameState;

import java.util.Optional;

/**
 * Run this program to see a demonstration of the GUI interface
 */
public class GUImain {
    /**
     * The main program.
     */
    public static void main(String[] args) {
        long seed = 1050L;
        GameState.runNewGame(seed, true);
    }
}
