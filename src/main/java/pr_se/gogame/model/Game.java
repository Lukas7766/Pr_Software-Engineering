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
    private int handicap = 0;

    private double komi = 7.5;
    private Board board;


    private int curMoveNumber = 0;
    private StoneColor curColor = StoneColor.BLACK;

    private Ruleset ruleset = new JapaneseRuleset();

    private FileSaver fileSaver;

    private int handicapStoneCounter = 0;   // counter for manually placed handicap stones
    private boolean confirmationNeeded;
    private boolean showMoveNumbers;
    private boolean showCoordinates;

    public Game() {
        this.listeners = new ArrayList<>();
        this.gameCommand = GameCommand.INIT;
        this.board = new Board(this, StoneColor.BLACK);
    }

    public void initGame() {
        this.gameCommand = GameCommand.INIT;

        System.out.println("initGame: " + gameCommand);
        fireGameEvent(new GameEvent(gameCommand));
    }


    @Override
    public void newGame(GameCommand gameCommand, int size, int handicap, double komi) {
        switch (gameCommand) {
            case BLACKSTARTS -> this.curColor = StoneColor.BLACK;
            case WHITSTARTS -> this.curColor = StoneColor.WHITE;
            default -> throw new IllegalArgumentException();
        }

        this.fileSaver = new FileSaver("Black", "White", String.valueOf(size));
        this.size = size;
        this.handicap = handicap;
        this.komi = komi;
        this.gameCommand = gameCommand;
        this.curMoveNumber = 1;
        this.board = new Board(this, this.curColor); // Warning: This may set the handicapStoneCounter, so beware of changing it after calling this constructor.

        System.out.println("newGame: " + gameCommand + " Size: " + size + " Handicap: " + handicap + " Komi: " + komi);
        fireGameEvent(new GameEvent(gameCommand, size, handicap, komi));
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
    public boolean saveFile(Path path) {
        return fileSaver.saveFile(path);
    }

    public boolean importFile(Path path) {
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
    public int getHandicap() {
        return this.handicap;
    }

    @Override
    public double getKomi() {
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
        this.gameCommand = GameCommand.CONFIRMCHOICE;
        System.out.println(this.gameCommand);
        fireGameEvent(new GameEvent(gameCommand));
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
        StoneColor c = board.getColorAt(x, y);
        return c;
    }

    @Override
    public int getHandicapStoneCounter() {
        return handicapStoneCounter;
    }

    @Override
    public void setCurMoveNumber(int curMoveNumber) {
        if (curMoveNumber < 1) {
            throw new IllegalArgumentException();
        }

        this.curMoveNumber = curMoveNumber;
    }

    @Override
    public void setCurColor(StoneColor c) {
        if (c == null) {
            throw new NullPointerException();
        }

        this.curColor = c;
    }

    @Override
    public void setHandicapStoneCounter(int counter) {
        this.handicapStoneCounter = counter;
    }

    @Override
    public void playMove(int x, int y) {
        if (board.setStone(x, y, curColor, false)) {
            curMoveNumber++;

            // Update current player color
            switchColor();
        } else {
            System.out.println("Move aborted.");
        }
    }

    @Override
    public void placeHandicapStone(int x, int y) {
        board.setStone(x, y, curColor, true);
        handicapStoneCounter--;
        if (handicapStoneCounter == 0) {
            switchColor();
        } else if (handicapStoneCounter < 0) {
            throw new IllegalStateException();
        }
    }

    @Override
    public void setConfirmationNeeded(boolean needed) {
        this.confirmationNeeded = needed;
        if (confirmationNeeded) {
            this.gameCommand = GameCommand.ENABLECONFIRMATION;
        } else {
            this.gameCommand = GameCommand.DISABLECONFIRMATION;
        }
        fireGameEvent(new GameEvent(gameCommand));
    }

    @Override
    public void setShowMoveNumbers(boolean show) {
        this.showMoveNumbers = show;
        if (show) {
            this.gameCommand = GameCommand.ENABLEMOVENUMBERS;
        } else {
            this.gameCommand = GameCommand.DISABLEMOVENUMBERS;
        }
        fireGameEvent(new GameEvent(gameCommand));
    }

    @Override
    public void setShowCoordinates(boolean show) {
        this.showCoordinates = show;
        if (show) {
            this.gameCommand = GameCommand.ENABLECOORDINATES;
        } else {
            this.gameCommand = GameCommand.DISABLECOORDINATES;
        }
        fireGameEvent(new GameEvent(gameCommand));
    }

    private void switchColor() {
        if (curColor == BLACK) {
            curColor = WHITE;
        } else {
            curColor = BLACK;
        }
    }


    /*
    I would have liked to give it default visibility so it's visible only in the same package, but alas IntelliJ
    won't let me.
    -> 20230502, SeWa: changed to package private
 */
    void fireGameEvent(GameEvent e) {
        for (GameListener l : listeners) {
            l.gameCommand(e);
        }
    }

    // TODO: Remove this debug method
    public void printDebugInfo(int x, int y) {
        board.printDebugInfo(x, y);
    }
}
