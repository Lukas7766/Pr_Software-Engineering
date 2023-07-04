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
import pr_se.gogame.model.helper.MarkShape;
import pr_se.gogame.view_controller.dialog.CustomExceptionDialog;
import pr_se.gogame.view_controller.observer.DebugEvent;
import pr_se.gogame.view_controller.observer.ViewListener;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static pr_se.gogame.model.helper.StoneColor.BLACK;
import static pr_se.gogame.model.helper.StoneColor.WHITE;

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
    private NumberBinding maxCellDimInt;

    /**
     * X index for keyboard controls
     */
    private int keyboardCellX = -1;

    /**
     * Y index for keyboard controls
     */
    private int keyboardCellY = -1;

    private String lastGraphicsPackFileName;

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
        this.showsMoveNumbers = GlobalSettings.isShowMoveNumbers();
        this.showsCoordinates = GlobalSettings.isShowCoordinates();
        this.needsMoveConfirmation = GlobalSettings.isConfirmationNeeded();
        this.lastGraphicsPackFileName = GlobalSettings.getGraphicsPackFileName();

        GlobalSettings.addListener(new ViewListener() {
            @Override
            public void onSettingsUpdated() {
                setMoveConfirmation(GlobalSettings.isConfirmationNeeded());
                setShowsCoordinates(GlobalSettings.isShowCoordinates());
                setShowsMoveNumbers(GlobalSettings.isShowMoveNumbers());
                loadGraphics(GlobalSettings.getGraphicsPath());
                updateGraphics();
            }

            @Override
            public void onMoveConfirmed() {
                confirmMove();
            }
        });

        game.addListener(e -> {
            if(e == null) {
                throw new NullPointerException();
            }

            PlayableBoardCell destinationPBC;

            switch(e.getGameCommand()) {
                case STONE_WAS_SET:
                    destinationPBC = getPlayableCell(e.getX(), e.getY());
                    destinationPBC.getLabel().setText("" + e.getMoveNumber());

                    if (e.getColor() == BLACK) {
                        destinationPBC.setBlack();
                    } else if(e.getColor() == WHITE) {
                        destinationPBC.setWhite();
                    }

                    break;

                case STONE_WAS_REMOVED:
                    getPlayableCell(e.getX(), e.getY()).unset();
                    break;

                case HANDICAP_SET:
                    destinationPBC = getPlayableCell(e.getX(), e.getY());

                    destinationPBC.showHandicapSlot();
                    destinationPBC.getLabel().setVisible(false);

                    break;

                case HANDICAP_REMOVED:
                    destinationPBC = getPlayableCell(e.getX(), e.getY());

                    destinationPBC.hideHandicapSlot();

                    break;

                case SETUP_STONE_SET:
                    destinationPBC = getPlayableCell(e.getX(), e.getY());

                    destinationPBC.getLabel().setVisible(false);

                    break;

                case UNMARK, MARK_CIRCLE, MARK_SQUARE, MARK_TRIANGLE:
                    destinationPBC = getPlayableCell(e.getX(), e.getY());

                    switch (e.getGameCommand()) {
                        case UNMARK -> destinationPBC.unMark();
                        case MARK_CIRCLE -> destinationPBC.markCircle();
                        case MARK_SQUARE -> destinationPBC.markSquare();
                        case MARK_TRIANGLE -> destinationPBC.markTriangle();
                        default -> throw new IllegalStateException("Unsupported GameCommand " + e.getGameCommand());
                    }
                    break;

                case UPDATE:
                    if(hoverPBC != null) {
                        hoverPBC.hover();
                    }
                    setMouseTransparent(false);
                    requestFocus(); // This is necessary so that focus is taken away from the comment text area upon doing anything.
                    break;

                case NEW_GAME:
                    setMouseTransparent(false);
                    init();
                    break;

                case INIT, GAME_WON:
                    setMouseTransparent(true);
                    setFocusTraversable(false);
                    setFocused(false);
                    if(selectionPBC != null) {
                        selectionPBC.deselect();
                    }
                    if(hoverPBC != null) {
                        hoverPBC.unhover();
                    }
                    keyboardCellX = -1;
                    keyboardCellY = -1;
                    break;

                case DEBUG_INFO:
                    DebugEvent de = (DebugEvent) e;
                    getPlayableCell(de.getX(), de.getY()).getLabel().setText(de.getPtrNo() + "," + de.getGroupNo());
                    break;

                default:
                    break;
            }

        });

        loadGraphics(GlobalSettings.getGraphicsPath());
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
        final NumberBinding maxCellDim = Bindings.min(
            widthProperty().divide(size + 2),
            heightProperty().divide(size + 2)
        );
        maxCellDimInt = Bindings.createIntegerBinding(maxCellDim::intValue, maxCellDim); // round down

        // Put the axes' corners in first to mess up the indexing as little as possible.
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
            String horizText = "" + (char)('A' + i);
            String vertText = "" + (size - i);

            // top
            BoardCell t = new BoardCell(this.outerEdge);
            t.getLabel().setText(horizText);
            t.getLabel().setAlignment(Pos.CENTER);
            add(t, i + 1, 0);

            // right
            BoardCell r = new BoardCell(this.outerEdge);
            r.getLabel().setText(vertText);
            r.getLabel().setAlignment(Pos.CENTER);
            add(r, size + 1, i + 1);

            // bottom
            BoardCell b = new BoardCell(this.outerEdge);
            b.getLabel().setText(horizText);
            b.getLabel().setAlignment(Pos.CENTER);
            add(b, i + 1, size + 1);

            // left
            BoardCell l = new BoardCell(this.outerEdge);
            l.getLabel().setText(vertText);
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

        setOnKeyPressed(e -> {
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
                case UP, W, I:
                    if(keyboardCellY > 0) {
                        keyboardCellY--;
                    }
                    hasMoved = true;
                    break;

                case DOWN, S, K:
                    if(keyboardCellY < size - 1) {
                        keyboardCellY++;
                    }
                    hasMoved = true;
                    break;

                case LEFT, A, J:
                    if(keyboardCellX > 0) {
                        keyboardCellX--;
                    }
                    hasMoved = true;
                    break;

                case RIGHT, D, L:
                    if(keyboardCellX < size - 1) {
                        keyboardCellX++;
                    }
                    hasMoved = true;
                    break;

                case M:
                    if(hoverPBC != null) {
                        hoverPBC.toggleCircleMark();
                    }
                    break;

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
        // this.setPadding(new Insets(7.5,7.5,7.5,5.5)); Sadly, this breaks the cells' aspect ratio (even equal insets on all four sides will)
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
                game.usePosition(col, row);
            }

            if(GlobalSettings.DEBUG) {
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
                hoverPBC.hover();
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
        if(!needsMoveConfirmation && selectionPBC != null) {
            selectionPBC.deselect();
            selectionPBC = null;
        }
        this.needsMoveConfirmation = needsMoveConfirmation;
    }

    public boolean showsMoveNumbers() {
        return showsMoveNumbers;
    }

    public void setShowsMoveNumbers(boolean showsMoveNumbers) {
        this.showsMoveNumbers = showsMoveNumbers;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                PlayableBoardCell pbc = getPlayableCell(j, i);
                if (GlobalSettings.DEBUG || !pbc.getLabel().getText().startsWith("0")) {
                    pbc.showOrHideMoveNumber();
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

    public int getSize() {
        return size;
    }

    // private methods
    /**
     * loads the image files from the specified graphics pack into memory
     * @param graphicsPath the absolute path of the graphics pack to be loaded
     */
    private void loadGraphics(String graphicsPath) {
        if(graphicsPath == null) {
            throw new NullPointerException();
        }

        if(graphicsPath.equals(this.graphicsPath)) {
            return; // Don't unnecessarily reload the entire graphics pack.
        }

        this.graphicsPath = graphicsPath;

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

            lastGraphicsPackFileName = GlobalSettings.getGraphicsPackFileName();
        } catch (Exception e) {
            CustomExceptionDialog.show(e, "Couldn't open graphics pack \"" + graphicsPath + "\"!", e.getMessage());
            GlobalSettings.setGraphicsPackFileName(lastGraphicsPackFileName);
        }
    }

    /**
     * Loads a particular image from the graphics pack zip file
     * @param fileName the file name of the image
     * @param zip the ZipFile of the graphics pack
     * @return the fully instantiated image
     */
    private Image loadImageFromGraphicsPack(String fileName, ZipFile zip) throws IOException {
        if(fileName == null || zip == null) {
            throw new NullPointerException();
        }

        ZipEntry zipEntry = zip.getEntry(fileName);

        if(zipEntry == null) {
            throw new IOException("File " + fileName + " is not present in graphics pack \"" + graphicsPath + "\"!");
        }

        Image ret;

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
            throw new IOException("File " + fileName + " appears to be present but unreadable in graphics pack \"" + graphicsPath + "\"! (" + e.getMessage() + ")");
        }

        return ret;
    }

    /**
     * Assigns the currently loaded images to the correct BoardCells.
     */
    private void updateGraphics() {
        for(int i = 0; i < 4; i++) {
            BoardCell bc = (BoardCell)getChildren().get(i);
            bc.updateImages(outerCorner);
            bc.getTile().setRotate(90.0 * i);
        }

        for(int i = 0; i < size; i++) {
            // edges
            for(int j = 0; j < 4; j++) {
                BoardCell bc = (BoardCell)getChildren().get(4 + i * 4 + j);
                bc.updateImages(outerEdge);
                bc.getTile().setRotate(90.0 * (j % 2));
            }
            // center
            for(int j = 0; j < size; j++) {
                getPlayableCell(j, i).updateImages(tile);
            }
        }

        getPlayableCell(0, 0).updateImages(tileCorner);
        getPlayableCell(this.size - 1, 0).updateImages(tileCorner);
        getPlayableCell(this.size - 1, 0).getTile().setRotate(90);
        getPlayableCell(this.size - 1, this.size - 1).updateImages(tileCorner);
        getPlayableCell(this.size - 1, this.size - 1).getTile().setRotate(180);
        getPlayableCell(0, this.size - 1).updateImages(tileCorner);
        getPlayableCell(0, this.size - 1).getTile().setRotate(270);

        for(int i = 1; i < this.size - 1; i++) {
            getPlayableCell(i, 0).updateImages(tileEdge);
            getPlayableCell(this.size - 1, i).updateImages(tileEdge);
            getPlayableCell(this.size - 1, i).getTile().setRotate(90);
            getPlayableCell(i, this.size - 1).updateImages(tileEdge);
            getPlayableCell(i, this.size - 1).getTile().setRotate(180);
            getPlayableCell(0, i).updateImages(tileEdge);
            getPlayableCell(0, i).getTile().setRotate(270);
        }
    }

    /**
     * This method is used to more easily and maintainably obtain a PlayableBoardCell from this BoardPane's list of
     * children.
     * @param x the x coordinate starting at the left
     * @param y the y coordinate starting at the top
     * @return the PlayableBoardCell at the specified coordinates
     */
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
        private final ResizableImageView tile;

        /**
         * the Label to be displayed by this BoardCell
         */
        protected final Label label;

        /**
         * Creates a new BoardCell with the specified background Image, as well as a visible label
         * @param tile the background Image (not to be confused with BackgroundImage) to be used for this BoardCell
         */
        private BoardCell(Image tile) {
            this.tile = addCellImageView(tile);
            this.tile.setVisible(true);

            this.label = new Label("0");
            this.label.setMinSize(0, 0);
            this.label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            this.label.setVisible(GlobalSettings.isShowCoordinates());

            final DoubleProperty fontSize = new SimpleDoubleProperty(0);
            fontSize.bind(this.label.widthProperty().divide(2).subtract(Bindings.length(this.label.textProperty())));
            this.label.styleProperty().bind(Bindings.concat("-fx-font-size: ", fontSize));

            updateLabelColor();

            getChildren().add(this.label);

            this.setMinSize(0, 0);
            prefWidthProperty().bind(maxCellDimInt);
            prefHeightProperty().bind(maxCellDimInt);
            setMouseTransparent(true);
        }

        /**
         * Properly sets up the background image for this BoardCell. Call this for each BoardCell after changing
         * the graphics pack (PlayableBoardCells call this automatically in their updateImages() method)
         * @param tile the background Image (not to be confused with BackgroundImage) to be used for this BoardCell
         */
        public void updateImages(Image tile) {
            this.tile.setImage(tile);
            updateLabelColor();
        }

        /**
         * Returns the background ResizableImageView used by this cell
         * @return the background ResizableImageView of this cell
         */
        public ResizableImageView getTile() {
            return tile;
        }

        // Getters
        public Label getLabel() {
            return label;
        }

        // Private methods
        /**
         * Sets the text color of this BoardCell's label to the inverse of its background tile's center color
         */
        protected void updateLabelColor() {
            Image bgImg = tile.getImage();

            PixelReader p = bgImg.getPixelReader();
            if(p == null) {
                throw new NullPointerException("Can't get tile background color");
            }
            label.setTextFill(
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
        private final ResizableImageView blackHover;

        /**
         * Instance of the global Image of the white stone; used for hovering and selection
         */
        private final ResizableImageView whiteHover;

        /**
         * Instance of the global Image of the Black stone, used for setting
         */
        private final ResizableImageView blackStone;

        /**
         * Instance of the global Image of the Black stone, used for setting
         */
        private final ResizableImageView whiteStone;

        /**
         * Instance of the global Image of the circle mark for use on empty cells
         */
        private final ResizableImageView circleMarkOnEmpty;

        /**
         * Instance of the global Image of the circle mark for use on black stones
         */
        private final ResizableImageView circleMarkOnBlack;

        /**
         * Instance of the global Image of the circle mark for use on white stones
         */
        private final ResizableImageView circleMarkOnWhite;

        /**
         * Instance of the global Image of the triangle mark for use on empty cells
         */
        private final ResizableImageView triangleMarkOnEmpty;

        /**
         * Instance of the global Image of the triangle mark for use on black stones
         */
        private final ResizableImageView triangleMarkOnBlack;

        /**
         * Instance of the global Image of the triangle mark for use on white stones
         */
        private final ResizableImageView triangleMarkOnWhite;

        /**
         * Instance of the global Image of the square mark for use on empty cells
         */
        private final ResizableImageView squareMarkOnEmpty;

        /**
         * Instance of the global Image of the square mark for use on black stones
         */
        private final ResizableImageView squareMarkOnBlack;

        /**
         * Instance of the global Image of the square mark for use on white stones or empty tiles
         */
        private final ResizableImageView squareMarkOnWhite;

        /**
         * Instance of the global image for use on handicap stone slots
         */
        private final ResizableImageView handicapSlot;

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

            this.handicapSlot = addCellImageView(BoardPane.this.handicapSlot);

            this.circleMarkOnEmpty = addCellImageView(circleMarks[0]);
            this.triangleMarkOnEmpty = addCellImageView(triangleMarks[0]);
            this.squareMarkOnEmpty = addCellImageView(squareMarks[0]);

            this.blackHover = addCellImageView(stones[0]);
            this.whiteHover = addCellImageView(stones[1]);

            this.blackStone = addCellImageView(stones[0]);
            this.whiteStone = addCellImageView(stones[1]);

            this.circleMarkOnBlack = addCellImageView(circleMarks[2]);
            this.circleMarkOnWhite = addCellImageView(circleMarks[1]);

            this.triangleMarkOnBlack = addCellImageView(triangleMarks[2]);
            this.triangleMarkOnWhite = addCellImageView(triangleMarks[1]);

            this.squareMarkOnBlack = addCellImageView(squareMarks[2]);
            this.squareMarkOnWhite = addCellImageView(squareMarks[1]);

            this.currentlySetStone = null;

            this.label.setVisible(false);
            this.label.setAlignment(Pos.CENTER);
            this.label.toFront();

            setMouseTransparent(false);

            // Set up listeners

            setOnMouseEntered(e -> {
                keyboardCellX = getColumnIndex(this) - 1;
                keyboardCellY = getRowIndex(this) - 1;

                hover();
            });

            setOnMouseDragEntered(getOnMouseEntered());

            setOnMouseExited(e -> {
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

            setOnMouseClicked(e -> {
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
        @Override
        public void updateImages(Image tile) {
            super.updateImages(tile);
            handicapSlot.setImage(BoardPane.this.handicapSlot);
            if(currentlySetStone != null) {
                updateLabelColor();
            }
            blackStone.setImage(stones[0]);
            blackHover.setImage(stones[0]);
            whiteStone.setImage(stones[1]);
            whiteHover.setImage(stones[1]);

            circleMarkOnEmpty.setImage(circleMarks[0]);
            circleMarkOnBlack.setImage(circleMarks[2]);
            circleMarkOnWhite.setImage(circleMarks[1]);
            triangleMarkOnEmpty.setImage(triangleMarks[0]);
            triangleMarkOnBlack.setImage(triangleMarks[2]);
            triangleMarkOnWhite.setImage(triangleMarks[1]);
            squareMarkOnEmpty.setImage(squareMarks[0]);
            squareMarkOnBlack.setImage(squareMarks[2]);
            squareMarkOnWhite.setImage(squareMarks[1]);
        }

        public void showHandicapSlot() {
            handicapSlot.setVisible(true);
        }

        public void hideHandicapSlot() {
            handicapSlot.setVisible(false);
        }

        /**
         * Updates the showing of the move number (i.e., the label) according to its own set status and the global
         * BoardPane's showsMoveNumbers attributes
         */
        public void showOrHideMoveNumber() {
            label.setVisible(currentlySetStone != null && showsMoveNumbers);
            updateLabelColor();
        }

        /**
         * Removes all hover indicators on this PlayableBoardCell, unless it is selected (to remove a selection
         * indicator, call deselect() instead).
         */
        public void unhover() {
            if (!isSelected) {
                blackHover.setVisible(false);
                whiteHover.setVisible(false);
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
        public void hover() {
            if (game.getCurColor() == BLACK) {
                hover(blackHover);
            } else {
                hover(whiteHover);
            }
        }

        /**
         * Makes this PlayableBoardCell display a translucent but slightly more opaque than just hovered version of the
         * white stone to indicate that it is currently selected.
         */
        public void selectWhite() {
            select(whiteHover);
        }

        /**
         * Makes this PlayableBoardCell display a translucent but slightly more opaque than just hovered version of the
         * black stone to indicate that it is currently selected.
         */
        public void selectBlack() {
            select(blackHover);
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
            set(whiteStone);
        }

        /**
         * Makes this PlayableBoardCell display an opaque black stone to indicate that one has been set.
         */
        public void setBlack() {
            set(blackStone);
        }

        /**
         * Removes all set AND all selection indicators on this PlayableBoardCell.
         */
        public void unset() {
            deselect();
            blackStone.setVisible(false);
            whiteStone.setVisible(false);
            label.setVisible(false);
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
                label.setVisible(true);
            }

            updateMarks();
        }

        /**
         * Marks this PlayableBoardCell with a circle mark
         */
        private void markCircle() {
            mark(circleMarkOnEmpty, circleMarkOnBlack, circleMarkOnWhite);
            isCircleMarked = true;
        }

        /**
         * Marks this PlayableBoardCell with a triangle mark
         */
        private void markTriangle() {
            mark(triangleMarkOnEmpty, triangleMarkOnBlack, triangleMarkOnWhite);
            isTriangleMarked = true;
        }

        /**
         * Marks this PlayableBoardCell with a square mark
         */
        private void markSquare() {
            mark(squareMarkOnEmpty, squareMarkOnBlack, squareMarkOnWhite);
            isSquareMarked = true;
        }

        /**
         * Marks this PlayableBoardCell with the supplied kind of mark
         * @param onBlack the ResizableImageView constituting the mark to be used on black stones
         * @param onWhite the ResizableImageView constituting the mark to be used on white stones or empty spaces
         */
        private void mark(ResizableImageView onEmpty, ResizableImageView onBlack, ResizableImageView onWhite) {
            if(currentlySetStone == blackStone) {
                onBlack.setVisible(true);
            } else if(currentlySetStone == whiteStone) {
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
         * Unmarks this PlayableBoardCell altogether.
         */
        private void unMark() {
            unMark(circleMarkOnEmpty, circleMarkOnBlack, circleMarkOnWhite);
            isCircleMarked = false;
            unMark(triangleMarkOnEmpty, triangleMarkOnBlack, triangleMarkOnWhite);
            isTriangleMarked = false;
            unMark(triangleMarkOnEmpty, squareMarkOnBlack, squareMarkOnWhite);
            isSquareMarked = false;
        }

        public void toggleCircleMark() {
            int x = getColumnIndex(this) - 1;
            int y = getRowIndex(this) - 1;

            if(isCircleMarked) {
                game.unmark(x, y);
            } else {
                game.mark(x, y, MarkShape.CIRCLE);
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
                if(currentlySetStone == blackStone) {
                    onEmpty.setVisible(false);
                    onBlack.setVisible(true);
                    onWhite.setVisible(false);
                } else if(currentlySetStone == whiteStone) {
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
         * Updates all marks on this PlayableBoardCell.
         */
        private void updateMarks() {
            updateMarks(isCircleMarked, circleMarkOnEmpty, circleMarkOnBlack, circleMarkOnWhite);
            updateMarks(isTriangleMarked, triangleMarkOnEmpty, triangleMarkOnBlack, triangleMarkOnWhite);
            updateMarks(isSquareMarked, squareMarkOnEmpty, squareMarkOnBlack, squareMarkOnWhite);
        }

        /**
         * Sets the text color of this PlayableBoardCell's label to the inverse of the center color of the stone that
         * is currently set.
         */
        @Override
        protected void updateLabelColor() {
            if(currentlySetStone != null) {
                PixelReader p = currentlySetStone.getImage().getPixelReader();
                if(p == null) {
                    throw new NullPointerException("Can't get stone color");
                }
                label.setTextFill(p.getColor((int)(currentlySetStone.getImage().getWidth() / 2), (int)(currentlySetStone.getImage().getHeight() / 2)).invert());
            } else {
                super.updateLabelColor();
            }
        }
    } // private class PlayableBoardCell extends BoardCell
}
