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
        this.board = new Board(this);
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
        this.board = new Board(this);
        this.ruleset.setHandicapStones(this, this.curColor, this.handicap);
        this.gameResult = null;

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
        UndoableCommand c = switchColor(); // Everything that was removed was already being done in switchColor(), so I replaced it with a simple method call to reduce code duplication

        // TODO: send c to FileTree, so that FileTree can save this UndoableCommand at the current node (and then, of course, append a new, command-less node).
    }

    @Override
    public void resign() {
        System.out.println("resign");

        final GameResult OLD_GAME_RESULT = this.gameResult;
        final GameCommand OLD_GAME_COMMAND = this.gameCommand;

        UndoableCommand c = new UndoableCommand() {
            @Override
            public void execute() {
                GameResult result;
                StringBuilder sb = new StringBuilder();
                sb.append("Game was resigned by").append(" ");
                switch (OLD_GAME_COMMAND) {
                    case WHITE_PLAYS, WHITE_STARTS -> {
                        Game.this.gameCommand = GameCommand.BLACK_WON;
                        result = new GameResult(playerBlackScore, playerWhiteScore, BLACK,sb.toString());
                        sb.append("White!").append("\n\n").append("Black won!");
                    }
                    case BLACK_PLAYS, BLACK_STARTS -> {
                        Game.this.gameCommand = GameCommand.WHITE_WON;
                        result = new GameResult(playerBlackScore, playerWhiteScore, WHITE,sb.toString());
                        sb.append("Black!").append("\n\n").append("White won!");
                    }
                    default -> {throw new IllegalStateException("Game was not resigned! Consult your application owner!");}
                }
                Game.this.gameResult = result;
                fireGameEvent(new GameEvent(gameCommand));
            }

            @Override
            public void undo() {
                Game.this.gameResult = OLD_GAME_RESULT;
                Game.this.gameCommand = OLD_GAME_COMMAND;
                fireGameEvent(new GameEvent(gameCommand));
            }
        };
        c.execute();

        // TODO: send c to FileTree, so that FileTree can save this UndoableCommand at the current node (and then, of course, append a new, command-less node).
    }

    @Override
    public void scoreGame() { // TODO: Is this only of cosmetic relevance or does it need to be undoable?
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

    /*
     * Although this method changes the state, it is only called at the beginning of the game and, hence, doesn't
     * appear to need to be undoable.
     */
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
    public UndoableCommand setCurColor(StoneColor c) { // TODO: Could this method be set to private? Is there ever a situation where the current color should be manually alterable during a game, outside of pass()?
        if (c == null) {
            throw new NullPointerException();
        }

        final StoneColor OLD_COLOR = this.curColor;
        final GameCommand OLD_COMMAND = this.gameCommand;

        UndoableCommand ret = new UndoableCommand() {
            @Override
            public void execute() {
                Game.this.curColor = c;
                if(Game.this.curColor == BLACK) {
                    Game.this.gameCommand = GameCommand.BLACK_PLAYS;
                } else {
                    Game.this.gameCommand = GameCommand.WHITE_PLAYS;
                }
            }

            @Override
            public void undo() {
                Game.this.curColor = OLD_COLOR;
                Game.this.gameCommand = OLD_COMMAND;
            }
        };
        ret.execute();

        return ret;
    }

    @Override
    public void playMove(int x, int y) {
        /*if(this.gameCommand != GameCommand.BLACK_STARTS && this.gameCommand != GameCommand.WHITE_STARTS &&
            this.gameCommand != GameCommand.BLACK_PLAYS && this.gameCommand != GameCommand.WHITE_PLAYS) {
            throw new IllegalStateException("Can't place stone when game isn't being played! Game State was " + this.gameCommand);
        }*/

        if(x < 0 || y < 0 || x >= size || y >= size) {
            throw new IllegalArgumentException();
        }

        final UndoableCommand UC01_setStone = board.setStone(x, y, curColor, false, true); // returned command is already executed within board.setStone().

        final int OLD_MOVE_NO = curMoveNumber;

        UndoableCommand c = new UndoableCommand() {
            UndoableCommand c_UC02_switchColor = null;

            @Override
            public void execute() {
                if (UC01_setStone != null) {
                    curMoveNumber++;
                    System.out.println("show move # " + showMoveNumbers);
                    System.out.println("Move played.");
                    printDebugInfo(x, y);
                    // Update current player color
                    c_UC02_switchColor = switchColor();
                } else {
                    System.out.println("Move aborted.");
                }
            }

            @Override
            public void undo() {
                c_UC02_switchColor.undo();
                curMoveNumber = OLD_MOVE_NO;
                UC01_setStone.undo();
            }
        };
        c.execute();

        // TODO: send c to FileTree, so that FileTree can save this UndoableCommand at the current node (and then, of course, append a new, command-less node).
    }

    @Override
    public void placeHandicapStone(int x, int y) {
        /*if(this.gameCommand != GameCommand.BLACK_STARTS && this.gameCommand != GameCommand.WHITE_STARTS) {
            throw new IllegalStateException("Can't place handicap stone after game start!");
        }*/

        if(x < 0 || y < 0 || x >= size || y >= size) {
            throw new IllegalArgumentException();
        }

        final int OLD_HANDICAP_COUNTER = handicapStoneCounter;

        if (handicapStoneCounter == 0) {
            throw new IllegalStateException("Can't place any more handicap stones!");
        }

        UndoableCommand c = new UndoableCommand() {
            UndoableCommand uc01_setStone = null;
            UndoableCommand uC02_switchColor = null;

            @Override
            public void execute() {
                handicapStoneCounter--; // TODO: Unsure whether this may cause problems.
                uc01_setStone = board.setStone(x, y, curColor, true, true);

                if (handicapStoneCounter == 0) {
                    // fileTree.insertBufferedStonesBeforeGame();
                    uC02_switchColor = switchColor();
                }
            }

            @Override
            public void undo() {
                if(uC02_switchColor != null) {
                    uC02_switchColor.undo();
                }
                uc01_setStone.undo();
                handicapStoneCounter = OLD_HANDICAP_COUNTER;
            }
        };
        c.execute();

        // TODO: send c to FileTree, so that FileTree can save this UndoableCommand at the current node (and then, of course, append a new, command-less node).
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
    public UndoableCommand addCapturedStones(StoneColor color, int amount) {
        if (color == null) {
            throw new NullPointerException();
        }
        if (amount < 0) {
            throw new IllegalArgumentException();
        }

        final int OLD_BLACK_CAPTURED_STONES = blackCapturedStones;
        final int OLD_WHITE_CAPTURED_STONES = whiteCapturedStones;
        final double OLD_BLACK_PLAYER_SCORE = playerBlackScore;
        final double OLD_WHITE_PLAYER_SCORE = playerWhiteScore;

        UndoableCommand ret = new UndoableCommand() {
            @Override
            public void execute() {
                if (color == BLACK) {
                    Game.this.blackCapturedStones += amount; // TODO: If this causes issues, maybe change to "OLD_BLACK_CAPTURED_STONES + amount" and so on?
                    Game.this.playerBlackScore += amount;
                } else {
                    Game.this.whiteCapturedStones += amount;
                    Game.this.playerWhiteScore += amount;
                }
            }

            @Override
            public void undo() {
                Game.this.blackCapturedStones = OLD_BLACK_CAPTURED_STONES;
                Game.this.whiteCapturedStones = OLD_WHITE_CAPTURED_STONES;
                Game.this.playerBlackScore = OLD_BLACK_PLAYER_SCORE;
                Game.this.playerWhiteScore = OLD_WHITE_PLAYER_SCORE;
            }
        };
        ret.execute();

        return ret;
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

    public UndoableCommand switchColor() { // TODO: Could this method be set to private? Is there ever a situation where the color should be switchable during a game, outside of pass()?
        UndoableCommand ret = new UndoableCommand() {
            UndoableCommand thisCommand;

            @Override
            public void execute() {
                if (curColor == BLACK) {
                    // this.gameCommand = GameCommand.WHITE_PLAYS; // handled by setCurColor()
                    thisCommand = setCurColor(WHITE);
                } else {
                    // this.gameCommand = GameCommand.BLACK_PLAYS; // handled by setCurColor()
                    thisCommand = setCurColor(BLACK);
                }

                fireGameEvent(new GameEvent(gameCommand));
            }

            @Override
            public void undo() {
                if(thisCommand != null) {
                    thisCommand.undo();
                    fireGameEvent(new GameEvent(gameCommand));
                }
            }
        };
        ret.execute();

        return ret;
    }

    void fireGameEvent(GameEvent e) { // package-private by design
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

