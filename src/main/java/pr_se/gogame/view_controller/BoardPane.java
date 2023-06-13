package pr_se.gogame.view_controller;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import pr_se.gogame.model.Game;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static pr_se.gogame.model.StoneColor.BLACK;
import static pr_se.gogame.model.StoneColor.WHITE;

/**
 * View/Controller
 * Go Board graphical representation that uses image files for its tiles and stones
 */
public class BoardPane extends GridPane {
    /**
     * whether moves have to be confirmed separately, rather than immediately played
     */
    private boolean needsMoveConfirmation;

    /**
     * whether move numbers are shown on the stones
     */
    private boolean showsMoveNumbers;

    /**
     * whether coordinates are shown on the sides of the board
     */
    private boolean showsCoordinates;

    /**
     * Number of PLAYABLE rows and columns of this board. Does not include the coordinate axes.
     */
    private int size;

    /**
     * the game that is being displayed by this BoardPane
     */
    private final Game game;

    // Custom resources
    /**
     * Absolute path of the graphics pack zip-file
     */
    private String graphicsPath;
    
    /**
     * Background Image (not to be confused with BackgroundImage) for inner playable BoardCells
     */
    private Image tile;

    /**
     * Background Image (not to be confused with BackgroundImage) for outer edge playable BoardCells
     */
    private Image tileEdge;

    /**
     * Background Image (not to be confused with BackgroundImage) for outer corner playable BoardCells
     */
    private Image tileCorner;


    /**
     * Image used for the black and white stones
     */
    private final Image[] stones = new Image [2];

    /**
     * Image used for circle marks
     */
    private final Image[] circleMarks = new Image[3];

    /**
     * Image used for triangle marks
     */
    private final Image[] triangleMarks = new Image[3];

    /**
     * Image used for square marks
     */
    private final Image[] squareMarks = new Image[3];

    /**
     * Background Image (not to be confused with BackgroundImage) for the BoardPane's outer edges
     */
    private Image outerEdge;
    /**
     * Background Image (not to be confused with BackgroundImage) for the BoardPane's outer corners
     */
    private Image outerCorner;

    /**
     * Image for handicap slots
     */
    private Image handicapSlot;

    /**
     * the currently selected PlayableBoardCell
     */
    private PlayableBoardCell selectionPBC = null;

    /**
     * the currently hovered PlayableBoardCell (only necessary for keyboard controls)
     */
    private PlayableBoardCell hoverPBC = null;

    /**
     * NumberBinding for the width and height of all BoardCells
     */
    private NumberBinding MAX_CELL_DIM_INT;

    /**
     * X index for keyboard controls
     */
    private int keyboardCellX = -1;

    /**
     * Y index for keyboard controls
     */
    private int keyboardCellY = -1;

    // TODO: Remove in final product (or maybe not)
    private final boolean debug = false;

    /**
     *
     * @param game the game that is to be displayed by this BoardPane
     */
    public BoardPane(Game game) {
        if(game == null) {
            throw new NullPointerException();
        }

        setMouseTransparent(true);

        this.game = game;
        this.graphicsPath = GlobalSettings.getGraphicsPath();
        this.showsMoveNumbers = GlobalSettings.isShowMoveNumbers();
        this.showsCoordinates = GlobalSettings.isShowCoordinates();
        this.needsMoveConfirmation = GlobalSettings.isConfirmationNeeded();

        GlobalSettings.addListener(new ViewListener() {
            @Override
            public void fire() {
                setMoveConfirmation(GlobalSettings.isConfirmationNeeded());
                setShowsCoordinates(GlobalSettings.isShowCoordinates());
                setShowsMoveNumbers(GlobalSettings.isShowMoveNumbers());
                setGraphicsPath(GlobalSettings.getGraphicsPath());
            }
        });

        game.addListener(e -> {
            if(e == null) {
                throw new NullPointerException();
            }

            switch(e.getGameCommand()) {
                case STONE_WAS_SET:
                    StoneEvent sse = (StoneEvent) e;
                    System.out.println("Stone was set at " + sse.getX() + "/" + sse.getY());
                    PlayableBoardCell destinationBC = getPlayableCell(sse.getX(), sse.getY());
                    destinationBC.getLabel().setText("" + sse.getMoveNumber());

                    if (sse.getColor() == BLACK) {
                        destinationBC.setBlack();
                    } else if(sse.getColor() == WHITE) {
                        destinationBC.setWhite();
                    }

                    if(game.getHandicapStoneCounter() >= 0) {
                        destinationBC.showHandicapSlot();
                        destinationBC.getLabel().setVisible(false);
                    }
                    break;

                case CONFIRM_CHOICE:
                    confirmMove();
                    break;

                case STONE_WAS_CAPTURED:
                    StoneEvent se = (StoneEvent) e;
                    System.out.println("Boardpane removing stone removed at " + se.getX() + "/" + se.getY());
                    getPlayableCell(se.getX(), se.getY()).unset();
                    break;

                case NEW_GAME:
                    setMouseTransparent(false);
                    init();
                    break;

                case INIT:
                case GAME_WON:
                    setMouseTransparent(true);
                    break;

                case DEBUG_INFO:
                    DebugEvent de = (DebugEvent) e;
                    getPlayableCell(de.getX(), de.getY()).getLabel().setText(de.getPtrNo() + "," + de.getGroupNo());
                    break;

                default:
                    break;
            }

        });

        loadGraphics(graphicsPath);
        init();
    }

    /**
     * (Re-)Initialises this BoardPane for a new game. Called automatically by the constructor. Use this to start
     * a new game, instead of creating a new BoardPane. This does not reload the graphics pack - use setGraphics()
     * instead.
     */
    private void init() {
        getChildren().clear();

        this.size = this.game.getSize();

        // determine cell size
        final NumberBinding MAX_CELL_DIM = Bindings.min(
            widthProperty().divide(size + 2),
            heightProperty().divide(size + 2)
        );
        MAX_CELL_DIM_INT = Bindings.createIntegerBinding(MAX_CELL_DIM::intValue, MAX_CELL_DIM); // round down

        // put the axes' corners in first to mess up the indexing as little as possible;
        BoardCell corner1 = new BoardCell(this.outerCorner);
        corner1.getLabel().setVisible(false);
        add(corner1, 0, 0);

        BoardCell corner2 = new BoardCell(this.outerCorner);
        corner2.getLabel().setVisible(false);
        add(corner2, size + 1, 0);

        BoardCell corner3 = new BoardCell(this.outerCorner);
        corner3.getLabel().setVisible(false);
        add(corner3, size + 1, size + 1);

        BoardCell corner4 = new BoardCell(this.outerCorner);
        corner4.getLabel().setVisible(false);
        add(corner4, 0, size + 1);

        // populate the coordinate axes
        for (int i = 0; i < this.size; i++) {
            // top
            BoardCell t = new BoardCell(this.outerEdge);
            t.getLabel().setText("" + (char)('A' + i));
            t.getLabel().setAlignment(Pos.CENTER);
            add(t, i + 1, 0);

            // right
            BoardCell r = new BoardCell(this.outerEdge);
            r.getLabel().setText("" + (size - i));
            r.getLabel().setAlignment(Pos.CENTER);
            add(r, size + 1, i + 1);

            // bottom
            BoardCell b = new BoardCell(this.outerEdge);
            b.getLabel().setText("" + (char)('A' + i));
            b.getLabel().setAlignment(Pos.CENTER);
            add(b, i + 1, size + 1);

            // left
            BoardCell l = new BoardCell(this.outerEdge);
            l.getLabel().setText("" + (size - i));
            l.getLabel().setAlignment(Pos.CENTER);
            add(l, 0, i + 1);
        }

        // Fill the grid with tiles
        for(int i = 0; i < this.size; i++) {
            for(int j = 0; j < this.size; j++) {
                PlayableBoardCell bc = new PlayableBoardCell(this.tile);
                add(bc, j + 1, i + 1);
            }
        }

        updateGraphics();

        // Set up listeners
        // If this is active, dragging from within this BoardPane but outside the actual playable board works (might be desirable)
        /*setOnDragDetected((e) -> {
            startFullDrag();
        });*/

        setOnKeyPressed((e) -> {
            if(keyboardCellX < 0 || keyboardCellY < 0) {
                if(selectionPBC == null) {
                    keyboardCellX = 0;
                    keyboardCellY = 0;
                } else {
                    keyboardCellX = getColumnIndex(selectionPBC) - 1;
                    keyboardCellY = getRowIndex(selectionPBC) - 1;
                }
            }

            boolean hasMoved = false;

            switch(e.getCode()) {
                case W:
                case I:
                case UP:
                    if(keyboardCellY > 0) {
                        keyboardCellY--;
                    }
                    hasMoved = true;
                    break;

                case S:
                case K:
                case DOWN:
                    if(keyboardCellY < size - 1) {
                        keyboardCellY++;
                    }
                    hasMoved = true;
                    break;

                case A:
                case J:
                case LEFT:
                    if(keyboardCellX > 0) {
                        keyboardCellX--;
                    }
                    hasMoved = true;
                    break;

                case D:
                case L:
                case RIGHT:
                    if(keyboardCellX < size - 1) {
                        keyboardCellX++;
                    }
                    hasMoved = true;
                    break;

                case SPACE:
                    if(hoverPBC != null) {
                        hoverPBC.select();
                    }
                    break;

                case ENTER:
                    confirmMove();
                    break;

                default: break;
            }

            if(hasMoved) {
                if(hoverPBC != null && hoverPBC != selectionPBC) {
                    hoverPBC.unhover();
                }
                hoverPBC = getPlayableCell(keyboardCellX, keyboardCellY);
                hoverPBC.hover();
            }
        });


        // Layout of this BoardPane
        setAlignment(Pos.CENTER);
        // this.setPadding(new Insets(7.5,7.5,7.5,5.5)); No, don't to that, it breaks the cells' aspect ratio (even equal insets on all four sides will)
        requestFocus();
    }

    /**
     * If moves are to be confirmed, calling this method confirms a move on the currently selected PlayableBoardCell,
     * calling the game's playMove() method.
     */
    public void confirmMove() {
        if(selectionPBC != null) {
            int col = getColumnIndex(selectionPBC) - 1;
            int row = getRowIndex(selectionPBC) - 1;
            if(col >= 0 && row >= 0) {
                if(game.getHandicapStoneCounter() < 0) {
                    game.playMove(col, row);
                } else {
                    game.placeHandicapPosition(col, row, true);
                }
            } else {
                System.out.println("Confirmation outside of actual board on " + selectionPBC); // TODO: Remove in finished product
            }

            if(debug) {
                game.printDebugInfo(col, row);
            }

            /*
             * This part is necessary to ensure that, once a stone has been set, keyboard controls don't originate from
             * it (as they would if it were still selected). Note that "selectionPBC = null;" must not be moved into
             * the deselect() method; otherwise, previous selections will no longer be cleaned up when clicking
             * about, as there is no pointer.
             */
            selectionPBC.deselect();
            selectionPBC = null;

            if(hoverPBC != null) {
                if (game.getCurColor() == BLACK) {
                    hoverPBC.hoverBlack();
                } else {
                    hoverPBC.hoverWhite();
                }
            }

            /*
             * Disable the following lines if you want CONSECUTIVE keyboard controls to originate from the last placed
             * stone.
             */
            keyboardCellX = -1;
            keyboardCellY = -1;
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
        System.out.println(showsMoveNumbers);
        for(int i = 0; i < size; i++) {
            for(int j = 0; j < size; j++) {
                PlayableBoardCell pbc = getPlayableCell(j, i);
                if(!pbc.getLabel().getText().startsWith("0")) {
                    pbc.showMoveNumber();
                }
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

    public void setGraphicsPath(String graphicsPath) {
        if(graphicsPath == null) {
            throw new NullPointerException();
        }

        if(this.graphicsPath.equals(graphicsPath)) {
            return; // Don't unnecessarily reload the entire graphics pack.
        }

        this.graphicsPath = graphicsPath;

        loadGraphics(graphicsPath);

        updateGraphics();
    }

    public int getSize() {
        return size;
    }

    // private methods
    /**
     * loads the image files from the specified graphics pack into memory
     * @param graphicsPath the absolute path of the graphics pack to be loaded
     */
    private void loadGraphics(String graphicsPath) {
        try (ZipFile zip = new ZipFile(graphicsPath)) {
            tile = loadImageFromGraphicsPack("tile.png", zip);
            tileCorner = loadImageFromGraphicsPack("tile_corner.png", zip);
            tileEdge = loadImageFromGraphicsPack("tile_edge.png", zip);
            outerCorner = loadImageFromGraphicsPack("outer_corner.png", zip);
            outerEdge = loadImageFromGraphicsPack("outer_edge.png", zip);
            stones[0] = loadImageFromGraphicsPack("stone_0.png", zip);
            stones[1] = loadImageFromGraphicsPack("stone_1.png", zip);
            circleMarks[0] = loadImageFromGraphicsPack("mark_circle_0.png", zip);
            circleMarks[1] = loadImageFromGraphicsPack("mark_circle_1.png", zip);
            circleMarks[2] = loadImageFromGraphicsPack("mark_circle_2.png", zip);
            triangleMarks[0] = loadImageFromGraphicsPack("mark_triangle_0.png", zip);
            triangleMarks[1] = loadImageFromGraphicsPack("mark_triangle_1.png", zip);
            triangleMarks[2] = loadImageFromGraphicsPack("mark_triangle_2.png", zip);
            squareMarks[0] = loadImageFromGraphicsPack("mark_square_0.png", zip);
            squareMarks[1] = loadImageFromGraphicsPack("mark_square_1.png", zip);
            squareMarks[2] = loadImageFromGraphicsPack("mark_square_2.png", zip);
            handicapSlot = loadImageFromGraphicsPack("handicap_slot.png", zip);
        } catch (Exception e) {
            String errMsg = "Couldn't open graphics pack " + graphicsPath + "!";
            CustomExceptionDialog.show(e, errMsg);
            System.err.println(errMsg);
            e.printStackTrace();
        }
    }

    private Image loadImageFromGraphicsPack(String fileName, ZipFile zip) {
        if(fileName == null || zip == null) {
            throw new NullPointerException();
        }

        ZipEntry zipEntry = zip.getEntry(fileName);

        if(zipEntry == null) {
            IOException e = new IOException();
            String errMsg = "File " + fileName + " is not present in graphics pack " + graphicsPath + "!";
            CustomExceptionDialog.show(e, errMsg);
            System.err.println(errMsg);
            e.printStackTrace();
        }

        Image ret = null;

        try (InputStream is = zip.getInputStream(zipEntry)) {
            final int DEFAULT_IMAGE_SIZE = 128;
            final boolean SMOOTH_IMAGES = false;

            ret = new Image(
                is,             // is (:InputStream)
                DEFAULT_IMAGE_SIZE, // requestedWidth
                DEFAULT_IMAGE_SIZE, // requestedHeight
                true,               // preserveRatio
                SMOOTH_IMAGES);     // smooth
        } catch (IOException e) {
            String errMsg = "File " + fileName + " appears to be present but unreadable in graphics pack " + graphicsPath + "!";
            CustomExceptionDialog.show(e, errMsg);
            System.err.println(errMsg);
            e.printStackTrace();
        }

        return ret;
    }

    private void updateGraphics() {
        for(int i = 0; i < 4; i++) {
            BoardCell bc = (BoardCell)getChildren().get(i);
            bc.setBackgroundImage(outerCorner);
            bc.getTile().setRotate(90 * i);
        }

        for(int i = 0; i < size; i++) {
            // edges
            for(int j = 0; j < 4; j++) {
                BoardCell bc = (BoardCell)getChildren().get(4 + i * 4 + j);
                bc.setBackgroundImage(outerEdge);
                bc.getTile().setRotate(90 * (j % 2));
            }
            // center
            for(int j = 0; j < size; j++) {
                getPlayableCell(j, i).updateImages(tile);
            }
        }

        getPlayableCell(0, 0).setBackgroundImage(tileCorner);
        getPlayableCell(this.size - 1, 0).setBackgroundImage(tileCorner);
        getPlayableCell(this.size - 1, 0).getTile().setRotate(90);
        getPlayableCell(this.size - 1, this.size - 1).setBackgroundImage(tileCorner);
        getPlayableCell(this.size - 1, this.size - 1).getTile().setRotate(180);
        getPlayableCell(0, this.size - 1).setBackgroundImage(tileCorner);
        getPlayableCell(0, this.size - 1).getTile().setRotate(270);

        for(int i = 1; i < this.size - 1; i++) {
            getPlayableCell(i, 0).setBackgroundImage(tileEdge);
            getPlayableCell(this.size - 1, i).setBackgroundImage(tileEdge);
            getPlayableCell(this.size - 1, i).getTile().setRotate(90);
            getPlayableCell(i, this.size - 1).setBackgroundImage(tileEdge);
            getPlayableCell(i, this.size - 1).getTile().setRotate(180);
            getPlayableCell(0, i).setBackgroundImage(tileEdge);
            getPlayableCell(0, i).getTile().setRotate(270);
        }
    }

    private PlayableBoardCell getPlayableCell(int x, int y) {
        if(x < 0 || x >= size || y < 0 || y >= size) {
            throw new IllegalArgumentException("Coordinates X=" + x + "/Y=" + y + "out of bounds for getPlayableCell() when size is " + size);
        }
        return (PlayableBoardCell)getChildren().get(4 + size * 4 + y * size + x);
    }

    /**
     * Base class for all Cells of the board. Only has a background and label. Use for edges and corners. For the
     * center tiles, use PlayableBoardCell instead.
     */
    private class BoardCell extends StackPane {
        /**
         * Instance of the background image
         */
        private final ResizableImageView TILE;

        /**
         * the Label to be displayed by this BoardCell
         */
        protected final Label LABEL;

        /**
         * Creates a new BoardCell with the specified background Image, as well as a visible label
         * @param tile the background Image (not to be confused with BackgroundImage) to be used for this BoardCell
         */
        private BoardCell(Image tile) {
            this.TILE = addCellImageView(tile);
            this.TILE.setVisible(true);

            this.LABEL = new Label("0");
            this.LABEL.setMinSize(0, 0);
            this.LABEL.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

            final DoubleProperty FONT_SIZE = new SimpleDoubleProperty(0);
            FONT_SIZE.bind(this.LABEL.widthProperty().divide(2).subtract(Bindings.length(this.LABEL.textProperty())));
            this.LABEL.styleProperty().bind(Bindings.concat("-fx-font-size: ", FONT_SIZE));

            updateLabelColor();

            getChildren().add(this.LABEL);

            this.setMinSize(0, 0);
            prefWidthProperty().bind(MAX_CELL_DIM_INT);
            prefHeightProperty().bind(MAX_CELL_DIM_INT);
            setMouseTransparent(true);
        }

        /**
         * Properly sets up the background image for this BoardCell. Call this for each BoardCell after changing
         * the graphics pack (PlayableBoardCells call this automatically in their updateImages() method)
         * @param tile the background Image (not to be confused with BackgroundImage) to be used for this BoardCell
         */
        public void setBackgroundImage(Image tile) {
            TILE.setImage(tile);
            updateLabelColor();
        }

        /**
         * Returns the background ResizableImageView used by this cell
         * @return the background ResizableImageView of this cell
         */
        public ResizableImageView getTile() {
            return TILE;
        }

        // Getters
        public Label getLabel() {
            return LABEL;
        }

        // Private methods
        /**
         * Sets the text color of this BoardCell's label to the inverse of its background tile's center color
         */
        private void updateLabelColor() {
            Image bgImg = TILE.getImage();

            PixelReader p = bgImg.getPixelReader();
            if(p == null) {
                throw new NullPointerException("Can't get tile background color");
            }
            LABEL.setTextFill(
                p.getColor((int)(bgImg.getWidth() / 2), (int)(bgImg.getHeight() / 2)).invert()
            );
        }

        /**
         * Produces an instance of the provided image that is set up properly for this BoardCell and adds
         * it to the BoardCell's children
         * @param i the image to be instantiated
         * @return a properly instantiated instance of the provided Image
         */
        protected ResizableImageView addCellImageView(Image i) {
            if(i == null) {
                throw new NullPointerException();
            }

            ResizableImageView iv = new ResizableImageView(i);
            iv.setPreserveRatio(true);
            iv.setMouseTransparent(true);
            iv.setSmooth(false);
            iv.setVisible(false);

            getChildren().add(iv);

            return iv;
        }
    } // private class BoardCell

    /**
     * Class for all PLAYABLE Cells of the board. Has Images for the black and white stones and hovers.
     */
    private class PlayableBoardCell extends BoardCell {

        /**
         * whether this PlayableBoardCell is currently selected
         */
        private boolean isSelected = false;

        private boolean isCircleMarked = false;

        private boolean isTriangleMarked = false;

        private boolean isSquareMarked = false;

        /**
         * Instance of the global Image of the Black stone; used for hovering and selection
         */
        private final ResizableImageView BLACK_HOVER;

        /**
         * Instance of the global Image of the white stone; used for hovering and selection
         */
        private final ResizableImageView WHITE_HOVER;

        /**
         * Instance of the global Image of the Black stone, used for setting
         */
        private final ResizableImageView BLACK_STONE;

        /**
         * Instance of the global Image of the Black stone, used for setting
         */
        private final ResizableImageView WHITE_STONE;

        /**
         * Instance of the global Image of the circle mark for use on empty cells
         */
        private final ResizableImageView CIRCLE_MARK_ON_EMPTY;

        /**
         * Instance of the global Image of the circle mark for use on black stones
         */
        private final ResizableImageView CIRCLE_MARK_ON_BLACK;

        /**
         * Instance of the global Image of the circle mark for use on white stones
         */
        private final ResizableImageView CIRCLE_MARK_ON_WHITE;

        /**
         * Instance of the global Image of the triangle mark for use on empty cells
         */
        private final ResizableImageView TRIANGLE_MARK_ON_EMPTY;

        /**
         * Instance of the global Image of the triangle mark for use on black stones
         */
        private final ResizableImageView TRIANGLE_MARK_ON_BLACK;

        /**
         * Instance of the global Image of the triangle mark for use on white stones
         */
        private final ResizableImageView TRIANGLE_MARK_ON_WHITE;

        /**
         * Instance of the global Image of the square mark for use on empty cells
         */
        private final ResizableImageView SQUARE_MARK_ON_EMPTY;

        /**
         * Instance of the global Image of the square mark for use on black stones
         */
        private final ResizableImageView SQUARE_MARK_ON_BLACK;

        /**
         * Instance of the global Image of the square mark for use on white stones or empty tiles
         */
        private final ResizableImageView SQUARE_MARK_ON_WHITE;

        /**
         * Instanc eof the global image for use on handicap stone slots
         */
        private final ResizableImageView HANDICAP_SLOT;

        /**
         * Pointer to the ImageView of the currently set stone, if any
         */
        private ResizableImageView currentlySetStone;

        /**
         * Creates a new PlayableBoardCell with the specified background Image, images for the black and white
         * stones and hovers, as well as an invisible label
         */
        private PlayableBoardCell(Image tile) {
            super(tile);

            this.HANDICAP_SLOT = addCellImageView(handicapSlot);

            this.CIRCLE_MARK_ON_EMPTY = addCellImageView(circleMarks[0]);
            this.TRIANGLE_MARK_ON_EMPTY = addCellImageView(triangleMarks[0]);
            this.SQUARE_MARK_ON_EMPTY = addCellImageView(squareMarks[0]);

            this.BLACK_HOVER = addCellImageView(stones[0]);
            this.WHITE_HOVER = addCellImageView(stones[1]);

            this.BLACK_STONE = addCellImageView(stones[0]);
            this.WHITE_STONE = addCellImageView(stones[1]);

            this.CIRCLE_MARK_ON_BLACK = addCellImageView(circleMarks[2]);
            this.CIRCLE_MARK_ON_WHITE = addCellImageView(circleMarks[1]);

            this.TRIANGLE_MARK_ON_BLACK = addCellImageView(triangleMarks[2]);
            this.TRIANGLE_MARK_ON_WHITE = addCellImageView(triangleMarks[1]);

            this.SQUARE_MARK_ON_BLACK = addCellImageView(squareMarks[2]);
            this.SQUARE_MARK_ON_WHITE = addCellImageView(squareMarks[1]);

            this.currentlySetStone = null;

            this.LABEL.setVisible(false);
            this.LABEL.setAlignment(Pos.CENTER);
            this.LABEL.toFront();

            setMouseTransparent(false);

            // Set up listeners
            // If this is enabled, dragging from outside the actual playable board doesn't work (might be desirable)
            /*setOnDragDetected((e) -> {
                startFullDrag();
            });*/

            setOnMouseEntered((e) -> {
                keyboardCellX = getColumnIndex(this) - 1;
                keyboardCellY = getRowIndex(this) - 1;

                hover();
            });

            setOnMouseDragEntered(getOnMouseEntered());

            setOnMouseExited((e) -> {
                unhover();

                /*
                 *  Doing this ensures that if the mouse leaves the playing field, the keyboard controls resume at
                 *  the default location of 0/0. Consequently, removing these lines means that keyboard controls resume
                 *  where the mouse was last on the board, which MIGHT be desirable.
                 */
                keyboardCellX = -1;
                keyboardCellY = -1;
            });

            setOnMouseDragExited(getOnMouseExited());

            setOnMouseClicked((e) -> {
                if(e.getButton() == MouseButton.PRIMARY) {
                    select();
                } else {
                    toggleCircleMark();
                }
            });

            setOnMouseDragReleased(getOnMouseClicked());
        }

        /**
         * Changes all Images used by this PlayableBoardCell to the current global Images from the graphics pack.
         * Call this for each PlayableBoardCell after loading a different graphics pack
         */
        public void updateImages(Image tile) {
            setBackgroundImage(tile);
            HANDICAP_SLOT.setImage(handicapSlot);
            if(currentlySetStone != null) {
                updateLabelColor();
            }
            BLACK_STONE.setImage(stones[0]);
            BLACK_HOVER.setImage(stones[0]);
            WHITE_STONE.setImage(stones[1]);
            WHITE_HOVER.setImage(stones[1]);

            CIRCLE_MARK_ON_EMPTY.setImage(circleMarks[0]);
            CIRCLE_MARK_ON_BLACK.setImage(circleMarks[2]);
            CIRCLE_MARK_ON_WHITE.setImage(circleMarks[1]);
            TRIANGLE_MARK_ON_EMPTY.setImage(triangleMarks[0]);
            TRIANGLE_MARK_ON_BLACK.setImage(triangleMarks[2]);
            TRIANGLE_MARK_ON_WHITE.setImage(triangleMarks[1]);
            SQUARE_MARK_ON_EMPTY.setImage(squareMarks[0]);
            SQUARE_MARK_ON_BLACK.setImage(squareMarks[2]);
            SQUARE_MARK_ON_WHITE.setImage(squareMarks[1]);
        }

        public void showHandicapSlot() {
            HANDICAP_SLOT.setVisible(true);
        }

        public void hideHandicapSlot() {
            HANDICAP_SLOT.setVisible(false);
        }

        /**
         * Updates the showing of the move number (i.e., the label) according to its own set status and the global
         * BoardPane's showsMoveNumbers attributes
         */
        public void showMoveNumber() {
            LABEL.setVisible(currentlySetStone != null && showsMoveNumbers);
            updateLabelColor();
        }

        /**
         * Makes this PlayableBoardCell display a translucent version of the white stone to indicate that it is being
         * hovered over and can be selected with a left click
         */
        public void hoverWhite() {
            hover(WHITE_HOVER);
        }

        /**
         * Makes this PlayableBoardCell display a translucent version of the black stone to indicate that it is being
         * hovered over and can be selected with a left click
         */
        public void hoverBlack() {
            hover(BLACK_HOVER);
        }

        /**
         * Removes all hover indicators on this PlayableBoardCell, unless it is selected (to remove a selection
         * indicator, call deselect() instead).
         */
        public void unhover() {
            if (!isSelected) {
                BLACK_HOVER.setVisible(false);
                WHITE_HOVER.setVisible(false);
                if(hoverPBC == this) {
                    hoverPBC = null;
                }
            }
        }

        /**
         * Makes this PlayableBoardCell display a translucent version of the provided ImageView to indicate that it is
         * being hovered over and can be selected with a left click
         * @param iv the ImageView to be displayed
         */
        private void hover(ImageView iv) {
            unhover();                      // might be unnecessary
            if(hoverPBC != null) {
                hoverPBC.unhover();             // necessary due to keyboard controls
                hoverPBC = null;
            }
            if(currentlySetStone == null && !isSelected) {
                iv.setOpacity(0.5);
                iv.setVisible(true);
            }
            hoverPBC = this;
        }

        /**
         * Makes this PlayableBoardCell display a translucent version of the current player color to indicate that it is
         * being hovered over and can be selected with a left click
         */
        private void hover() {
            if (game.getCurColor() == BLACK) {
                hoverBlack();
            } else {
                hoverWhite();
            }
        }

        /**
         * Makes this PlayableBoardCell display a translucent but slightly more opaque than just hovered version of the
         * white stone to indicate that it is currently selected.
         */
        public void selectWhite() {
            select(WHITE_HOVER);
        }

        /**
         * Makes this PlayableBoardCell display a translucent but slightly more opaque than just hovered version of the
         * black stone to indicate that it is currently selected.
         */
        public void selectBlack() {
            select(BLACK_HOVER);
        }

        /**
         * Removes all selection indicators on this PlayableBoardCell.
         */
        public void deselect() {
            isSelected = false;
            unhover();
        }

        /**
         * Makes this PlayableBoardCell display a translucent but slightly more opaque than just hovered version of the
         * provided ImageView to indicate that it is currently selected.
         * @param iv the ImageView that is to be displayed
         */
        private void select(ImageView iv) {
            if (selectionPBC != null) {
                selectionPBC.deselect();
            }
            selectionPBC = this;

            deselect();             // might be unnecessary
            iv.setOpacity(0.75);
            iv.setVisible(true);
            isSelected = true;

            if (!needsMoveConfirmation) {
                confirmMove();
            }
        }

        private void select() {
            if (game.getCurColor() == BLACK) {
                selectBlack();
            } else {
                selectWhite();
            }
        }

        /**
         * Makes this PlayableBoardCell display an opaque white stone to indicate that one has been set.
         */
        public void setWhite() {
            set(WHITE_STONE);
        }

        /**
         * Makes this PlayableBoardCell display an opaque black stone to indicate that one has been set.
         */
        public void setBlack() {
            set(BLACK_STONE);
        }

        /**
         * Removes all set AND all selection indicators on this PlayableBoardCell.
         */
        public void unset() {
            deselect();
            BLACK_STONE.setVisible(false);
            WHITE_STONE.setVisible(false);
            LABEL.setVisible(false);
            currentlySetStone = null;

            updateMarks();
        }

        /**
         * Makes this PlayableBoardCell display an opaque provided ImageView to indicate that a stone has been set.
         * @param iv the ImageView to be displayed
         */
        private void set(ResizableImageView iv) {
            deselect();
            iv.setVisible(true);
            currentlySetStone = iv;
            if(showsMoveNumbers) {
                updateLabelColor();
                LABEL.setVisible(true);
            }

            updateMarks();
        }

        /**
         * Marks this PLayableBoardCell with a circle mark
         */
        private void markCircle() {
            mark(CIRCLE_MARK_ON_EMPTY, CIRCLE_MARK_ON_BLACK, CIRCLE_MARK_ON_WHITE);
            isCircleMarked = true;
            // Game.getFileTree().markACoordinate(getColumnIndex(this) - 1, getRowIndex(this) - 1)
        }

        /**
         * Marks this PLayableBoardCell with a triangle mark
         */
        private void markTriangle() {
            mark(TRIANGLE_MARK_ON_EMPTY, TRIANGLE_MARK_ON_BLACK, TRIANGLE_MARK_ON_WHITE);
            isTriangleMarked = true;
        }

        /**
         * Marks this PLayableBoardCell with a square mark
         */
        private void markSquare() {
            mark(SQUARE_MARK_ON_EMPTY, SQUARE_MARK_ON_BLACK, SQUARE_MARK_ON_WHITE);
            isSquareMarked = true;
        }

        /**
         * Marks this PlayableBoardCell with the supplied kind of mark
         * @param onBlack the ResizableImageView constituting the mark to be used on black stones
         * @param onWhite the ResizableImageView constituting the mark to be used on white stones or empty spaces
         */
        private void mark(ResizableImageView onEmpty, ResizableImageView onBlack, ResizableImageView onWhite) {
            if(currentlySetStone == BLACK_STONE) {
                onBlack.setVisible(true);
            } else if(currentlySetStone == WHITE_STONE) {
                onWhite.setVisible(true);
            } else {
                onEmpty.setVisible(true);
            }
        }

        /**
         * Unmarks this PlayableBoardCell from the supplied kind of mark
         * @param onBlack the ResizableImageView constituting the mark to be used on black stones
         * @param onWhite the ResizableImageView constituting the mark to be used on white stones or empty spaces
         */
        private void unMark(ResizableImageView onEmpty, ResizableImageView onBlack, ResizableImageView onWhite) {
            onEmpty.setVisible(false);
            onWhite.setVisible(false);
            onBlack.setVisible(false);
        }

        /**
         * Unmarks this PLayableBoardCell altogether.
         */
        private void unMark() {
            unMark(CIRCLE_MARK_ON_EMPTY, CIRCLE_MARK_ON_BLACK, CIRCLE_MARK_ON_WHITE);
            isCircleMarked = false;
            unMark(TRIANGLE_MARK_ON_EMPTY, TRIANGLE_MARK_ON_BLACK, TRIANGLE_MARK_ON_WHITE);
            isTriangleMarked = false;
            unMark(TRIANGLE_MARK_ON_EMPTY, SQUARE_MARK_ON_BLACK, SQUARE_MARK_ON_WHITE);
            isSquareMarked = false;
        }

        public void toggleCircleMark() {
            if(isCircleMarked) {
                unMark();
            } else {
                markCircle();
            }
        }

        /**
         * Updates whether the marks in question are shown
         * @param isMarked whether this playableBoardCell is marked thusly
         * @param onBlack the ResizableImageView constituting the mark to be used on black stones
         * @param onWhite the ResizableImageView constituting the mark to be used on white stones or empty spaces
         */
        private void updateMarks(boolean isMarked, ResizableImageView onEmpty, ResizableImageView onBlack, ResizableImageView onWhite) {
            if(isMarked) {
                if(currentlySetStone == BLACK_STONE) {
                    onEmpty.setVisible(false);
                    onBlack.setVisible(true);
                    onWhite.setVisible(false);
                } else if(currentlySetStone == WHITE_STONE) {
                    onEmpty.setVisible(false);
                    onBlack.setVisible(false);
                    onWhite.setVisible(true);
                } else {
                    onEmpty.setVisible(true);
                    onBlack.setVisible(false);
                    onWhite.setVisible(false);
                }
            }
        }

        /**
         * Updates all marks on this PLayableBoardCell.
         */
        private void updateMarks() {
            updateMarks(isCircleMarked, CIRCLE_MARK_ON_EMPTY, CIRCLE_MARK_ON_BLACK, CIRCLE_MARK_ON_WHITE);
            updateMarks(isTriangleMarked, TRIANGLE_MARK_ON_EMPTY, TRIANGLE_MARK_ON_BLACK, TRIANGLE_MARK_ON_WHITE);
            updateMarks(isSquareMarked, SQUARE_MARK_ON_EMPTY, SQUARE_MARK_ON_BLACK, SQUARE_MARK_ON_WHITE);
        }

        /**
         * Sets the text color of this PlayableBoardCell's label to the inverse of the center color of the stone that
         * is currently set.
         */
        private void updateLabelColor() {
            if(currentlySetStone != null) {
                PixelReader p = currentlySetStone.getImage().getPixelReader();
                if(p == null) {
                    throw new NullPointerException("Can't get stone color");
                }
                LABEL.setTextFill(p.getColor((int)(currentlySetStone.getImage().getWidth() / 2), (int)(currentlySetStone.getImage().getHeight() / 2)).invert());
            } else {
                super.updateLabelColor();
            }
        }
    } // private class PlayableBoardCell extends BoardCell
}
