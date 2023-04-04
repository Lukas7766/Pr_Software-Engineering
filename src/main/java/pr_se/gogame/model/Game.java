package pr_se.gogame.model;

import pr_se.gogame.view_controller.GameEvent;
import pr_se.gogame.view_controller.GameListener;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Game implements GameInterface {
    private final List<GameListener> listeners;
    private int size = 19;
    private int komi = 0;

    public Game() {
        this.listeners = new ArrayList<>();
    }

    public void newGame() {
        newGame(size, komi);
    }

    @Override
    public void newGame(int size, int komi) {
        this.size = size;
        this.komi = komi;
        System.out.println("newGame, Size: " + size + " Komi: " + komi);
        fireNewGame(size, komi);
    }


    @Override
    public boolean saveGame() {
        return true;
    }

    @Override
    public boolean importGame(Path path) {
        System.out.println(path);
        return true;
    }

    @Override
    public boolean exportGame(Path path) {
        System.out.println(path);
        return true;
    }

    @Override
    public void pass() {
        System.out.println("pass");
    }

    @Override
    public void resign() {
        System.out.println("resign");

    }

    @Override
    public void scoreGame() {
        System.out.println("scoreGame");
    }

    @Override
    public int getSize() {
        return this.size;
    }

    @Override
    public int getKomi() {
        return this.komi;
    }

    @Override
    public void addListener(GameListener l) {
        listeners.add(l);
    }

    @Override
    public void removeListener(GameListener l) {
        listeners.remove(l);

    }

    private void fireNewGame(int size, int komi) {
        GameEvent e = new GameEvent(size, komi);
        for (GameListener l : listeners) {
            l.resetGame(e);
        }
    }

}
