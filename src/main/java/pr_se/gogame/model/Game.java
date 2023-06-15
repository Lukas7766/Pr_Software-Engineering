package pr_se.gogame.model;

import pr_se.gogame.view_controller.GameEvent;
import pr_se.gogame.view_controller.GameListener;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static pr_se.gogame.model.StoneColor.BLACK;

public class Game implements GameInterface {

    //Settings
    private Ruleset ruleset = new JapaneseRuleset();
    private int size = 19; // TODO: Just a thought, but technically, this is really just a property of the board, so maybe the Game shouldn't save this at all and instead just provide a method to obtain the board size via its interface (said method would then return board.getSize()).
    private int handicap = 0;

    //global (helper) variables
    private Path savaGamePath;
    private FileTree fileTree;
    private GameCommand gameCommand;
    private final List<GameListener> listeners;
    private Board board;
    private int curMoveNumber;
    private StoneColor curColor;
    private int handicapStoneCounter = 0;   // counter for manually placed handicap stones
    private double playerBlackScore;
    private int blackCapturedStones;

    private double playerWhiteScore;
    private int whiteCapturedStones;
    private GameResult gameResult;

    // TODO: Remove this temporary helper class
    private GeraldsHistory geraldsHistory;

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
    public void newGame(StoneColor startingColor, int size, int handicap, Ruleset ruleset) {
        if(size < 0 || handicap < 0 || handicap > 9) {
            throw new IllegalArgumentException();
        }

        if(startingColor == null) {
            throw new NullPointerException();
        }

        //TODO: Remove this when GeraldsHistory is removed
        geraldsHistory = new GeraldsHistory(this);

        this.curColor = startingColor;
        this.size = size;
        this.handicap = handicap;
        this.ruleset = ruleset;

        this.fileTree = new FileTree(size,"Black", "White");

        this.playerBlackScore = handicap;
        this.playerWhiteScore = this.ruleset.getKomi();
        this.blackCapturedStones = 0;
        this.whiteCapturedStones = 0;
        this.curMoveNumber = 0; // Note: Indicates to the BoardPane that handicap stones are being set.
        this.gameResult = null;

        this.board = new Board(this);
        this.ruleset.reset();
        fireGameEvent(new GameEvent(GameCommand.NEW_GAME));
        this.gameCommand = GameCommand.COLOR_HAS_CHANGED;
        this.ruleset.setHandicapStones(this, this.curColor, this.handicap);

        this.curMoveNumber = 1;
        this.gameCommand = GameCommand.COLOR_HAS_CHANGED;

        System.out.println("\nnewGame: " + gameCommand + " Size: " + size + " Handicap: " + handicap + " Komi: " + this.ruleset.getKomi() + "\n------");
    }

    @Override
    public boolean saveGame() {
        if (savaGamePath == null) {
            return false;
        }
        System.out.println("saved a file");
        return fileTree.saveFile(savaGamePath);
    }

    @Override
    public boolean loadGame(Path path) {
        //TODO: Das board überchreiben od nd
        FileHandler fileHandler = new FileHandler();
        fileHandler.loadFile(path);

        this.newGame(BLACK, 19, 0, new JapaneseRuleset());
        return false;
    }

    @Override
    public void pass() {
        System.out.println("pass");
        UndoableCommand c = switchColor();

        // TODO: send c to FileTree, so that FileTree can save this UndoableCommand at the current node (and then, of course, append a new, command-less node).
        geraldsHistory.addNode(new GeraldsNode(c, "pass"));

        for(GameEvent e : c.getExecuteEvents()) {
            fireGameEvent(e);
        }
    }

    @Override
    public void resign() {
        if(this.gameCommand == GameCommand.GAME_WON) {
            throw new IllegalStateException("Game has already ended!");
        }

        System.out.println("resign");

        final GameResult OLD_GAME_RESULT = this.gameResult;
        final GameCommand OLD_GAME_COMMAND = this.gameCommand;
        final StoneColor FINAL_CUR_COLOR = this.curColor;

        UndoableCommand c = new UndoableCommand() {
            @Override
            public void execute(boolean saveEffects) {
                String msg =
                        "Game was resigned by " + FINAL_CUR_COLOR + "!\n" +
                        "\n" +
                        StoneColor.getOpposite(FINAL_CUR_COLOR) + " won!";
                gameResult = new GameResult(playerBlackScore, playerWhiteScore, StoneColor.getOpposite(FINAL_CUR_COLOR), msg);
                gameCommand = GameCommand.GAME_WON;
                if(saveEffects) {
                    getExecuteEvents().add(new GameEvent(gameCommand));
                    getUndoEvents().add(new GameEvent(OLD_GAME_COMMAND));
                }
            }

            @Override
            public void undo() {
                gameResult = OLD_GAME_RESULT;
                gameCommand = OLD_GAME_COMMAND;
            }
        };
        c.execute(true);

        // TODO: send c to FileTree, so that FileTree can save this UndoableCommand at the current node (and then, of course, append a new, command-less node).
        geraldsHistory.addNode(new GeraldsNode(c, "resign"));

        for(GameEvent e : c.getExecuteEvents()) {
            fireGameEvent(e);
        }
    }

    @Override
    public void scoreGame() { // TODO: Is this only of cosmetic relevance or does it need to be undoable?
        System.out.println("scoreGame");
        this.gameResult = ruleset.scoreGame(this);
        this.playerBlackScore = gameResult.getScoreBlack();
        this.playerWhiteScore = gameResult.getScoreWhite();
        this.gameCommand = GameCommand.GAME_WON;

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
        if((noStones < 0 && noStones != -1)) {
            throw new IllegalArgumentException();
        }

        this.handicapStoneCounter = noStones;
    }

    @Override
    public GameCommand getGameState() {
        return gameCommand;
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

    private UndoableCommand setCurColor(StoneColor c) {
        if (c == null) {
            throw new NullPointerException();
        }

        final StoneColor OLD_COLOR = this.curColor;
        final GameCommand OLD_COMMAND = this.gameCommand;

        UndoableCommand ret = new UndoableCommand() {
            @Override
            public void execute(boolean saveEffects) {
                Game.this.curColor = c;
                Game.this.gameCommand = GameCommand.COLOR_HAS_CHANGED;
            }

            @Override
            public void undo() {
                Game.this.curColor = OLD_COLOR;
                Game.this.gameCommand = OLD_COMMAND;
            }
        };
        ret.execute(true);

        return ret;
    }

    @Override
    public void playMove(int x, int y) {
        /*if(this.gameCommand != GameCommand.NEW_GAME && this.gameCommand != GameCommand.COLOR_HAS_CHANGED) {
            throw new IllegalStateException("Can't place stone when game isn't being played! Game State was " + this.gameCommand);
        }*/

        if(x < 0 || y < 0 || x >= size || y >= size) {
            throw new IllegalArgumentException();
        }

        final UndoableCommand UC01_SET_STONE = board.setStone(x, y, curColor, false); // UC01_SET_STONE is already executed within board.setStone().

        if(UC01_SET_STONE == null) {
            System.out.println("Move aborted.");
            return;
        }

        // Assertion: UC01_SET_STONE != null and was hence a valid move.

        final UndoableCommand UC02_IS_KO = ruleset.isKo(this);

        if(UC02_IS_KO == null) {
            UC01_SET_STONE.undo();
            System.out.println("Ko detected. Move aborted.");
            return;
        }

        final int OLD_MOVE_NO = curMoveNumber;

        final UndoableCommand UC03_SWITCH_COLOR = new UndoableCommand() {
            UndoableCommand UC03_01_switchColor = null;

            @Override
            public void execute(boolean saveEffects) {
                curMoveNumber++;
                // Update current player color
                UC03_01_switchColor = switchColor();
                if(saveEffects) {
                    getExecuteEvents().addAll(UC03_01_switchColor.getExecuteEvents());
                    getUndoEvents().addAll(UC03_01_switchColor.getUndoEvents());
                }
            }

            @Override
            public void undo() {
                if(UC03_01_switchColor != null) {
                    UC03_01_switchColor.undo();
                }
                curMoveNumber = OLD_MOVE_NO;
            }
        };
        UC03_SWITCH_COLOR.execute(true);

        UndoableCommand c = new UndoableCommand() {
            @Override
            public void execute(boolean saveEffects) {
                UC01_SET_STONE.execute(saveEffects);
                UC02_IS_KO.execute(saveEffects);
                UC03_SWITCH_COLOR.execute(saveEffects);
            }

            @Override
            public void undo() {
                UC03_SWITCH_COLOR.undo();
                UC02_IS_KO.undo();
                UC01_SET_STONE.undo();
            }
        };
        // c was already executed piecemeal
        c.getExecuteEvents().addAll(UC01_SET_STONE.getExecuteEvents());
        c.getExecuteEvents().addAll(UC03_SWITCH_COLOR.getExecuteEvents());
        c.getUndoEvents().addAll(UC01_SET_STONE.getUndoEvents());
        c.getUndoEvents().addAll(UC03_SWITCH_COLOR.getUndoEvents());

        // TODO: send c to FileTree, so that FileTree can save this UndoableCommand at the current node (and then, of course, append a new, command-less node).
        geraldsHistory.addNode(new GeraldsNode(c, "playMove(" + x + ", " + y + ")"));

        for(GameEvent e : c.getExecuteEvents()) {
            fireGameEvent(e);
        }
    }

    @Override
    public void placeHandicapPosition(int x, int y, boolean placeStone) {
        /*if(this.gameCommand != GameCommand.COLOR_HAS_CHANGED) {
            throw new IllegalStateException("Can't place handicap stone after game start!");
        }*/

        if(x < 0 || y < 0 || x >= size || y >= size) {
            throw new IllegalArgumentException();
        }

        final int OLD_HANDICAP_CTR = handicapStoneCounter;
        handicapStoneCounter--;
        final int NEW_HANDICAP_CTR = handicapStoneCounter;

        if(placeStone) {
            if (OLD_HANDICAP_CTR <= 0) {
                throw new IllegalStateException("Can't place any more handicap stones!");
            }

            final UndoableCommand UC01_SET_STONE = board.setStone(x, y, curColor, true); // UC01_SET_STONE is already executed within board.setStone().

            if(UC01_SET_STONE == null) {
                System.out.println("Move aborted.");
                return;
            }

            // Assertion: UC01_SET_STONE != null and was hence a valid move.

            final UndoableCommand UC02_UPDATE_COUNTER = new UndoableCommand() {
                UndoableCommand uC02_switchColor = null;

                @Override
                public void execute(boolean saveEffects) {
                    handicapStoneCounter = NEW_HANDICAP_CTR;

                    if (NEW_HANDICAP_CTR <= 0) {
                        System.out.println("handicapStoneCounter is now less than 0.");
                        // fileTree.insertBufferedStonesBeforeGame();
                        uC02_switchColor = switchColor();
                        if(saveEffects) {
                            getExecuteEvents().addAll(uC02_switchColor.getExecuteEvents());
                            getUndoEvents().addAll(uC02_switchColor.getUndoEvents());
                        }
                    }
                }

                @Override
                public void undo() {
                    if(uC02_switchColor != null) {
                        uC02_switchColor.undo();
                    }
                    handicapStoneCounter = OLD_HANDICAP_CTR;
                }
            };
            UC02_UPDATE_COUNTER.execute(true);

            UndoableCommand c = new UndoableCommand() {
                @Override
                public void execute(boolean saveEffects) {
                    UC01_SET_STONE.execute(saveEffects);
                    UC02_UPDATE_COUNTER.execute(saveEffects);
                }

                @Override
                public void undo() {
                    UC02_UPDATE_COUNTER.undo();
                    UC01_SET_STONE.undo();
                }
            };
            // c was already executed piecemeal
            c.getExecuteEvents().addAll(UC01_SET_STONE.getExecuteEvents());
            c.getExecuteEvents().addAll(UC02_UPDATE_COUNTER.getExecuteEvents());
            c.getUndoEvents().addAll(UC01_SET_STONE.getUndoEvents());
            c.getUndoEvents().addAll(UC02_UPDATE_COUNTER.getUndoEvents());

            // TODO: send c to FileTree, so that FileTree can save this UndoableCommand at the current node (and then, of course, append a new, command-less node).
            geraldsHistory.addNode(new GeraldsNode(c, "placeHandicapPosition(" + x + ", " + y + ")"));

            for(GameEvent e : c.getExecuteEvents()) {
                System.out.println(e);
                fireGameEvent(e);
            }
        }

        fireGameEvent(new GameEvent(GameCommand.HANDICAP_SET, x, y, null, curMoveNumber));
        System.out.println("Handicap Ctr now: " + handicapStoneCounter);
        System.out.println();
    }

    public void stepBack() {
        geraldsHistory.stepBack();
    }

    public void stepForward() {
        geraldsHistory.stepForward();
    }

    public void rewind() {
        geraldsHistory.rewind();
    }

    public void skipToEnd() {
        geraldsHistory.skipToEnd();
    }

    public String getComment() {
        return geraldsHistory.currentComment();
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
            public void execute(boolean saveEffects) {
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
        ret.execute(true);

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

    @Override
    public Path getSavePath() {
        return savaGamePath;
    }

    @Override
    public void setSavePath(Path path) {
        if(path == null) return;
        this.savaGamePath = path;
    }

    private UndoableCommand switchColor() {
        UndoableCommand ret = new UndoableCommand() {
            UndoableCommand thisCommand;

            @Override
            public void execute(boolean saveEffects) {
                thisCommand = setCurColor(StoneColor.getOpposite(curColor));
                if(saveEffects) {
                    getExecuteEvents().add(new GameEvent(gameCommand));
                    getUndoEvents().add(new GameEvent(gameCommand));
                }
            }

            @Override
            public void undo() {
                if(thisCommand != null) {
                    thisCommand.undo();
                }
            }
        };
        ret.execute(true);

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

    public void printDebugInfo(int x, int y) {
        board.printDebugInfo(x, y);
    }

}

