package pr_se.gogame.view_controller;

public class GameEvent {

    private final boolean newGame;
    private final int size;
    private final int komi;

    public GameEvent(){
        this(true,-1,-1);
    }
    public GameEvent(int size, int komi){
        this(false,-1,-1);
    }

    private GameEvent(boolean newGame, int size, int komi){
        this.size = size;
        this.komi = komi;
        this.newGame = newGame;
    }

    public int getKomi() {
        return komi;
    }

    public int getSize() {
        return size;
    }

    public boolean isNewGame() {
        return newGame;
    }
}
