package com.zhunio.coingame.game;

/**
 * Represents the state of the game.
 */
public class GameState extends State {
    public static final int LIVE = 0;
    public static final int PAUSED = 1;
    public static final int GAME_OVER = 2;

    public GameState(int state) {
        super(state);
    }
}
