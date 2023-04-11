package pr_se.gogame.view_controller;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import pr_se.gogame.model.Board;
import pr_se.gogame.model.StoneColor;
import javafx.geometry.Pos;

public class BoardPane2 extends GridPane {

    private boolean needsMoveConfirmation = false;  // whether moves have to be confirmed separately (TODO: might need a better name)
    private boolean showsMoveNumbers = true;

    private final int SIZE;
    private Board board;

    private final Image[] tiles = new Image [2];
    private final Image[] stones = new Image [2];

    private BoardPane2.BoardCell lastBC = null;
    private BoardPane2.BoardCell selectionBC = null;

    public BoardPane2(Board board, String tile0, String tile1, String stone0, String stone1) {
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

        stones[0] = new Image(
                stone0,     // URL
                true);      // backgroundLoading
        stones[1] = new Image(stone1, true);

        // create coordinate axes
        HBox topLabels = new HBox();
        HBox bottomLabels = new HBox();
        VBox leftLabels = new VBox();
        VBox rightLabels = new VBox();

        // divvy up the available area ahead of time to avoid cyclic dependencies
        // final NumberBinding CELL_ASPECT_RATIO = Bindings.min(widthProperty().subtract(leftLabels.widthProperty()).subtract(rightLabels.widthProperty()), heightProperty().subtract(topLabels.heightProperty()).subtract(bottomLabels.heightProperty())).divide(SIZE);
        final NumberBinding MAX_CELL_WIDTH = widthProperty().subtract(widthProperty().multiply(0.1)).divide(SIZE);                                                          // Get maximum width if all cells are equally wide
        final NumberBinding MAX_CELL_WIDTH_INT = Bindings.createIntegerBinding(() -> MAX_CELL_WIDTH.intValue(), MAX_CELL_WIDTH);    // round down
        final NumberBinding MAX_CELL_HEIGHT = heightProperty().subtract(heightProperty().multiply(0.1)).divide(SIZE);                                                        // Get maximum height if all cells are equally wide
        final NumberBinding MAX_CELL_HEIGHT_INT = Bindings.createIntegerBinding(() -> MAX_CELL_HEIGHT.intValue(), MAX_CELL_HEIGHT); // round down

        final NumberBinding MAX_CELL_DIM = Bindings.min(MAX_CELL_WIDTH_INT, MAX_CELL_HEIGHT_INT);                                   // Use whatever is smaller AFTER the division
        final NumberBinding MAX_CELL_DIM_INT = Bindings.createIntegerBinding(() -> MAX_CELL_DIM.intValue(), MAX_CELL_DIM);          // round down

        final NumberBinding HORIZONTAL_COORDINATE_HEIGHT = heightProperty().subtract(MAX_CELL_DIM_INT.multiply(SIZE)).divide(2);  // Get no. unused pixels at top/bottom
        final NumberBinding VERTICAL_COORDINATE_WIDTH = widthProperty().subtract(MAX_CELL_DIM_INT.multiply(SIZE)).divide(2);      // Get no. unused pixels at left/right

        topLabels.setMinWidth(0);
        topLabels.setMinHeight(0);
        bottomLabels.setMinWidth(0);
        bottomLabels.setMinHeight(0);
        leftLabels.setMinWidth(0);
        leftLabels.setMinHeight(0);
        rightLabels.setMinWidth(0);
        rightLabels.setMinHeight(0);

        topLabels.prefHeightProperty().bind(HORIZONTAL_COORDINATE_HEIGHT);
        bottomLabels.prefHeightProperty().bind(HORIZONTAL_COORDINATE_HEIGHT);
        leftLabels.prefWidthProperty().bind(VERTICAL_COORDINATE_WIDTH);
        rightLabels.prefWidthProperty().bind(VERTICAL_COORDINATE_WIDTH);

        topLabels.maxHeightProperty().bind(HORIZONTAL_COORDINATE_HEIGHT);
        bottomLabels.maxHeightProperty().bind(HORIZONTAL_COORDINATE_HEIGHT);
        leftLabels.maxWidthProperty().bind(VERTICAL_COORDINATE_WIDTH);
        rightLabels.maxWidthProperty().bind(VERTICAL_COORDINATE_WIDTH);

        /*final NumberBinding CELL_ASPECT_RATIO = Bindings.min(widthProperty().subtract(leftLabels.widthProperty()).subtract(rightLabels.widthProperty()), heightProperty().subtract(topLabels.heightProperty()).subtract(bottomLabels.heightProperty())).divide(SIZE);
        final NumberBinding ROUNDED_CELL_ASPECT_RATIO = Bindings.createIntegerBinding(() -> CELL_ASPECT_RATIO.intValue(), CELL_ASPECT_RATIO);*/

        // populate the coordinate axes
        for(int i = 0; i < this.SIZE; i++) {
            Label t = new Label("" + (char)('A' + i));
            t.setMaxWidth(Double.MAX_VALUE);
            t.setAlignment(Pos.BOTTOM_CENTER);
            makeLabelSizeDynamic(t, topLabels.widthProperty());

            Label b = new Label("" + (char)('A' + i));
            b.setMaxWidth(Double.MAX_VALUE);
            b.setAlignment(Pos.TOP_CENTER);
            makeLabelSizeDynamic(b, bottomLabels.widthProperty());

            Label l = new Label("" + (SIZE - i));
            l.setMaxHeight(Double.MAX_VALUE);
            l.setAlignment(Pos.CENTER_RIGHT);
            makeLabelSizeDynamic(l, leftLabels.heightProperty());

            Label r = new Label("" + (SIZE - i));
            r.setMaxHeight(Double.MAX_VALUE);
            r.setAlignment(Pos.CENTER_LEFT);
            makeLabelSizeDynamic(r, rightLabels.heightProperty());

            topLabels.getChildren().add(t);
            bottomLabels.getChildren().add(b);
            leftLabels.getChildren().add(l);
            rightLabels.getChildren().add(r);

            topLabels.setHgrow(t, Priority.ALWAYS);
            bottomLabels.setHgrow(b, Priority.ALWAYS);
            leftLabels.setVgrow(l, Priority.ALWAYS);
            rightLabels.setVgrow(r, Priority.ALWAYS);
        }

        // put the coordinate axes in first to mess up the indexing as little as possible;
        topLabels.setAlignment(Pos.BOTTOM_CENTER);
        bottomLabels.setAlignment(Pos.TOP_CENTER);
        leftLabels.setAlignment(Pos.CENTER_RIGHT);
        rightLabels.setAlignment(Pos.CENTER_LEFT);

        setHgrow(topLabels, Priority.ALWAYS);
        setHgrow(bottomLabels, Priority.ALWAYS);
        setVgrow(leftLabels, Priority.ALWAYS);
        setVgrow(rightLabels, Priority.ALWAYS);

        add(topLabels, 1, 0, SIZE, 1);
        add(bottomLabels, 1, SIZE + 1, SIZE, 1);
        add(leftLabels, 0, 1, 1, SIZE);
        add(rightLabels, SIZE + 1, 1, 1, SIZE);

        // Fill the grid with alternating tiles
        for(int i = 0; i < this.SIZE; i++) {
            for(int j = 0; j < this.SIZE; j++) {
                BoardPane2.BoardCell bc = new BoardPane2.BoardCell(tiles[(j % 2 + i % 2) % 2]);
                bc.prefWidthProperty().bind(MAX_CELL_DIM);
                bc.prefHeightProperty().bind(MAX_CELL_DIM);
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

                    if (col != null && row != null && target instanceof BoardPane2.BoardCell) {
                        BoardPane2.BoardCell targetBC = (BoardPane2.BoardCell) target;
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

        // Layout of this BoardPane
        setAlignment(Pos.CENTER);
    }

    public void setBoard(Board board) {
        this.board = board;

        board.addListener(new GoListener() {
            @Override
            public void stoneSet(StoneSetEvent e) {
                BoardPane2.BoardCell destinationBC = (BoardPane2.BoardCell) getChildren().get(e.getRow() * SIZE + e.getCol() + 4);
                destinationBC.getLabel().setText("" + e.getMoveNumber());

                if(e.getColor() == StoneColor.BLACK) {
                    destinationBC.setBlack();
                } else {
                    destinationBC.setWhite();
                }
            }

            @Override
            public void stoneRemoved(StoneRemovedEvent e) {
                BoardPane2.BoardCell destinationBC = (BoardPane2.BoardCell) getChildren().get(e.getRow() * SIZE + e.getCol() + 4);

                destinationBC.unset();
            }

            @Override
            public void debugInfoRequested(int x, int y, int StoneGroupPtrNO, int StoneGroupSerialNo) {
                BoardPane2.BoardCell destinationBC = (BoardPane2.BoardCell) getChildren().get(y * SIZE + x);
                destinationBC.getLabel().setText(StoneGroupPtrNO + "," + StoneGroupSerialNo);
            }
        });
    }

    // TODO: Call this from the main UI if moves are to be confirmed.
    // TODO: (minor tweak) Immediately change lastMouseHover on completion (esp. if a situation arises where the mouse might be on the board during confirmation)
    // TODO: Although it might be said that the model should remain unchanged until confirmation, I am not sure whether this is really the responsibility of the view.
    public void confirmMove() {
        if(selectionBC != null) {
            Integer col = getColumnIndex(selectionBC) - 1;
            Integer row = getRowIndex(selectionBC) - 1;
            if(col != null && row != null) { // Remember to account for the inclusion of labels in the grid, which could potentially be at either end.
                board.setStone(col, row, board.getCurColor());
            } else {
                System.out.println("Confirmation outside of actual board on " + lastBC); // TODO: Remove in finished product
            }
        }
    }

    // overridden parent methods
    /*@Override
    public void resize(double width, double height) {
        width -= width % SIZE;
        height -= height % SIZE;

        double dim = Math.min(width, height);

        super.resize(dim, dim);

        /*double myWidth = getChildren().get(0).getLayoutBounds().getWidth() * SIZE;
        double myHeight = getChildren().get(0).getLayoutBounds().getHeight() * SIZE;*/

        /*setMaxWidth(Math.min(myWidth, myHeight));
        setMaxHeight(Math.min(myWidth, myHeight));*/
    // }

    private void makeLabelSizeDynamic(Label l, ReadOnlyDoubleProperty dimProperty) {
        final DoubleProperty FONT_SIZE = new SimpleDoubleProperty(0);
        FONT_SIZE.bind(dimProperty.divide(100.0 / (this.SIZE * 2.5)).divide(this.SIZE).subtract(Bindings.length(l.textProperty())));
        l.styleProperty().bind(Bindings.concat("-fx-font-size: ", FONT_SIZE));
    }

    private class BoardCell extends StackPane {
        private final ResizableImageView BLACK_HOVER;
        private final ResizableImageView WHITE_HOVER;
        private final ResizableImageView BLACK_STONE;
        private final ResizableImageView WHITE_STONE;
        private final Label LABEL;

        private boolean isSelected = false;
        private boolean isSet = false;

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
            this.setBackground(new Background(bgImg));

            this.BLACK_HOVER = getCellImageView(stones[0]);
            getChildren().add(this.BLACK_HOVER);

            this.WHITE_HOVER = getCellImageView(stones[1]);
            getChildren().add(this.WHITE_HOVER);

            this.BLACK_STONE = getCellImageView(stones[0]);
            getChildren().add(this.BLACK_STONE);

            this.WHITE_STONE = getCellImageView(stones[1]);
            getChildren().add(this.WHITE_STONE);

            this.LABEL = new Label("0");
            this.LABEL.setVisible(false);
            this.LABEL.setMinSize(0, 0);

            final DoubleProperty FONT_SIZE = new SimpleDoubleProperty(0);
            FONT_SIZE.bind(BLACK_STONE.fitWidthProperty().divide(2).subtract(Bindings.length(this.LABEL.textProperty()))); // Binding it to widthProperty() of the BoardCell causes the numbers to flicker
            this.LABEL.styleProperty().bind(Bindings.concat("-fx-font-size: ", FONT_SIZE));

            getChildren().add(this.LABEL);
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
