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
import pr_se.gogame.model.Board;
import pr_se.gogame.model.StoneColor;

/**
 * View/Controller
 * Board that uses image files for its tiles and stones
 */
public class BoardPane extends GridPane {

    private boolean needsMoveConfirmation = false;  // whether moves have to be confirmed separately (TODO: might need a better name)
    private boolean showsMoveNumbers = true;
    private boolean showsCoordinates = true;

    private final int SIZE;
    private Board board;

    private final Image[] tiles = new Image [2];
    private final Image[] stones = new Image [2];
    private final Image edge;
    private final Image corner;

    private BoardCell lastBC = null;
    private BoardCell selectionBC = null;

    private final NumberBinding MAX_CELL_DIM_INT;

    public BoardPane(Board board, String tile0, String tile1, String edge, String corner, String stone0, String stone1) {
        setBoard(board);
        this.SIZE = board.getSize();

        // TODO: In the end product, the files would be chosen by the user (and perhaps packaged in an archive)
        final int DEFAULT_IMAGE_SIZE = 128;
        final boolean SMOOTH_IMAGES = false;

        tiles[0] = new Image(
                tile0,              // URL
                DEFAULT_IMAGE_SIZE, // requestedWidth
                DEFAULT_IMAGE_SIZE, // requestedHeight
                true,               // preserveRation
                SMOOTH_IMAGES,      // smooth
                true);              // backgroundLoading
        tiles[1] = new Image(tile1, DEFAULT_IMAGE_SIZE, DEFAULT_IMAGE_SIZE, true, SMOOTH_IMAGES, true);
        this.edge = new Image(edge, DEFAULT_IMAGE_SIZE, DEFAULT_IMAGE_SIZE, true, SMOOTH_IMAGES, true);
        this.corner = new Image(corner, DEFAULT_IMAGE_SIZE, DEFAULT_IMAGE_SIZE, true, SMOOTH_IMAGES, true);

        stones[0] = new Image(
                stone0,     // URL
                true);      // backgroundLoading
        stones[1] = new Image(stone1, true);

        // determine cell size
        final NumberBinding MAX_CELL_WIDTH = widthProperty().divide(SIZE + 2);                                                 // Get maximum width if all cells are equally wide
        final NumberBinding MAX_CELL_WIDTH_INT = Bindings.createIntegerBinding(MAX_CELL_WIDTH::intValue, MAX_CELL_WIDTH);        // round down
        final NumberBinding MAX_CELL_HEIGHT = heightProperty().divide(SIZE + 2);                                              // Get maximum height if all cells are equally wide
        final NumberBinding MAX_CELL_HEIGHT_INT = Bindings.createIntegerBinding(MAX_CELL_HEIGHT::intValue, MAX_CELL_HEIGHT);    // round down

        final NumberBinding MAX_CELL_DIM = Bindings.min(MAX_CELL_WIDTH_INT, MAX_CELL_HEIGHT_INT);                               // Use whatever is smaller after the division
        MAX_CELL_DIM_INT = Bindings.createIntegerBinding(MAX_CELL_DIM::intValue, MAX_CELL_DIM);                                 // round down

        // put the axes' corners in first to mess up the indexing as little as possible;
        BoardCell corner1 = new BoardCell(this.corner, false);
        add(corner1, 0, 0);
        BoardCell corner2 = new BoardCell(this.corner, false);
        add(corner2, SIZE + 1, 0);
        BoardCell corner3 = new BoardCell(this.corner, false);
        add(corner3, 0, SIZE + 1);
        BoardCell corner4 = new BoardCell(this.corner, false);
        add(corner4, SIZE + 1, SIZE + 1);
        
        // populate the coordinate axes
        for(int i = 0; i < this.SIZE; i++) {
            // top
            BoardCell t = new BoardCell(this.edge, false);
            t.getLabel().setText("" + (char)('A' + i));
            t.getLabel().setAlignment(Pos.BOTTOM_CENTER);
            add(t, i + 1, 0);

            // bottom
            BoardCell b = new BoardCell(this.edge, false);
            b.getLabel().setText("" + (char)('A' + i));
            b.getLabel().setAlignment(Pos.TOP_CENTER);
            add(b, i + 1, SIZE + 1);

            // left
            BoardCell l = new BoardCell(this.edge, false);
            l.getLabel().setText("" + (SIZE - i));
            l.getLabel().setAlignment(Pos.CENTER_RIGHT);
            add(l, 0, i + 1);

            // right
            BoardCell r = new BoardCell(this.edge, false);
            r.getLabel().setText("" + (SIZE - i));
            r.getLabel().setAlignment(Pos.CENTER_LEFT);
            add(r, SIZE + 1, i + 1);
        }

        // Fill the grid with alternating tiles
        for(int i = 0; i < this.SIZE; i++) {
            for(int j = 0; j < this.SIZE; j++) {
                BoardCell bc = new BoardCell(tiles[(j % 2 + i % 2) % 2], true);
                add(bc, j + 1, i + 1);
            }
        }

        // Set up listeners
        setOnMouseMoved(e -> {
            Node target = (Node)e.getTarget();
            // System.out.println("Target is of class " + target.getClass());   // TODO: Remove in finished product
            if(target != null) {
                if(target != lastBC) {                                 // TODO: This seems to fire a bit too readily, making the program run less efficiently. I am not sure why, though.
                    Integer col = getColumnIndex(target);
                    Integer row = getRowIndex(target);

                    if (col != null && row != null && target instanceof BoardCell targetBC && targetBC.isPlayable()) {   // IntelliJ suggested the "instanceof BoardCell targetBC"; it's called a "pattern variable"
                        // printDebugInfo();

                        if (this.board.getCurColor() == StoneColor.BLACK) {
                            targetBC.hoverBlack();
                        } else {
                            targetBC.hoverWhite();
                        }

                        // Remove old hover                                    // TODO: Remove in finished product
                        if(lastBC != null) {
                            lastBC.unhover();
                        }
                        //System.out.println("Removed hover!");               // TODO: Remove in finished product
                        lastBC = targetBC;
                    } else if(lastBC != null) {
                        if(lastBC.isPlayable()) {
                            lastBC.unhover();
                        }
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
                    if(board.getCurColor() == StoneColor.BLACK) {
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
                BoardCell destinationBC = (BoardCell)getChildren().get(e.getRow() * SIZE + e.getCol() + 4 + SIZE * 4);
                destinationBC.getLabel().setText("" + e.getMoveNumber());

                if(e.getColor() == StoneColor.BLACK) {
                    destinationBC.setBlack();
                } else {
                    destinationBC.setWhite();
                }
            }

            @Override
            public void stoneRemoved(StoneRemovedEvent e) {
                BoardCell destinationBC = (BoardCell)getChildren().get(e.getRow() * SIZE + e.getCol() + 4 + SIZE * 4);

                destinationBC.unset();
            }

            @Override
            public void debugInfoRequested(int x, int y, int StoneGroupPtrNO, int StoneGroupSerialNo) {
                BoardCell destinationBC = (BoardCell) getChildren().get(y * SIZE + x + 4 + SIZE * 4);
                destinationBC.getLabel().setText(StoneGroupPtrNO + "," + StoneGroupSerialNo);
            }
        });
    }

    // TODO: Call this from the main UI if moves are to be confirmed.
    // TODO: (minor tweak) Immediately change lastMouseHover on completion (esp. if a situation arises where the mouse might be on the board during confirmation)
    // TODO: Although it might be said that the model should remain unchanged until confirmation, I am not sure whether this is really the responsibility of the view.
    public void confirmMove() {
        if(selectionBC != null) {
            int col = getColumnIndex(selectionBC) - 1;
            int row = getRowIndex(selectionBC) - 1;
            if(col >= 0 && row >= 0) { // Remember to account for the inclusion of labels in the grid, which could potentially be at either end.
                board.setStone(col, row, board.getCurColor());
            } else {
                System.out.println("Confirmation outside of actual board on " + lastBC); // TODO: Remove in finished product
            }
        }
    }

    // TODO: Remove in finished product
    public void printDebugInfo() {
        BoardCell targetBC = (BoardCell)getChildren().get(4 + SIZE * 4);
        //this.board.printDebugInfo(col, row);
        System.out.println("width/height: " + getWidth() + "/" + getHeight());
        //System.out.println("prefWidth/prefHeight: " + getPrefWidth() + "/" + getPrefHeight());
        //System.out.println("MinWidth/Height: " + getMinWidth() + "/" + getMinHeight());
        System.out.println("BoardCell size: " + targetBC.getWidth() + "/" + targetBC.getHeight());
        //System.out.println("Cell bounds in local: " + targetBC.getBoundsInLocal());
        System.out.println("Cell bounds in parent: " + targetBC.getBoundsInParent());

        //System.out.println("Hover over " + col + " " + row);
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
    }

    public boolean showsCoordinates() {
        return showsCoordinates;
    }

    public void setShowsCoordinates(boolean showsCoordinates) {
        this.showsCoordinates = showsCoordinates;
    }

    public int getSIZE() {
        return SIZE;
    }

    public Board getBoard() {
        return board;
    }

    public Image[] getTiles() {
        return tiles;
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

    private class BoardCell extends StackPane {
        private final ResizableImageView BLACK_HOVER;
        private final ResizableImageView WHITE_HOVER;
        private final ResizableImageView BLACK_STONE;
        private final ResizableImageView WHITE_STONE;
        private final Label LABEL;

        private final boolean isPlayable;
        private boolean isSelected = false;
        private boolean isSet = false;

        private BoardCell(Image tile, boolean isPlayable) {
            this.isPlayable = isPlayable;

            this.setMinSize(0, 0);

            BackgroundSize bgSz = new BackgroundSize(
                    100,     // width
                    100,        // height
                    true,       // widthAsPercentage
                    true,       // heightAsPercentage
                    false,      // contain
                    true);      // cover
            BackgroundImage bgImg = new BackgroundImage(tile, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, bgSz);
            this.setBackground(new Background(bgImg));

            //if(isPlayable) {
                this.BLACK_HOVER = getCellImageView(stones[0]);
                getChildren().add(this.BLACK_HOVER);

                this.WHITE_HOVER = getCellImageView(stones[1]);
                getChildren().add(this.WHITE_HOVER);

                this.BLACK_STONE = getCellImageView(stones[0]);
                getChildren().add(this.BLACK_STONE);

                this.WHITE_STONE = getCellImageView(stones[1]);
                getChildren().add(this.WHITE_STONE);
            /*} else {
                this.BLACK_HOVER = null;
                this.WHITE_HOVER = null;
                this.BLACK_STONE = null;
                this.WHITE_STONE = null;
            }*/

            this.LABEL = new Label("0");
            if(isPlayable) {
                this.LABEL.setVisible(false);
            }
            this.LABEL.setMinSize(0, 0);
            this.LABEL.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            this.LABEL.setAlignment(Pos.CENTER);

            final DoubleProperty FONT_SIZE = new SimpleDoubleProperty(0);
            FONT_SIZE.bind(BLACK_STONE.fitWidthProperty().divide(2).subtract(Bindings.length(this.LABEL.textProperty()))); // Binding it to [this.]widthProperty() of the BoardCell causes the numbers to flicker
            this.LABEL.styleProperty().bind(Bindings.concat("-fx-font-size: ", FONT_SIZE));

            getChildren().add(this.LABEL);

            prefWidthProperty().bind(MAX_CELL_DIM_INT);
            prefHeightProperty().bind(MAX_CELL_DIM_INT);
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

        // Getters
        public boolean isPlayable() {
            return isPlayable;
        }

        public ImageView getBlackHover() {
            return BLACK_HOVER;
        }

        public ImageView getWhiteHover() {
            return WHITE_HOVER;
        }

        public ImageView getBlackStone() {
            return BLACK_STONE;
        }

        public ImageView getWhiteStone() {
            return WHITE_STONE;
        }

        public Label getLabel() {
            return LABEL;
        }
    } // private class BoardCell
}
