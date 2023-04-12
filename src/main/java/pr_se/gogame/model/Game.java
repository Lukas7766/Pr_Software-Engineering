package pr_se.gogame.model;

import pr_se.gogame.view_controller.GameEvent;
import pr_se.gogame.view_controller.GameListener;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Game implements GameInterface {

    private GameCommand gameCommand;
    private final List<GameListener> listeners;
    private int size = 19;
    private int komi = 0;
    private Board board;

    public Game() {
        this.listeners = new ArrayList<>();
        this.gameCommand = GameCommand.INIT;
        this.board = new Board(this.size, this.komi);
    }

    public void initGame() {
       fireInitGame(GameCommand.INIT);
    }


    @Override
    public void newGame(GameCommand gameCommand, int size, int komi) {
        this.size = size;
        this.komi = komi;
        this.gameCommand = gameCommand;
        System.out.println("newGame, Size: " + size + " Komi: " + komi);
        this.board = new Board(this.size, this.komi);
        fireNewGame(gameCommand, size, komi);
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

    @Override
    public GameCommand getGameState() {
        return null;
    }

    @Override
    public void confirmChoice() {
        System.out.println("confirmChoice");
    }

    @Override
    public Board getBoard() {
        return this.board;
    }

    private void fireNewGame(GameCommand gameCommand,int size, int komi) {
        GameEvent e = new GameEvent(gameCommand,size, komi);
        for (GameListener l : listeners) {
            l.gameCommand(e);
        }
    }

    private void fireInitGame(GameCommand init) {
        GameEvent e = new GameEvent(GameCommand.INIT);
        for (GameListener l : listeners){
            l.gameCommand(e);
        }
    }


}
