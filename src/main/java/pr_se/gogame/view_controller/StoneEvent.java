package pr_se.gogame.view_controller;

import pr_se.gogame.model.GameCommand;

public class StoneEvent extends GameEvent {
    private final int X;
    private final int Y;

    protected StoneEvent(GameCommand gameCommand, int x, int y) {
        super(gameCommand);
        if(x < 0 || y < 0) {
            throw new IllegalArgumentException();
        }

        this.X = x;
        this.Y = y;
    }

    public int getX() {
        return X;
    }

    public int getY() {
        return Y;
    }
}
