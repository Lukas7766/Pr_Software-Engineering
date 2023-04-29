package pr_se.gogame.model;

import pr_se.gogame.view_controller.GameEvent;
import pr_se.gogame.view_controller.GameListener;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static pr_se.gogame.model.StoneColor.BLACK;
import static pr_se.gogame.model.StoneColor.WHITE;

public class Game implements GameInterface {

    private GameCommand gameCommand;
    private final List<GameListener> listeners;
    private int size = 19;
    private int komi = 0;
    private Board board;


    private int curMoveNumber = 1;
    private StoneColor curColor = StoneColor.BLACK;

    private Ruleset ruleset = new JapaneseRuleset();

    private FileSaver fileSaver;

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
        if(gameCommand == GameCommand.BLACKSTARTS) {
            this.curColor = StoneColor.BLACK;
        } else if (gameCommand == GameCommand.WHITSTARTS) {
            this.curColor = StoneColor.WHITE;
        } else {
            throw new IllegalArgumentException();
        }

        this.fileSaver = new FileSaver("Black", "White", String.valueOf(size));
        this.size = size;
        this.komi = komi;
        this.gameCommand = gameCommand;
        this.curMoveNumber = 1;
        System.out.println("newGame, Size: " + size + " Komi: " + komi);
        this.board = new Board(this, this.curColor);
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
        return saveFile(path);
    }

    /*
     * Note from Gerald: I moved these out of Board, as the FileSaver saves more than just the board's contents, such as player names,
     * and most importantly the game tree. Additionally, it just seems a good idea to have all IO connections go through
     * Game, as it is the "main class" of the model.
     */
    public boolean saveFile(Path path){
        return fileSaver.saveFile(path);
    }

    public boolean importFile(Path path){
        return FileSaver.importFile(path);
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
        return this.curMoveNumber;
    }

    @Override
    public StoneColor getCurColor() {
        return this.curColor;
    }

    @Override
    public Ruleset getRuleset() {
        return this.ruleset;
    }

    @Override
    public FileSaver getFileSaver() {
        return fileSaver;
    }

    @Override
    public StoneColor getColorAt(int x, int y) {
        return board.getColorAt(x, y);
    }

    @Override
    public void setCurMoveNumber(int curMoveNumber) {
        if(curMoveNumber < 1) {
            throw new IllegalArgumentException();
        }

        this.curMoveNumber = curMoveNumber;
    }

    @Override
    public void setCurColor(StoneColor c) {
        if(c == null) {
            throw new NullPointerException();
        }

        this.curColor = c;
    }

    /*
        I would have liked to give it default visibility so it's visible only in the same package, but alas IntelliJ
        won't let me.
     */
    @Override
    public void fireGameEvent(GameEvent e) {
        for (GameListener l : listeners) {
            l.gameCommand(e);
        }
    }

    @Override
    public void playMove(int x, int y) {
        board.setStone(x, y, curColor, false);

        curMoveNumber++;

        // Update current player color
        if(curColor == WHITE) {
            curColor = BLACK;
        } else {
            curColor = WHITE;
        }
    }

    private void fireNewGame(GameCommand gameCommand, int size, int komi) {
        fireGameEvent(new GameEvent(gameCommand, size, komi));
    }

    private void fireGameCommand(GameCommand command) {
        fireGameEvent(new GameEvent(command));
    }

    // TODO: Remove this debug method
    public void printDebugInfo(int x, int y) {
        board.printDebugInfo(x, y);
    }
}
