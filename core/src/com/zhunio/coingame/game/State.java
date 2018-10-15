package com.zhunio.coingame.game;

/** Represents a state. */
public abstract class State {
    private int state;

    public State(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
