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
    private boolean needsMoveConfirmation = false;

    /**
     * whether move numbers are shown on the stones
     */
    private boolean showsMoveNumbers = false;

    /**
     * whether coordinates are shown on the sides of the board
     */
    private boolean showsCoordinates = true;

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
     * Background Image (not to be confused with BackgroundImage) for playable BoardCells
     */
    private Image tile;
    
    /**
     * Image used for the black and white stones
     */
    private final Image[] stones = new Image [2];
    
    /**
     * Background Image (not to be confused with BackgroundImage) for the BoardPane's edges
     */
     
    private Image edge;
    /**
     * Background Image (not to be confused with BackgroundImage) for the BoardPane's corners
     */
    private Image corner;

    /**
     * the currently selected PlayableBoardCell
     */
    private PlayableBoardCell selectionPBC = null;

    /**
     * NumberBinding for the width and height of all BoardCells
     */
    private NumberBinding MAX_CELL_DIM_INT;

    // TODO: Remove in final product
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

        game.addListener(e -> {
            if(e == null) {
                throw new NullPointerException();
            }

            switch(e.getGameCommand()) {
                case CONFIRMCHOICE:
                    confirmMove();
                    break;
                case INIT:
                    setMouseTransparent(true);
                    break;
                case WHITSTARTS:
                case BLACKSTARTS:
                    System.out.println(e.getGameCommand()+" inBoardPane: BoardSize: " + e.getSize() + " Komi: "+  e.getKomi());

                    setMouseTransparent(false);
                    init();
                    break;
                case BLACKHANDICAP:
                case WHITEHANDICAP:
                    StoneSetEvent sseH = (StoneSetEvent) e;
                    PlayableBoardCell destBC = getPlayableCell(sseH.getX(), sseH.getY());

                    if (sseH.getColor() == BLACK) {
                        destBC.setBlack();
                    } else {
                        destBC.setWhite();
                    }
                    destBC.getLabel().setVisible(false);

                    break;
                case BLACKPLAYS:
                case WHITEPLAYS:
                    StoneSetEvent sse = (StoneSetEvent) e;
                    PlayableBoardCell destinationBC = getPlayableCell(sse.getX(), sse.getY());
                    destinationBC.getLabel().setText("" + sse.getMoveNumber());

                    if (sse.getColor() == BLACK) {
                        destinationBC.setBlack();
                    } else {
                        destinationBC.setWhite();
                    }
                    break;
                case WHITEREMOVED:
                case BLACKREMOVED:
                    StoneRemovedEvent sre = (StoneRemovedEvent) e;
                    getPlayableCell(sre.getX(), sre.getY()).unset();
                    break;
                case DEBUGINFO:
                    DebugEvent de = (DebugEvent) e;
                    getPlayableCell(de.getX(), de.getY()).getLabel().setText(de.getPtrNo() + "," + de.getGroupNo());
                    break;
                default: return;
            }

        }); //ToDo: full Event integration

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
        for (int i = 0; i < this.size; i++) {
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
                 *
                 * Note: I changed this to make sure that Game would be the only connection between Model and View/Controller.
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

        // Set up listeners
        // If this is active, dragging from within this BoardPane but outside the actual playble board works (might be desirable)
        /*setOnDragDetected((e) -> {
            startFullDrag();
        });*/

        setOnKeyPressed((e) -> {
            // TODO: Keyboard input?
        });


        // Layout of this BoardPane
        setAlignment(Pos.CENTER);
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
            bc.setBackgroundImage(corner);
        }

        for(int i = 0; i < size; i++) {
            // edges
            for(int j = 0; j < 4; j++) {
                BoardCell bc = (BoardCell)getChildren().get(4 + i * 4 + j);
                bc.setBackgroundImage(edge);
            }
            // center
            for(int j = 0; j < size; j++) {
                getPlayableCell(j, i).updateImages();
            }
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

    public Image getEdge() {
        return edge;
    }

    public Image getCorner() {
        return corner;
    }

    // private methods
    /**
     * loads the image files from the specified graphics pack into memory
     * @param graphicsPath the absolute path of the graphics pack to be loaded
     */
    private void loadGraphics(String graphicsPath) {
        try (ZipFile zip = new ZipFile(graphicsPath)) {
            ZipEntry tileEntry = zip.getEntry("tile.png");
            ZipEntry cornerEntry = zip.getEntry("corner.png");
            ZipEntry edgeEntry = zip.getEntry("edge.png");
            ZipEntry stone0Entry = zip.getEntry("stone_0.png");
            ZipEntry stone1Entry = zip.getEntry("stone_1.png");

            if(Stream.of(tileEntry, cornerEntry, edgeEntry, stone0Entry, stone1Entry).anyMatch(Objects::isNull)) {
                throw new IllegalStateException("ERROR: Graphics pack " + graphicsPath + " is missing files!");
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
         * the Label to be displayed by this BoardCell
         */
        protected final Label LABEL;

        /**
         * Creates a new BoardCell with the specified background Image, as well as a visible label
         * @param tile the background Image (not to be confused with BackgroundImage) to be used for this BoardCell
         */
        private BoardCell(Image tile) {
            this.LABEL = new Label("0");
            this.LABEL.setMinSize(0, 0);
            this.LABEL.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

            final DoubleProperty FONT_SIZE = new SimpleDoubleProperty(0);
            FONT_SIZE.bind(this.LABEL.widthProperty().divide(2).subtract(Bindings.length(this.LABEL.textProperty())));
            this.LABEL.styleProperty().bind(Bindings.concat("-fx-font-size: ", FONT_SIZE));

            getChildren().add(this.LABEL);

            setBackgroundImage(tile);

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
            BackgroundSize bgSz = new BackgroundSize(
                100,     // width
                100,        // height
                true,       // widthAsPercentage
                true,       // heightAsPercentage
                false,      // contain
                true        // cover
            );
            BackgroundImage bgImg = new BackgroundImage(
                tile,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                bgSz
            );
            this.setBackground(new Background(bgImg));
            updateLabelColor();
        }

        /**
         * Sets the text color of this BoardCell's label to the inverse of its background tile's center color
         */
        private void updateLabelColor() {
            Image bgImg = getBackground().getImages().get(0).getImage();

            PixelReader p = bgImg.getPixelReader();
            if(p == null) {
                throw new NullPointerException("Can't get tile background color");
            }
            LABEL.setTextFill(
                p.getColor((int)(bgImg.getWidth() / 2), (int)(bgImg.getHeight() / 2)).invert()
            );
        }

        // Getters
        public Label getLabel() {
            return LABEL;
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
         * Pointer to the ImageView of the currently set stone, if any
         */
        private ResizableImageView CURRENTLY_SET_STONE;

        /**
         * Creates a new PlayableBoardCell with the specified background Image, images for the black and white
         * stones and hovers, as well as an invisible label
         */
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
                if (game.getCurColor() == BLACK) {
                    hoverBlack();
                } else {
                    hoverWhite();
                }
            });

            setOnMouseDragEntered(getOnMouseEntered());

            setOnMouseExited((e) -> {
                unhover();
            });

            setOnMouseDragExited(getOnMouseExited());

            setOnMouseClicked((e) -> {
                if (selectionPBC != null) {
                    selectionPBC.deselect();
                }
                selectionPBC = this;

                if(game.getCurColor() == BLACK) {
                    selectBlack();
                } else {
                    selectWhite();
                }

                if(!needsMoveConfirmation) {
                    confirmMove();
                }
            });

            setOnMouseDragReleased(getOnMouseClicked());
        }

        /**
         * Changes all Images used by this PlayableBoardCell to the current global Images from the graphics pack.
         * Call this for each PlayableBoardCell after loading a different graphics pack
         */
        public void updateImages() {
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
            unhover();              // might be unnecessary
            if(CURRENTLY_SET_STONE == null && !isSelected) {
                iv.setOpacity(0.5);
                iv.setVisible(true);
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
            deselect();             // might be unnecessary
            iv.setOpacity(0.75);
            iv.setVisible(true);
            isSelected = true;
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
        }

        /**
         * Produces an instance of the provided image that is set up properly for this PlayableBoardCell
         * @param i the image to be instantiated
         * @return a properly instantiated instance of the provided Image
         */
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
