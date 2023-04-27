package pr_se.gogame.view_controller;

import pr_se.gogame.model.GameCommand;

public class StoneEvent extends GameEvent {
    private final int X;
    private final int Y;

    protected StoneEvent(GameCommand gameCommand, int col, int row) {
        super(gameCommand);
        if(col < 0 || row < 0) {
            throw new IllegalArgumentException();
        }

        this.X = col;
        this.Y = row;
    }

    public int getX() {
        return X;
    }

    public int getY() {
        return Y;
    }
}
