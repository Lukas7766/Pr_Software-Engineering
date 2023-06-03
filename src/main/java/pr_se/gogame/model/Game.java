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
    private int size = 19; // TODO: Just a thought, but technically, this is really just a property of the board, so maybe the Game shouldn't save this at all and instead just provide a method to obtain the board size via its interface (said method would then return board.getSize()).
    private int handicap = 0;
    private boolean confirmationNeeded = false;
    private boolean showMoveNumbers = false;
    private boolean showCoordinates = true;

    private String graphicsPath = "src/main/resources/pr_se/gogame/debug.zip";

    private boolean demoMode = false;

    //global (helper) variables
    private FileTree fileTree;
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
        if(size < 0 || handicap < 0 || handicap > 9) {
            throw new IllegalArgumentException();
        }

        if(gameCommand == null) {
            throw new NullPointerException();
        }

        switch (gameCommand) {
            case BLACK_STARTS -> this.curColor = StoneColor.BLACK;
            case WHITE_STARTS -> this.curColor = StoneColor.WHITE;
            default -> throw new IllegalArgumentException();
        }

        this.fileTree = new FileTree(size,"Black", "White");
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
        return exportGame(path);
    }

    @Override
    public boolean importGame(Path path) {
        //TODO: Das board überchreiben od nd
        //return FileSaver.importFile(path);
        return false;
    }

    @Override
    public boolean exportGame(Path path) {
        System.out.println("saved a file");
        return fileTree.saveFile(path);
    }

    //ToDo delete this method when it is not needed anymore??
    public boolean importFile(Path path) {
        return true;
    }

    @Override
    public void pass() {
        System.out.println("pass");
        switch (gameCommand) {
            case BLACK_PLAYS, BLACK_STARTS -> {
                // this.gameCommand = GameCommand.WHITE_PLAYS; // moved into setCurColor()
                this.setCurColor(WHITE);
            }
            case WHITE_PLAYS, WHITE_STARTS -> {
                // this.gameCommand = GameCommand.BLACK_PLAYS; // moved into setCurColor()
                this.setCurColor(BLACK);
            }
        }
        fireGameEvent(new GameEvent(gameCommand));
    }

    @Override
    public void resign() {
        System.out.println("resign");
        GameResult result;
        StringBuilder sb = new StringBuilder();
        sb.append("Game was resigned by").append(" ");
        switch (gameCommand) {
            case WHITE_PLAYS, WHITE_STARTS -> {
                this.gameCommand = GameCommand.BLACK_WON;
                sb.append("White!").append("\n\n").append("Black won!");
                result = new GameResult(playerBlackScore, playerWhiteScore, BLACK,sb.toString());
            }
            case BLACK_PLAYS, BLACK_STARTS -> {
                this.gameCommand = GameCommand.WHITE_WON;
                sb.append("Black!").append("\n\n").append("White won!");
                result = new GameResult(playerBlackScore, playerWhiteScore, WHITE,sb.toString());
            }
            default -> {throw new IllegalStateException("Game was not resigned! Consult your application owner!");}
        }
        this.gameResult=result;
        fireGameEvent(new GameEvent(gameCommand));
    }

    @Override
    public void scoreGame() {
        System.out.println("scoreGame");
        this.gameResult = ruleset.scoreGame(this);
        this.playerBlackScore = gameResult.getScoreBlack();
        this.playerWhiteScore = gameResult.getScoreWhite();

        if (gameResult.getWinner() == BLACK) {
            this.gameCommand = GameCommand.BLACK_WON;
        } else {
            this.gameCommand = GameCommand.WHITE_WON;
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
        if(l == null) {
            throw new NullPointerException();
        }
        listeners.add(l);
    }

    @Override
    public void removeListener(GameListener l) {
        if(l == null) {
            throw new NullPointerException();
        }
        listeners.remove(l);
    }

    @Override
    public void setHandicapStoneCounter(int noStones) {
        if(noStones < 0 || noStones > handicap) {
            throw new IllegalArgumentException();
        }

        this.handicapStoneCounter = noStones;
    }

    @Override
    public GameCommand getGameState() {
        return gameCommand;
    }

    @Override
    public void confirmChoice() {
        this.gameCommand = GameCommand.CONFIRM_CHOICE;
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
    public FileTree getFileTree() {
        return fileTree;
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
        if(this.curColor == BLACK) {
            this.gameCommand = GameCommand.BLACK_PLAYS;
        } else {
            this.gameCommand = GameCommand.WHITE_PLAYS;
        }
    }

    @Override
    public void playMove(int x, int y) {
        if (board.setStone(x, y, curColor, false, true)) {
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
        board.setStone(x, y, curColor, true, true);
        handicapStoneCounter--;
        if (handicapStoneCounter == 0) {
            // fileTree.insertBufferedStonesBeforeGame();
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
        this.gameCommand = GameCommand.CONFIG_DEMO_MODE;
        fireGameEvent(new GameEvent(gameCommand));
    }

    @Override
    public void setConfirmationNeeded(boolean needed) {
        this.confirmationNeeded = needed;
        this.gameCommand = GameCommand.CONFIG_CONFIRMATION;

        fireGameEvent(new GameEvent(gameCommand));
    }

    @Override
    public boolean isConfirmationNeeded() {
        return this.confirmationNeeded;
    }

    @Override
    public void setShowMoveNumbers(boolean show) {
        this.showMoveNumbers = show;
        this.gameCommand = GameCommand.CONFIG_SHOWMOVENUMBERS;

        fireGameEvent(new GameEvent(gameCommand));
    }

    @Override
    public boolean isShowMoveNumbers() {
        return this.showMoveNumbers;
    }

    @Override
    public void setShowCoordinates(boolean show) {
        this.showCoordinates = show;
        this.gameCommand = GameCommand.CONFIG_SHOW_COORDINATES;

        fireGameEvent(new GameEvent(gameCommand));
    }

    @Override
    public boolean isShowCoordinates() {
        return this.showCoordinates;
    }

    @Override
    public void addCapturedStones(StoneColor color, int amount) {
        if (color == null) {
            throw new NullPointerException();
        }
        if (amount < 0) {
            throw new IllegalArgumentException();
        }

        if (color == BLACK) {
            this.blackCapturedStones += amount;
            this.playerBlackScore += amount;
        } else {
            this.whiteCapturedStones += amount;
            this.playerWhiteScore += amount;
        }
    }

    @Override
    public int getStonesCapturedBy(StoneColor color) {
        if (color == null) throw new NullPointerException();

        if (color == BLACK) return this.blackCapturedStones;
        else return this.whiteCapturedStones;
    }

    @Override
    public double getScore(StoneColor color) {
        if(color == null) {
            throw new NullPointerException();
        }

        return color == BLACK ? this.playerBlackScore : this.playerWhiteScore;
    }

    @Override
    public GameResult getGameResult() {
        return gameResult;
    }


    @Override
    public String getGraphicsPath() {
        return graphicsPath;
    }

    @Override
    public void setGraphicsPath(String path) {
        if(path == null) {
            throw new NullPointerException();
        }
        this.graphicsPath = path;
        fireGameEvent(new GameEvent(GameCommand.CONFIG_GRAPHICS));
    }

    public void switchColor() {
        if (curColor == BLACK) {
            // this.gameCommand = GameCommand.WHITE_PLAYS; // handled by setCurColor()
            setCurColor(WHITE);
        } else {
            // this.gameCommand = GameCommand.BLACK_PLAYS; // handled by setCurColor()
            setCurColor(BLACK);
        }
        fireGameEvent(new GameEvent(gameCommand));
    }

    /*
    I would have liked to give it default visibility, so it's visible only in the same package, but alas IntelliJ
    won't let me.
    -> 20230502, SeWa: changed to package private
 */
    void fireGameEvent(GameEvent e) {
        if(e == null) {
            throw new NullPointerException();
        }

        for (GameListener l : listeners) {
            l.gameCommand(e);
        }
    }

    // TODO: Remove this debug method
    public void printDebugInfo(int x, int y) {
        board.printDebugInfo(x, y);
    }

}

