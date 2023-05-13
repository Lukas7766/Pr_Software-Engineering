package pr_se.gogame.model;

import pr_se.gogame.view_controller.GameEvent;
import pr_se.gogame.view_controller.GameListener;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static pr_se.gogame.model.StoneColor.BLACK;
import static pr_se.gogame.model.StoneColor.WHITE;

public class Game implements GameInterface {

    //Settings
    private final Ruleset ruleset = new JapaneseRuleset();
    private int size = 19;
    private int handicap = 0;
    private boolean confirmationNeeded = false;
    private boolean showMoveNumbers = false;
    private boolean showCoordinates = true;

    private boolean demoMode = false;

    //global (helper) variables
    private FileSaver fileSaver;
    private GameCommand gameCommand;
    private final List<GameListener> listeners;
    private Board board;
    private int curMoveNumber = 0;
    private StoneColor curColor;
    private int handicapStoneCounter = 0;   // counter for manually placed handicap stones
    private double playerBlackScore;
    private int blackCapturedStones;

    private double playerWhiteScore;
    private int whiteCapturedStones;
    private GameResult gameResult;


    public Game() {
        this.listeners = new ArrayList<>();
        this.gameCommand = GameCommand.INIT;
        this.board = new Board(this, BLACK);
    }

    @Override
    public void initGame() {
        this.gameCommand = GameCommand.INIT;

        System.out.println("initGame: " + gameCommand);
        fireGameEvent(new GameEvent(gameCommand));
    }

    @Override
    public void newGame(GameCommand gameCommand, int size, int handicap) {
        switch (gameCommand) {
            case BLACKSTARTS -> this.curColor = StoneColor.BLACK;
            case WHITESTARTS -> this.curColor = StoneColor.WHITE;
            default -> throw new IllegalArgumentException();
        }

        this.fileSaver = new FileSaver("Black", "White", String.valueOf(size));
        this.gameCommand = gameCommand;
        this.size = size;
        this.handicap = handicap;
        this.playerBlackScore = handicap;
        this.playerWhiteScore = this.ruleset.getKomi();
        this.blackCapturedStones = 0;
        this.whiteCapturedStones = 0;
        this.curMoveNumber = 0;
        this.board = new Board(this, curColor);

        System.out.println("newGame: " + gameCommand + " Size: " + size + " Handicap: " + handicap + " Komi: " + this.ruleset.getKomi() + "\n");
        fireGameEvent(new GameEvent(gameCommand, size, handicap));
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
    //ToDo move competence to importFile methode?? and delete this method when it is not needed anymore
    public boolean saveFile(Path path) {
        return fileSaver.saveFile(path);
    }

    //ToDo delete this method when it is not needed anymore??
    public boolean importFile(Path path) {
        return FileSaver.importFile(path);
    }

    @Override
    public void pass() {
        System.out.println("pass");
        switch (gameCommand) {
            case BLACKPLAYS, BLACKSTARTS -> {
                this.gameCommand = GameCommand.WHITEPLAYS;
                this.setCurColor(WHITE);
            }
            case WHITEPLAYS, WHITESTARTS -> {
                this.gameCommand = GameCommand.BLACKPLAYS;
                this.setCurColor(BLACK);
            }
        }
        fireGameEvent(new GameEvent(gameCommand));
    }

    @Override
    public void resign() {
        System.out.println("resign");
        switch (gameCommand) {
            case WHITEPLAYS, WHITESTARTS -> this.gameCommand = GameCommand.BLACKWON;
            case BLACKPLAYS, BLACKSTARTS -> this.gameCommand = GameCommand.WHITEWON;
        }
        fireGameEvent(new GameEvent(gameCommand));
    }

    @Override
    public void scoreGame() {
        System.out.println("scoreGame");
        this.gameResult = ruleset.scoreGame(this);
        this.playerBlackScore = gameResult.getScoreBlack();
        this.playerWhiteScore = gameResult.getScoreWhite();

        if (gameResult.getWinner() == BLACK) {
            this.gameCommand = GameCommand.BLACKWON;
        } else if (gameResult.getWinner() == WHITE) {
            this.gameCommand = GameCommand.WHITEWON;
        } else {
            this.gameCommand = GameCommand.DRAW;
        }
        fireGameEvent(new GameEvent(gameCommand));
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
        return this.ruleset.getKomi();
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
        return board.getColorAt(x, y);
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
            System.out.println("show move # " + showMoveNumbers);
            System.out.println("Move played.");
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
    public boolean isDemoMode() {
        return demoMode;
    }

    @Override
    public void setDemoMode(boolean demoMode) {
        this.demoMode = demoMode;
        this.gameCommand = GameCommand.CONFIGDEMOMODE;
        fireGameEvent(new GameEvent(gameCommand));
    }

    @Override
    public void setConfirmationNeeded(boolean needed) {
        this.confirmationNeeded = needed;
        this.gameCommand = GameCommand.CONFIGCONFIRMATION;

        fireGameEvent(new GameEvent(gameCommand));
    }

    @Override
    public boolean isConfirmationNeeded() {
        return this.confirmationNeeded;
    }

    @Override
    public void setShowMoveNumbers(boolean show) {
        this.showMoveNumbers = show;
        this.gameCommand = GameCommand.CONFIGSHOWMOVENUMBERS;

        fireGameEvent(new GameEvent(gameCommand));
    }

    @Override
    public boolean isShowMoveNumbers() {
        return this.showMoveNumbers;
    }

    @Override
    public void setShowCoordinates(boolean show) {
        this.showCoordinates = show;
        this.gameCommand = GameCommand.CONFIGSHOWCOORDINATES;

        fireGameEvent(new GameEvent(gameCommand));
    }

    @Override
    public boolean isShowCoordinates() {
        return this.showCoordinates;
    }

    @Override
    public void setCapturedStones(StoneColor color, int amount) {
        if (color == null) throw new NullPointerException();
        if (amount < 0) throw new IllegalArgumentException();

        if (color == BLACK) {
            this.blackCapturedStones += amount;
            this.playerBlackScore += amount;
        } else {
            this.whiteCapturedStones += amount;
            this.playerWhiteScore += amount;
        }
    }

    @Override
    public int getCapturedStones(StoneColor color) {
        if (color == null) throw new NullPointerException();

        if (color == BLACK) return this.blackCapturedStones;
        else return this.whiteCapturedStones;
    }

    @Override
    public double getScore(StoneColor color) {
        return color == BLACK ? this.playerBlackScore : this.playerWhiteScore;
    }

    @Override
    public GameResult getGameResult() {
        return gameResult;
    }

    private void switchColor() {
        if (curColor == BLACK) {
            curColor = WHITE;
            this.gameCommand = GameCommand.WHITEPLAYS;
        } else {
            curColor = BLACK;
            this.gameCommand = GameCommand.BLACKPLAYS;
        }
        fireGameEvent(new GameEvent(gameCommand));
    }

    /*
    I would have liked to give it default visibility, so it's visible only in the same package, but alas IntelliJ
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

