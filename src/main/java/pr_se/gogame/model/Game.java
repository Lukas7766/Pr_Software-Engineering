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


    private int curMoveNumber = 1;

    public Game() {
        this.listeners = new ArrayList<>();
        this.gameCommand = GameCommand.INIT;
        this.board = new Board(this, StoneColor.BLACK);
    }

    public void initGame() {
        this.gameCommand = GameCommand.INIT;
        fireGameCommand(gameCommand);
    }


    @Override
    public void newGame(GameCommand gameCommand, int size, int komi) {
        this.size = size;
        this.komi = komi;
        this.gameCommand = gameCommand;
        System.out.println("newGame, Size: " + size + " Komi: " + komi);
        this.board = new Board(this, StoneColor.BLACK);
        fireNewGame(gameCommand, size, komi);
    }


    @Override
    public boolean saveGame(Path path) {
        return true;
    }

    @Override
    public boolean importGame(Path path) {
        //TODO: Das board überschreiben od nd
        return FileSaver.importFile(path);
    }

    @Override
    public boolean exportGame(Path path) {
        System.out.println("saved a file");
        return board.saveFile(path);
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
        return gameCommand;
    }

    @Override
    public void confirmChoice() {
        System.out.println("confirmChoice");
        this.gameCommand = GameCommand.CONFIRMCHOICE;
        fireGameCommand(gameCommand);
    }

    @Override
    public Board getBoard() {
        return this.board;
    }

    @Override
    public int getCurMoveNumber() {
        return curMoveNumber;
    }

    @Override
    public void setCurMoveNumber(int curMoveNumber) {
        if(curMoveNumber < 1) {
            throw new IllegalArgumentException();
        }

        this.curMoveNumber = curMoveNumber;
    }

    /*
        (Added by Gerald) I would have liked to give it default visibility so it's visible only in the same package.
     */
    @Override
    public void fireGameEvent(GameEvent e) {
        for (GameListener l : listeners) {
            l.gameCommand(e);
        }
    }

    private void fireNewGame(GameCommand gameCommand, int size, int komi) {
        fireGameEvent(new GameEvent(gameCommand, size, komi));
    }

    private void fireGameCommand(GameCommand command) {
        fireGameEvent(new GameEvent(command));
    }
}
