package pr_se.gogame.view_controller;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import pr_se.gogame.model.Board;
import pr_se.gogame.model.StoneColor;

/**
 * View/Controller
 * Go Board that uses image files for its (checkerboard) tiles and stones
 */
public class BoardPane extends GridPane {

    private final int SIZE;                         // number of columns and rows, respectively
    private boolean needsMoveConfirmation = false;  // whether moves have to be confirmed separately (TODO: might need a better name)

    private boolean showsMoveNumbers = true;        // whether move numbers are shown
    private Board board;                            // Model for MVC-adherence; Will likely be replaced with Game

    /*
     * Custom resources
     */
    private final Image[] tiles = new Image [2];
    private final Image[] stones = new Image [2];
    private BoardCell lastBC = null;
    private BoardCell selectionBC = null;

    private class BoardCell extends StackPane {
        private final ImageView TILE;
        private final ImageView BLACK_HOVER;
        private final ImageView WHITE_HOVER;
        private final ImageView BLACK_STONE;
        private final ImageView WHITE_STONE;
        private final Label LABEL;

        private boolean isSelected = false;

        private BoardCell(Image tile) {
            this.setMinSize(0, 0);

            BackgroundSize bgSz = new BackgroundSize(
                    100,     // width
                    100,        // height
                    true,       // widthAsPercentage
                    true,       // heightAsPercentage
                    false,      // contain
                    true);      // cover
            BackgroundImage bgImg = new BackgroundImage(tile, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, bgSz);
            BackgroundImage bgImg2 = new BackgroundImage(stones[0], BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, bgSz);
            this.setBackground(new Background(bgImg));
            this.TILE = null;

            this.BLACK_HOVER = getCellImageView(stones[0]);
            this.BLACK_HOVER.setVisible(false);
            getChildren().add(this.BLACK_HOVER);

            this.WHITE_HOVER = getCellImageView(stones[1]);
            this.WHITE_HOVER.setVisible(false);
            getChildren().add(this.WHITE_HOVER);

            this.BLACK_STONE = getCellImageView(stones[0]);
            this.BLACK_STONE.setVisible(false);
            getChildren().add(this.BLACK_STONE);

            this.WHITE_STONE = getCellImageView(stones[1]);
            this.WHITE_STONE.setVisible(false);
            getChildren().add(this.WHITE_STONE);

            this.LABEL = new Label("0");
            this.LABEL.setVisible(false);
            setMargin(this.LABEL, new Insets(0, 0, 0, 0));
            this.LABEL.setMinSize(0, 0);
            final DoubleProperty FONT_SIZE = new SimpleDoubleProperty(0);
            FONT_SIZE.bind(BLACK_STONE.fitWidthProperty().divide(4).subtract(Bindings.length(this.LABEL.textProperty())));
            this.LABEL.styleProperty().bind(Bindings.concat("-fx-font-size: ", FONT_SIZE));
            getChildren().add(this.LABEL);
        }

        private ImageView getCellImageView(Image i) {
            if(i == null) {
                throw new NullPointerException();
            }

            ImageView iv = new ImageView(i);
            iv.setPreserveRatio(true);

            final NumberBinding CELL_ASPECT_RATIO = Bindings.min(BoardPane.this.widthProperty(), BoardPane.this.heightProperty()).divide(SIZE);
            final NumberBinding ROUNDED_CELL_ASPECT_RATIO = Bindings.createIntegerBinding(() -> CELL_ASPECT_RATIO.intValue(), CELL_ASPECT_RATIO);
            iv.fitHeightProperty().bind(ROUNDED_CELL_ASPECT_RATIO);
            iv.fitWidthProperty().bind(ROUNDED_CELL_ASPECT_RATIO);
            iv.setMouseTransparent(true);
            iv.setSmooth(false);
            setMargin(iv, new Insets(0, 0, 0, 0));

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
            unhover(); // might be unnecessary
            iv.setOpacity(0.5);
            iv.setVisible(true);
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
            // maybe deselect() first? See hover().
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
        }

        private void set(ImageView iv) {
            iv.setVisible(true);
        }

        // Getters

        public ImageView getTile() {
            return TILE;
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
    }

    // TODO: Maybe move constructor content into an init() method, especially with regards to loading images and even the board (as those might be changed during a game).
    public BoardPane(Board board, String tile0, String tile1, String stone0, String stone1) {
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
                SMOOTH_IMAGES,     // smooth
                true);              // backgroundLoading
        tiles[1] = new Image(tile1, DEFAULT_IMAGE_SIZE, DEFAULT_IMAGE_SIZE, true, SMOOTH_IMAGES, true);

        stones[0] = new Image(
                stone0,     // URL
                true);      // backgroundLoading
        stones[1] = new Image(stone1, true);

        // Graphical details of this board pane
        setBackground(new Background(new BackgroundFill(Color.RED, null, null)));
        setAlignment(Pos.CENTER);
        setHgap(0);
        setVgap(0);
        setPadding(new Insets(0, 0, 0, 0));
        setMinSize(0, 0);

        // setGridLinesVisible(true);

        // Fill the grid with BoardCells [of alternating tiles]
        for(int i = 0; i < this.SIZE; i++) {
            for(int j = 0; j < this.SIZE; j++) {
                BoardCell bc = new BoardCell(tiles[(j % 2 + i % 2) % 2]);
                add(bc, j, i);
                setMargin(bc, new Insets(0, 0, 0, 0));
                setHalignment(bc, HPos.LEFT);
                setValignment(bc, VPos.TOP);
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

                    if (col != null && row != null) {
                        BoardCell targetBC = (BoardCell) target;
                        // this.board.printDebugInfo(col, row);
                        /*System.out.println("GridPane size: " + widthProperty().get() + "/" + heightProperty().get());
                        System.out.println("BoardCell size: " + targetBC.getWidth() + "/" + targetBC.getHeight());
                        System.out.println("Black Stone size: " + targetBC.getBlackStone().getFitWidth() + "/" + targetBC.getBlackStone().getFitHeight());*/

                        // System.out.println("Hover over " + col + " " + row);    // TODO: Remove in finished product
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
                        //System.out.println("Hover target is not a cell!");  // TODO: Remove in finished product
                        lastBC.unhover();
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


    }

    public void setBoard(Board board) {
        this.board = board;

        board.addListener(new GoListener() {
            @Override
            public void stoneSet(StoneSetEvent e) {
                BoardCell destinationBC = (BoardCell) getChildren().get(e.getRow() * SIZE + e.getCol());
                ImageView iv = destinationBC.getBlackStone();
                Color labelColor = Color.rgb(255, 255, 255);
                if(e.getColor() != StoneColor.BLACK) {
                    iv = destinationBC.getWhiteStone();
                    labelColor = Color.rgb(0, 0, 0);
                }

                /*if(selectionHover != null) {
                    selectionHover.setVisible(false);
                }*/

                iv.setVisible(true);

                if(showsMoveNumbers) {
                    Label l = destinationBC.getLabel();
                    l.setText("" + e.getMoveNumber());
                    l.setTextFill(labelColor);
                    l.toFront();
                    l.setVisible(true);
                }
            }

            @Override
            public void stoneRemoved(StoneRemovedEvent e) {
                BoardCell destinationBC = (BoardCell) getChildren().get(e.getRow() * SIZE + e.getCol());

                destinationBC.unset();
            }

            @Override
            public void debugInfoRequested(int x, int y, int StoneGroupPtrNO, int StoneGroupSerialNo) {
                BoardCell destinationBC = (BoardCell) getChildren().get(y * SIZE + x);
                destinationBC.getLabel().setText(StoneGroupPtrNO + "," + StoneGroupSerialNo);
            }
        });
    }

    // TODO: Call this from the main UI if moves are to be confirmed.
    // TODO: Immediately change lastMouseHover on completion (esp. if a situation arises where the mouse might be on the board during confirmation)
    // TODO: Although it might be said that the model should remain unchanged until confirmation, I am not sure whether this is really the responsibility of the view.
    public void confirmMove() {
        if(selectionBC != null) {
            Integer col = getColumnIndex(selectionBC);
            Integer row = getRowIndex(selectionBC);
            if(col != null && row != null) { // Remember to account for the inclusion of labels in the grid, which could potentially be at either end.
                board.setStone(col, row, board.getCurColor());
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
    }

    public boolean showsCoordinates() {
        return false;
        // TODO: Implement
    }

    public void setShowsCoordinates() {
        // TODO: Implement
    }
}
