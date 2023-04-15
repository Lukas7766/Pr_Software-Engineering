package pr_se.gogame.view_controller;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import pr_se.gogame.model.*;

import java.io.InputStream;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static pr_se.gogame.model.StoneColor.*;

/**
 * View/Controller
 * Board that uses image files for its tiles and stones
 */
public class BoardPane extends GridPane {

    private boolean needsMoveConfirmation = false;  // whether moves have to be confirmed separately (TODO: might need a better name)
    private boolean showsMoveNumbers = true;
    private boolean showsCoordinates = true;

    private int size;
    private Board board;

    private final Game game;

    // Custom resources
    private String graphics;
    private Image tile;
    private final Image[] stones = new Image [2];
    private Image edge;
    private Image corner;

    private PlayableBoardCell lastBC = null;
    private PlayableBoardCell selectionBC = null;

    private NumberBinding MAX_CELL_DIM_INT;

    public BoardPane(Game game, String graphics) {
        if(game == null || graphics == null) {
            throw new NullPointerException();
        }

        setMouseTransparent(true);

        this.game = game;
        this.graphics = graphics;

        game.addListener(l -> {
            if(!(l.getGameCommand().equals(GameCommand.WHITSTARTS) || l.getGameCommand().equals(GameCommand.BLACKSTARTS))) return;
            System.out.println(l.getGameCommand()+" inBoardPane: BoardSize: " + l.getSize() + " Komi: "+  l.getKomi());

            setMouseTransparent(false);
            init();
        }); //ToDo: full Event integration

        init();
    }

    private void init() {
        getChildren().removeAll(getChildren());

        setBoard(this.game.getBoard());
        this.size = board.getSize();

        loadGraphics(graphics);

        // determine cell size
        final NumberBinding MAX_CELL_WIDTH = widthProperty().divide(size + 2);                                                 // Get maximum width if all cells are equally wide
        final NumberBinding MAX_CELL_WIDTH_INT = Bindings.createIntegerBinding(MAX_CELL_WIDTH::intValue, MAX_CELL_WIDTH);        // round down
        final NumberBinding MAX_CELL_HEIGHT = heightProperty().divide(size + 2);                                              // Get maximum height if all cells are equally wide
        final NumberBinding MAX_CELL_HEIGHT_INT = Bindings.createIntegerBinding(MAX_CELL_HEIGHT::intValue, MAX_CELL_HEIGHT);    // round down

        final NumberBinding MAX_CELL_DIM = Bindings.min(MAX_CELL_WIDTH_INT, MAX_CELL_HEIGHT_INT);                               // Use whatever is smaller after the division
        MAX_CELL_DIM_INT = Bindings.createIntegerBinding(MAX_CELL_DIM::intValue, MAX_CELL_DIM);                                 // round down

        // put the axes' corners in first to mess up the indexing as little as possible;
        BoardCell corner1 = new BoardCell(this.corner);
        corner1.getLabel().setVisible(false);
        add(corner1, 0, 0);
        BoardCell corner2 = new BoardCell(this.corner);
        corner2.getLabel().setVisible(false);
        add(corner2, size + 1, 0);
        BoardCell corner3 = new BoardCell(this.corner);
        corner3.getLabel().setVisible(false);
        add(corner3, 0, size + 1);
        BoardCell corner4 = new BoardCell(this.corner);
        corner4.getLabel().setVisible(false);
        add(corner4, size + 1, size + 1);

        // populate the coordinate axes
        for(int i = 0; i < this.size; i++) {
            // top
            BoardCell t = new BoardCell(this.edge);
            t.getLabel().setText("" + (char)('A' + i));
            t.getLabel().setAlignment(Pos.BOTTOM_CENTER);
            add(t, i + 1, 0);

            // bottom
            BoardCell b = new BoardCell(this.edge);
            b.getLabel().setText("" + (char)('A' + i));
            b.getLabel().setAlignment(Pos.TOP_CENTER);
            add(b, i + 1, size + 1);

            // left
            BoardCell l = new BoardCell(this.edge);
            l.getLabel().setText("" + (size - i));
            l.getLabel().setAlignment(Pos.CENTER_RIGHT);
            add(l, 0, i + 1);

            // right
            BoardCell r = new BoardCell(this.edge);
            r.getLabel().setText("" + (size - i));
            r.getLabel().setAlignment(Pos.CENTER_LEFT);
            add(r, size + 1, i + 1);
        }

        // Fill the grid with tiles
        for(int i = 0; i < this.size; i++) {
            for(int j = 0; j < this.size; j++) {
                PlayableBoardCell bc = new PlayableBoardCell();
                /*
                 * We have to check for the initial board condition here, as the BoardPane cannot exist when the Board
                 * is initialised, as that happens on creating the Game, which is required to create the BoardPane.
                 */
                StoneColor c = this.board.getColorAt(j, i);
                if(c != null) {
                    if(c == BLACK) {
                        bc.setBlack();
                    } else {
                        bc.setWhite();
                    }
                    bc.getLabel().setVisible(false);
                }
                add(bc, j + 1, i + 1);
            }
        }

        // Set up listeners
        setOnMouseMoved(e -> {                                                  // TODO: Should this be handled by the BoardCells themselves?
            Node target = (Node)e.getTarget();
            if(target != null) {
                if(target != lastBC) {                                          // TODO: This seems to fire a bit too readily, making the program run less efficiently. I am not sure why, though.
                    Integer col = getColumnIndex(target);
                    Integer row = getRowIndex(target);

                    if (col != null && row != null) {
                        PlayableBoardCell targetBC = (PlayableBoardCell)target;

                        if (this.board.getCurColor() == BLACK) {
                            targetBC.hoverBlack();
                        } else {
                            targetBC.hoverWhite();
                        }

                        // Remove old hover
                        if(lastBC != null) {
                            lastBC.unhover();
                        }
                        lastBC = targetBC;
                    } else if(lastBC != null) {
                        lastBC.unhover();
                        //System.out.println("Hover target is not a cell!");  // TODO: Remove in finished product
                        lastBC = null;
                    }
                }
            } else {
                //System.out.println("Hover target is null!");                // TODO: Remove in finished product
                lastBC = null;
            }
        });

        setOnMouseClicked(e -> {
            if(e.getButton() == MouseButton.PRIMARY) { // This check is only for testing independently of the main UI.
                if (lastBC != null) {
                    if (selectionBC != null) {
                        selectionBC.deselect();
                    }
                    selectionBC = lastBC;
                    if(board.getCurColor() == BLACK) {
                        selectionBC.selectBlack();
                    } else {
                        selectionBC.selectWhite();
                    }

                    lastBC = null;

                    if (!needsMoveConfirmation) {
                        confirmMove();
                    }
                } else {
                    System.out.println("Click outside of BoardPane"); // TODO: Remove in finished product
                }
            } else if(e.getButton() == MouseButton.SECONDARY && needsMoveConfirmation) { // Only for testing purposes
                confirmMove();
            }


        });

        setOnKeyPressed((e) -> {
            // TODO: Keyboard input?
        });


        // Layout of this BoardPane
        setAlignment(Pos.CENTER);
    }

    public void setBoard(Board board) {
        this.board = board;

        board.addListener(new GoListener() {
            @Override
            public void stoneSet(StoneSetEvent e) {
                if(e == null) {
                    throw new NullPointerException();
                }

                PlayableBoardCell destinationBC = (PlayableBoardCell)getChildren().get(e.getRow() * size + e.getCol() + 4 + size * 4);
                destinationBC.getLabel().setText("" + e.getMoveNumber());

                if(e.getColor() == BLACK) {
                    destinationBC.setBlack();
                } else {
                    destinationBC.setWhite();
                }
            }

            @Override
            public void stoneRemoved(StoneRemovedEvent e) {
                if(e == null) {
                    throw new NullPointerException();
                }

                PlayableBoardCell destinationBC = (PlayableBoardCell)getChildren().get(e.getRow() * size + e.getCol() + 4 + size * 4);

                destinationBC.unset();
            }

            @Override
            public void debugInfoRequested(int x, int y, int StoneGroupPtrNO, int StoneGroupSerialNo) {
                BoardCell destinationBC = (BoardCell) getChildren().get(y * size + x + 4 + size * 4);
                destinationBC.getLabel().setText(StoneGroupPtrNO + "," + StoneGroupSerialNo);
            }
        });
    }

    // TODO: (minor tweak) Immediately change lastMouseHover on completion (esp. if a situation arises where the mouse might be on the board during confirmation)
    // TODO: Although it might be said that the model should remain unchanged until confirmation, I am not sure whether this is really the responsibility of the view.
    public void confirmMove() {
        if(selectionBC != null) {
            int col = getColumnIndex(selectionBC) - 1;
            int row = getRowIndex(selectionBC) - 1;
            if(col >= 0 && row >= 0) { // Remember to account for the inclusion of labels in the grid, which could potentially be at either end.
                board.setStone(col, row, board.getCurColor(), false);
            } else {
                System.out.println("Confirmation outside of actual board on " + lastBC); // TODO: Remove in finished product
            }
        }
    }

    // Getters and Setters
    public boolean needsMoveConfirmation() {
        return needsMoveConfirmation;
    }

    public void setMoveConfirmation(boolean needsMoveConfirmation) {
        this.needsMoveConfirmation = needsMoveConfirmation;
    }

    public boolean showsMoveNumbers() {
        return showsMoveNumbers;
    }

    public void setShowsMoveNumbers(boolean showsMoveNumbers) {
        this.showsMoveNumbers = showsMoveNumbers;

        for(int i = 0; i < size; i++) {
            for(int j = 0; j < size; j++) {
                BoardCell bc = (BoardCell)getChildren().get(4 + size * 4 + i * size + j);
                bc.getLabel().setVisible(this.showsMoveNumbers);
            }
        }
    }

    public boolean showsCoordinates() {
        return showsCoordinates;
    }

    public void setShowsCoordinates(boolean showsCoordinates) {
        this.showsCoordinates = showsCoordinates;

        for(int i = 0; i < size * 4; i++) {
            BoardCell bc = (BoardCell)getChildren().get(4 + i);
            bc.getLabel().setVisible(this.showsCoordinates);
        }
    }

    public void setGraphics(String graphics) {
        if(graphics == null) {
            throw new NullPointerException();
        }

        this.graphics = graphics;

        loadGraphics(graphics);

        for(int i = 0; i < 4; i++) {
            BoardCell bc = (BoardCell)getChildren().get(i);
            bc.setBackgroundImage(corner);                  // I could just replace this with updateImages(), but this way we'll save one unnecessary method call.
        }

        for(int i = 0; i < size; i++) {
            // edges
            for(int j = 0; j < 4; j++) {
                BoardCell bc = (BoardCell)getChildren().get(4 + i * 4 + j);
                bc.setBackgroundImage(edge);               // Same as with corner
            }
            // centre
            for(int j = 0; j < size; j++) {
                BoardCell bc = (BoardCell)getChildren().get(4 + size * 4 + i * size + j);
                bc.updateImages(tile);
            }
        }
    }

    public int getSize() {
        return size;
    }

    public Board getBoard() {
        return board;
    }

    public Image getTile() {
        return tile;
    }

    public Image[] getStones() {
        return stones;
    }

    public Image getEdge() {
        return edge;
    }

    public Image getCorner() {
        return corner;
    }

    // private methods
    private void loadGraphics(String graphics) {
        try (ZipFile zip = new ZipFile(graphics)) {
            ZipEntry tileEntry = zip.getEntry("tile.png");
            ZipEntry cornerEntry = zip.getEntry("corner.png");
            ZipEntry edgeEntry = zip.getEntry("edge.png");
            ZipEntry stone0Entry = zip.getEntry("stone_0.png");
            ZipEntry stone1Entry = zip.getEntry("stone_1.png");

            if(Stream.of(tileEntry, cornerEntry, edgeEntry, stone0Entry, stone1Entry).anyMatch(Objects::isNull)) {
                throw new IllegalStateException("ERROR: Graphics pack " + graphics + " is missing files!");
            }

            try (InputStream tileIS = zip.getInputStream(tileEntry);
                 InputStream cornerIS = zip.getInputStream(cornerEntry);
                 InputStream edgeIS = zip.getInputStream(edgeEntry);
                 InputStream stone0IS = zip.getInputStream(stone0Entry);
                 InputStream stone1IS = zip.getInputStream(stone1Entry)
            ) {
                final int DEFAULT_IMAGE_SIZE = 128;
                final boolean SMOOTH_IMAGES = false;

                tile = new Image(
                        tileIS,             // is (:InputStream)
                        DEFAULT_IMAGE_SIZE, // requestedWidth
                        DEFAULT_IMAGE_SIZE, // requestedHeight
                        true,               // preserveRation
                        SMOOTH_IMAGES);     // smooth
                this.edge = new Image(edgeIS, DEFAULT_IMAGE_SIZE, DEFAULT_IMAGE_SIZE, true, SMOOTH_IMAGES);
                this.corner = new Image(cornerIS, DEFAULT_IMAGE_SIZE, DEFAULT_IMAGE_SIZE, true, SMOOTH_IMAGES);

                stones[0] = new Image(stone0IS);
                stones[1] = new Image(stone1IS);
            } catch (Exception e) {
                System.err.println("ERROR: Couldn't read file from graphics pack " + graphics + "!");
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.err.println("ERROR: Couldn't open graphics pack " + graphics + "!");
            e.printStackTrace();
        }
    }

    private class BoardCell extends StackPane {
        protected final Label LABEL;

        private BoardCell(Image tile) {
            this.setMinSize(0, 0);
            setBackgroundImage(tile);

            this.LABEL = new Label("0");
            this.LABEL.setMinSize(0, 0);
            this.LABEL.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

            final DoubleProperty FONT_SIZE = new SimpleDoubleProperty(0);
            FONT_SIZE.bind(this.LABEL.widthProperty().divide(2).subtract(Bindings.length(this.LABEL.textProperty())));
            this.LABEL.styleProperty().bind(Bindings.concat("-fx-font-size: ", FONT_SIZE));

            getChildren().add(this.LABEL);

            prefWidthProperty().bind(MAX_CELL_DIM_INT);
            prefHeightProperty().bind(MAX_CELL_DIM_INT);

            setMouseTransparent(true);
        }

        public void setBackgroundImage(Image tile) {
            BackgroundSize bgSz = new BackgroundSize(
                    100,     // width
                    100,        // height
                    true,       // widthAsPercentage
                    true,       // heightAsPercentage
                    false,      // contain
                    true);      // cover
            BackgroundImage bgImg = new BackgroundImage(tile, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, bgSz);
            this.setBackground(new Background(bgImg));
        }

        public void updateImages(Image tile) {
            setBackgroundImage(tile);
        }

        // Getters
        public Label getLabel() {
            return LABEL;
        }
    } // private class BoardCell

    private class PlayableBoardCell extends BoardCell {

        private boolean isSelected = false;
        private boolean isSet = false;

        private final ResizableImageView BLACK_HOVER;
        private final ResizableImageView WHITE_HOVER;
        private final ResizableImageView BLACK_STONE;
        private final ResizableImageView WHITE_STONE;

        private PlayableBoardCell() {
            super(tile);

            this.BLACK_HOVER = getCellImageView(stones[0]);
            getChildren().add(this.BLACK_HOVER);

            this.WHITE_HOVER = getCellImageView(stones[1]);
            getChildren().add(this.WHITE_HOVER);

            this.BLACK_STONE = getCellImageView(stones[0]);
            getChildren().add(this.BLACK_STONE);

            this.WHITE_STONE = getCellImageView(stones[1]);
            getChildren().add(this.WHITE_STONE);

            this.LABEL.setVisible(false);
            this.LABEL.setAlignment(Pos.CENTER);

            setMouseTransparent(false);
        }

        private ResizableImageView getCellImageView(Image i) {
            if(i == null) {
                throw new NullPointerException();
            }

            ResizableImageView iv = new ResizableImageView(i);
            iv.setPreserveRatio(true);
            iv.setMouseTransparent(true);
            iv.setSmooth(false);
            iv.setVisible(false);

            return iv;
        }

        public void updateImages() {
            super.updateImages(tile);

            BLACK_STONE.setImage(stones[0]);
            BLACK_HOVER.setImage(stones[0]);
            WHITE_STONE.setImage(stones[1]);
            WHITE_HOVER.setImage(stones[1]);
        }

        public void hoverWhite() {
            hover(WHITE_HOVER);
        }

        public void hoverBlack() {
            hover(BLACK_HOVER);
        }

        public void unhover() {
            if(!isSelected) {
                BLACK_HOVER.setVisible(false);
                WHITE_HOVER.setVisible(false);
            }
        }

        private void hover(ImageView iv) {
            unhover();              // might be unnecessary
            if(!isSet && !isSelected) {
                iv.setOpacity(0.5);
                iv.setVisible(true);
            }
        }

        public void selectWhite() {
            select(WHITE_HOVER);
        }

        public void selectBlack() {
            select(BLACK_HOVER);
        }

        public void deselect() {
            isSelected = false;
            unhover();
        }

        private void select(ImageView iv) {
            deselect();             // might be unnecessary
            iv.setOpacity(0.75);
            iv.setVisible(true);
            isSelected = true;
        }

        private void setWhite() {
            set(WHITE_STONE);
        }

        private void setBlack() {
            set(BLACK_STONE);
        }

        public void unset() {
            deselect();
            BLACK_STONE.setVisible(false);
            WHITE_STONE.setVisible(false);
            LABEL.setVisible(false);
            isSet = false;
        }

        private void set(ImageView iv) {
            deselect();
            iv.setVisible(true);
            if(showsMoveNumbers) {
                PixelReader p = iv.getImage().getPixelReader();
                if(p == null) {
                    throw new NullPointerException("Can't get stone color");
                }
                this.LABEL.setTextFill(p.getColor((int)(iv.getImage().getWidth() / 2), (int)(iv.getImage().getHeight() / 2)).invert());
                this.LABEL.setVisible(true);
            }
            isSet = true;
        }
    } // private class PlayableBoardCell extends BoardCell
}
