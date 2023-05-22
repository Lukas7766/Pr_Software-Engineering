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
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import pr_se.gogame.model.Game;
import pr_se.gogame.model.StoneColor;

import java.io.InputStream;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static pr_se.gogame.model.StoneColor.*;

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
     * @param graphicsPath the absolute path of the graphics pack zip-file
     */
    public BoardPane(Game game, String graphicsPath) {
        if(game == null || graphicsPath == null) {
            throw new NullPointerException();
        }

        setMouseTransparent(true);

        this.game = game;
        this.graphicsPath = graphicsPath;
        this.showsMoveNumbers = game.isShowMoveNumbers();
        this.showsCoordinates = game.isShowCoordinates();
        this.needsMoveConfirmation = game.isConfirmationNeeded();

        game.addListener(e -> {
            if(e == null) {
                throw new NullPointerException();
            }
            System.out.println("show move numbers border pane " + showsMoveNumbers);
            switch(e.getGameCommand()) {
                case BLACK_PLAYS:
                case WHITE_PLAYS:
                    if(e instanceof StoneSetEvent) {//TODO: StoneEvent vs GameCommand question
                        System.out.println("StoneSetEvent");
                        StoneSetEvent sse = (StoneSetEvent) e;
                        PlayableBoardCell destinationBC = getPlayableCell(sse.getX(), sse.getY());
                        destinationBC.getLabel().setText("" + sse.getMoveNumber());

                        if (sse.getColor() == BLACK) {
                            destinationBC.setBlack();
                        } else {
                            destinationBC.setWhite();
                        }
                    }
                    break;
                case CONFIRM_CHOICE:
                    confirmMove();
                    break;
                case WHITE_HAS_CAPTURED:
                case BLACK_HAS_CAPTURED:
                    StoneRemovedEvent sre = (StoneRemovedEvent) e;
                    getPlayableCell(sre.getX(), sre.getY()).unset();
                    break;
                case INIT:
                    setMouseTransparent(true);
                    break;
                case WHITE_STARTS:
                case BLACK_STARTS:
                    setMouseTransparent(false);
                    init();
                    break;
                case BLACK_HANDICAP:
                case WHITE_HANDICAP:
                    StoneSetEvent sseH = (StoneSetEvent) e;
                    PlayableBoardCell destBC = getPlayableCell(sseH.getX(), sseH.getY());

                    if (sseH.getColor() == BLACK) {
                        destBC.setBlack();
                    } else {
                        destBC.setWhite();
                    }
                    destBC.getLabel().setVisible(false);
                    break;
                case BLACK_WON:
                case WHITE_WON:
                case DRAW:
                    setMouseTransparent(true);
                    break;
                case CONFIG_CONFIRMATION:
                    setMoveConfirmation(game.isConfirmationNeeded());
                    break;
                case CONFIG_SHOW_COORDINATES:
                    setShowsCoordinates(game.isShowCoordinates());
                    break;
                case CONFIG_SHOWMOVENUMBERS:
                    setShowsMoveNumbers(game.isShowMoveNumbers());
                    break;
                case DEBUG_INFO:
                    DebugEvent de = (DebugEvent) e;
                    getPlayableCell(de.getX(), de.getY()).getLabel().setText(de.getPtrNo() + "," + de.getGroupNo());
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
        getChildren().removeAll(getChildren());

        this.size = this.game.getSize();
        // this.setPadding(new Insets(7.5,7.5,7.5,5.5)); No, don't to that, it breaks the cells' aspect ratio (even equal insets on all four sides will)

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
        corner2.getTile().setRotate(90);
        add(corner2, size + 1, 0);
        BoardCell corner3 = new BoardCell(this.outerCorner);
        corner3.getLabel().setVisible(false);
        corner3.getTile().setRotate(180);
        add(corner3, size + 1, size + 1);
        BoardCell corner4 = new BoardCell(this.outerCorner);
        corner4.getLabel().setVisible(false);
        corner4.getTile().setRotate(270);
        add(corner4, 0, size + 1);

        // populate the coordinate axes
        for (int i = 0; i < this.size; i++) {
            // top
            BoardCell t = new BoardCell(this.outerEdge);
            t.getLabel().setText("" + (char)('A' + i));
            t.getLabel().setAlignment(Pos.BOTTOM_CENTER);
            add(t, i + 1, 0);

            // right
            BoardCell r = new BoardCell(this.outerEdge);
            r.getLabel().setText("" + (size - i));
            r.getLabel().setAlignment(Pos.CENTER_LEFT);
            r.getTile().setRotate(90);
            add(r, size + 1, i + 1);

            // bottom
            BoardCell b = new BoardCell(this.outerEdge);
            b.getLabel().setText("" + (char)('A' + i));
            b.getLabel().setAlignment(Pos.TOP_CENTER);
            add(b, i + 1, size + 1);

            // left
            BoardCell l = new BoardCell(this.outerEdge);
            l.getLabel().setText("" + (size - i));
            l.getLabel().setAlignment(Pos.CENTER_RIGHT);
            l.getTile().setRotate(90);
            add(l, 0, i + 1);
        }

        // Fill the grid with tiles
        for(int i = 0; i < this.size; i++) {
            for(int j = 0; j < this.size; j++) {
                PlayableBoardCell bc = new PlayableBoardCell(this.tile);
                /*
                 * We have to check for the initial board condition here, as the BoardPane cannot exist when the Board
                 * is initialised, as that happens on creating the Game, which is required to create the BoardPane.
                 */
                StoneColor c = this.game.getColorAt(j, i);
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

        // Update the corner and edge tiles to use the proper backgrounds
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

        // Set up listeners
        // If this is active, dragging from within this BoardPane but outside the actual playable board works (might be desirable)
        /*setOnDragDetected((e) -> {
            startFullDrag();
        });*/

        setOnKeyPressed((e) -> {
            System.out.println(e.getCode() + " PRESSED");

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

                case ENTER:
                case SPACE:
                    if(hoverPBC != null) {
                        hoverPBC.select();
                    }
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
        requestFocus();
    }

    /*
     * TODO: (minor tweak) Immediately change lastMouseHover on completion (esp. if a situation arises where the mouse
     *  might be on the board during confirmation)
     * TODO: Although it might be said that the model should remain unchanged until confirmation, I am not sure whether
     *  this is really the responsibility of the view.
     */
    /**
     * If moves are to be confirmed, calling this method confirms a move on the currently selected PlayableBoardCell,
     * calling the game's playMove() method.
     */
    public void confirmMove() {
        if(selectionPBC != null) {
            int col = getColumnIndex(selectionPBC) - 1;
            int row = getRowIndex(selectionPBC) - 1;
            if(col >= 0 && row >= 0) {
                if(game.getHandicapStoneCounter() <= 0) {
                    game.playMove(col, row);
                } else {
                    game.placeHandicapStone(col, row);
                }
            } else {
                System.out.println("Confirmation outside of actual board on " + selectionPBC); // TODO: Remove in finished product
            }

            if(debug) {
                for (int i = 0; i < size; i++) {
                    for (int j = 0; j < size; j++) {
                        game.printDebugInfo(i, j);
                    }
                }
            }

            /*
             * This part is necessary to ensure that, once a stone has been set, keyboard controls don't originate from
             * it (as they would if it were still selected). Note that "selectionPBC = null;" must not be moved into
             * the deselect()-method; otherwise, previous selections will no longer be cleaned up, when clicking
             * about, as there is no pointer.
             */
            selectionPBC.deselect();
            selectionPBC = null;

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
                getPlayableCell(j, i).showMoveNumber();
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

        this.graphicsPath = graphicsPath;

        loadGraphics(graphicsPath);

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

        // Update the corner and edge tiles to use the proper backgrounds
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

    public int getSize() {
        return size;
    }

    public Image getTile() {
        return tile;
    }

    public Image[] getStones() {
        return stones;
    }

    public Image getOuterEdge() {
        return outerEdge;
    }

    public Image getOuterCorner() {
        return outerCorner;
    }

    // private methods
    /**
     * loads the image files from the specified graphics pack into memory
     * @param graphicsPath the absolute path of the graphics pack to be loaded
     */
    private void loadGraphics(String graphicsPath) {
        try (ZipFile zip = new ZipFile(graphicsPath)) {
            ZipEntry tileEntry = zip.getEntry("tile.png");
            ZipEntry tileCornerEntry = zip.getEntry("tile_corner.png");
            ZipEntry tileEdgeEntry = zip.getEntry("tile_edge.png");
            ZipEntry outerCornerEntry = zip.getEntry("outer_corner.png");
            ZipEntry outerEdgeEntry = zip.getEntry("outer_edge.png");
            ZipEntry stone0Entry = zip.getEntry("stone_0.png");
            ZipEntry stone1Entry = zip.getEntry("stone_1.png");
            ZipEntry circleMark0Entry = zip.getEntry("mark_circle_0.png");
            ZipEntry circleMark1Entry = zip.getEntry("mark_circle_1.png");
            ZipEntry circleMark2Entry = zip.getEntry("mark_circle_2.png");
            ZipEntry triangleMark0Entry = zip.getEntry("mark_triangle_0.png");
            ZipEntry triangleMark1Entry = zip.getEntry("mark_triangle_1.png");
            ZipEntry triangleMark2Entry = zip.getEntry("mark_triangle_2.png");
            ZipEntry squareMark0Entry = zip.getEntry("mark_square_0.png");
            ZipEntry squareMark1Entry = zip.getEntry("mark_square_1.png");
            ZipEntry squareMark2Entry = zip.getEntry("mark_square_2.png");

            if(Stream.of(tileEntry,
                    tileCornerEntry,
                    tileEdgeEntry,
                    outerCornerEntry,
                    outerEdgeEntry,
                    stone0Entry,
                    stone1Entry,
                    circleMark0Entry,
                    circleMark1Entry,
                    circleMark2Entry,
                    triangleMark0Entry,
                    triangleMark1Entry,
                    triangleMark2Entry,
                    squareMark0Entry,
                    squareMark1Entry,
                    squareMark2Entry
                ).anyMatch(Objects::isNull)) {
                throw new IllegalStateException("ERROR: Graphics pack " + graphicsPath + " is missing files!");
            }

            try (InputStream tileIS = zip.getInputStream(tileEntry);
                 InputStream tileCornerIS = zip.getInputStream(tileCornerEntry);
                 InputStream tileEdgeIS = zip.getInputStream(tileEdgeEntry);
                 InputStream outerCornerIS = zip.getInputStream(outerCornerEntry);
                 InputStream outerEdgeIS = zip.getInputStream(outerEdgeEntry);
                 InputStream stone0IS = zip.getInputStream(stone0Entry);
                 InputStream stone1IS = zip.getInputStream(stone1Entry);
                 InputStream circleMark0IS = zip.getInputStream(circleMark0Entry);
                 InputStream circleMark1IS = zip.getInputStream(circleMark1Entry);
                 InputStream circleMark2IS = zip.getInputStream(circleMark2Entry);
                 InputStream triangleMark0IS = zip.getInputStream(triangleMark0Entry);
                 InputStream triangleMark1IS = zip.getInputStream(triangleMark1Entry);
                 InputStream triangleMark2IS = zip.getInputStream(triangleMark2Entry);
                 InputStream squareMark0IS = zip.getInputStream(squareMark0Entry);
                 InputStream squareMark1IS = zip.getInputStream(squareMark1Entry);
                 InputStream squareMark2IS = zip.getInputStream(squareMark2Entry)
            ) {
                final int DEFAULT_IMAGE_SIZE = 128;
                final boolean SMOOTH_IMAGES = false;

                tile = new Image(
                        tileIS,             // is (:InputStream)
                        DEFAULT_IMAGE_SIZE, // requestedWidth
                        DEFAULT_IMAGE_SIZE, // requestedHeight
                        true,               // preserveRation
                        SMOOTH_IMAGES);     // smooth
                this.tileEdge = new Image(tileEdgeIS, DEFAULT_IMAGE_SIZE, DEFAULT_IMAGE_SIZE, true, SMOOTH_IMAGES);
                this.tileCorner = new Image(tileCornerIS, DEFAULT_IMAGE_SIZE, DEFAULT_IMAGE_SIZE, true, SMOOTH_IMAGES);
                this.outerEdge = new Image(outerEdgeIS, DEFAULT_IMAGE_SIZE, DEFAULT_IMAGE_SIZE, true, SMOOTH_IMAGES);
                this.outerCorner = new Image(outerCornerIS, DEFAULT_IMAGE_SIZE, DEFAULT_IMAGE_SIZE, true, SMOOTH_IMAGES);

                stones[0] = new Image(stone0IS);
                stones[1] = new Image(stone1IS);

                circleMarks[0] = new Image(circleMark0IS);
                circleMarks[1] = new Image(circleMark1IS);
                circleMarks[2] = new Image(circleMark2IS);

                triangleMarks[0] = new Image(triangleMark0IS);
                triangleMarks[1] = new Image(triangleMark1IS);
                triangleMarks[2] = new Image(triangleMark2IS);

                squareMarks[0] = new Image(squareMark0IS);
                squareMarks[1] = new Image(squareMark1IS);
                squareMarks[2] = new Image(squareMark2IS);
            } catch (Exception e) {
                System.err.println("ERROR: Couldn't read file from graphics pack " + graphicsPath + "!");
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.err.println("ERROR: Couldn't open graphics pack " + graphicsPath + "!");
            e.printStackTrace();
        }
    }

    private PlayableBoardCell getPlayableCell(int x, int y) {
        if(x < 0 || x >= size || y < 0 || y >= size) {
            throw new IllegalArgumentException();
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
            this.TILE = getCellImageView(tile);
            getChildren().add(this.TILE);
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
         * Produces an instance of the provided image that is set up properly for this PlayableBoardCell
         * @param i the image to be instantiated
         * @return a properly instantiated instance of the provided Image
         */
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

        protected ResizableImageView getCellImageView(Image i) {
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
         * Pointer to the ImageView of the currently set stone, if any
         */
        private ResizableImageView CURRENTLY_SET_STONE;

        /**
         * Creates a new PlayableBoardCell with the specified background Image, images for the black and white
         * stones and hovers, as well as an invisible label
         */
        private PlayableBoardCell(Image tile) {
            super(tile);

            this.CIRCLE_MARK_ON_EMPTY = getCellImageView(circleMarks[0]);
            getChildren().add(this.CIRCLE_MARK_ON_EMPTY);
            this.TRIANGLE_MARK_ON_EMPTY = getCellImageView(triangleMarks[0]);
            getChildren().add(this.TRIANGLE_MARK_ON_EMPTY);
            this.SQUARE_MARK_ON_EMPTY = getCellImageView(squareMarks[0]);
            getChildren().add(this.SQUARE_MARK_ON_EMPTY);

            this.BLACK_HOVER = getCellImageView(stones[0]);
            getChildren().add(this.BLACK_HOVER);
            this.WHITE_HOVER = getCellImageView(stones[1]);
            getChildren().add(this.WHITE_HOVER);

            this.BLACK_STONE = getCellImageView(stones[0]);
            getChildren().add(this.BLACK_STONE);
            this.WHITE_STONE = getCellImageView(stones[1]);
            getChildren().add(this.WHITE_STONE);

            this.CIRCLE_MARK_ON_BLACK = getCellImageView(circleMarks[2]);
            getChildren().add(this.CIRCLE_MARK_ON_BLACK);
            this.CIRCLE_MARK_ON_WHITE = getCellImageView(circleMarks[1]);
            getChildren().add(this.CIRCLE_MARK_ON_WHITE);

            this.TRIANGLE_MARK_ON_BLACK = getCellImageView(triangleMarks[2]);
            getChildren().add(this.TRIANGLE_MARK_ON_BLACK);
            this.TRIANGLE_MARK_ON_WHITE = getCellImageView(triangleMarks[1]);
            getChildren().add(this.TRIANGLE_MARK_ON_WHITE);

            this.SQUARE_MARK_ON_BLACK = getCellImageView(squareMarks[2]);
            getChildren().add(this.SQUARE_MARK_ON_BLACK);
            this.SQUARE_MARK_ON_WHITE = getCellImageView(squareMarks[1]);
            getChildren().add(this.SQUARE_MARK_ON_WHITE);

            this.CURRENTLY_SET_STONE = null;

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
            if(CURRENTLY_SET_STONE != null) {
                if(CURRENTLY_SET_STONE == BLACK_STONE) {
                    CURRENTLY_SET_STONE.setImage(stones[0]);
                } else {
                    CURRENTLY_SET_STONE.setImage(stones[1]);
                }
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

        /**
         * Updates the showing of the move number (i.e., the label) according to its own set status and the global
         * BoardPane's showsMoveNumbers attributes
         */
        public void showMoveNumber() {
            LABEL.setVisible(CURRENTLY_SET_STONE != null && showsMoveNumbers);
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
            if(CURRENTLY_SET_STONE == null && !isSelected) {
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
        private void setWhite() {
            set(WHITE_STONE);
        }

        /**
         * Makes this PlayableBoardCell display an opaque black stone to indicate that one has been set.
         */
        private void setBlack() {
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
            CURRENTLY_SET_STONE = null;

            updateMarks();
        }

        /**
         * Makes this PlayableBoardCell display an opaque provided ImageView to indicate that a stone has been set.
         * @param iv the ImageView to be displayed
         */
        private void set(ResizableImageView iv) {
            deselect();
            iv.setVisible(true);
            CURRENTLY_SET_STONE = iv;
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
            if(CURRENTLY_SET_STONE  == BLACK_STONE) {
                onBlack.setVisible(true);
            } else if(CURRENTLY_SET_STONE == WHITE_STONE) {
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
                if(CURRENTLY_SET_STONE == BLACK_STONE) {
                    onEmpty.setVisible(false);
                    onBlack.setVisible(true);
                    onWhite.setVisible(false);
                } else if(CURRENTLY_SET_STONE == WHITE_STONE) {
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
            if(CURRENTLY_SET_STONE != null) {
                PixelReader p = CURRENTLY_SET_STONE.getImage().getPixelReader();
                if(p == null) {
                    throw new NullPointerException("Can't get stone color");
                }
                LABEL.setTextFill(p.getColor((int)(CURRENTLY_SET_STONE.getImage().getWidth() / 2), (int)(CURRENTLY_SET_STONE.getImage().getHeight() / 2)).invert());
            } else {
                super.updateLabelColor();
            }
        }
    } // private class PlayableBoardCell extends BoardCell
}
