package pr_se.gogame.view_controller;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableBooleanValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import pr_se.gogame.model.Board;
import pr_se.gogame.model.StoneColor;

/**
 * View/Controller
 * Go Board that uses image files for its (checkerboard) tiles and stones
 */
public class BoardPane extends TilePane {

    private final int SIZE;                         // number of columns and rows, respectively
    private boolean needsMoveConfirmation = false;  // whether moves have to be confirmed separately (TODO: might need a better name)

    private boolean showsMoveNumbers = false;        // whether move numbers are shown
    private Board board;                            // Model for MVC-adherence; Will likely be replaced with Game

    /*
     * Custom resources
     */
    private final Image[] tiles = new Image [2];
    private final Image[] stones = new Image [2];

    // TODO: This sort of thing would ideally be cleaned up by using some sort of data structure.
    private Node lastMouseTarget = null;
    private ImageView lastMouseHover = null;
    private Node selectionTarget = null;
    private ImageView selectionHover = null;

    private enum cellLayerIndices {
        BACKGROUND, BLACKHOVER, WHITEHOVER, BLACKSTONE, WHITESTONE, LABEL;
    }

    // TODO: Maybe move constructor content into an init() method, especially with regards to loading images and even the baord (as those might be changed during a game).
    public BoardPane(Board board, String tile0, String tile1, String stone0, String stone1) {
        super();

        setBoard(board);
        this.SIZE = board.getSize();
        setPrefColumns(this.SIZE);


        // TODO: In the end product, the files would be chosen by the user (and perhaps packaged in an archive)
        tiles[0] = new Image(
                tile0,      // URL
                128,        // requestedWidth
                128,        // requestedHeight
                true,       // preserveRation
                false,      // smooth
                true);      // backgroundLoading
        tiles[1] = new Image(tile1, 128, 128, true, false, true);

        // setBackground(new Background(new BackgroundImage[]{ new BackgroundImage(tiles[0], BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, new BackgroundSize(1, 1, true, true, false, false))}));

        stones[0] = new Image(
                stone0,     // URL
                true);      // backgroundLoading
        stones[1] = new Image(stone1, true);

        // Graphical details of this board pane
        setAlignment(Pos.CENTER);
        setHgap(0);
        setVgap(0);
        setPadding(new Insets(0, 0, 0, 0));
        setMinSize(0, 0);

        // setGridLinesVisible(true);

        // Fill the grid with ImageViews of alternating tiles
        for(int i = 0; i < this.SIZE; i++) {
            for(int j = 0; j < this.SIZE; j++) {
                StackPane sp = new StackPane();

                // TODO: To prevent the thin lines from disappearing, and possibly also the white lines in between from
                // appearing, maybe the tiles should not actually change in size and instead be loaded in way bigger
                // than they could be shown, with only the viewport (displayed portion) changing in size. Diasadvantage
                // would be that the lines would always remain as thin as they are, no matter how large the board is
                // scaled.
                ImageView iv = getCellImageView(tiles[(j % 2 + i % 2) % 2]);
                sp.getChildren().add(iv);

                ImageView blackHover = getCellImageView(stones[0]);
                blackHover.setVisible(false);
                sp.getChildren().add(blackHover);

                ImageView whiteHover = getCellImageView(stones[1]);
                whiteHover.setVisible(false);
                sp.getChildren().add(whiteHover);

                ImageView blackStone = getCellImageView(stones[0]);
                blackStone.setVisible(false);
                sp.getChildren().add(blackStone);

                ImageView whiteStone = getCellImageView(stones[1]);
                whiteStone.setVisible(false);
                sp.getChildren().add(whiteStone);

                Label l = new Label("0");
                l.setVisible(false);
                sp.getChildren().add(l);

                getChildren().add(sp);

                setMargin(sp, new Insets(0, 0, 0, 0));
                sp.setPadding(new Insets(0, 0, 0, 0));
            }
        }

        /*setPrefTileWidth(10);
        setPrefTileHeight(10);*/
        StackPane cell0 = (StackPane)getChildren().get(0);
        ImageView bg = (ImageView)cell0.getChildren().get(cellLayerIndices.BACKGROUND.ordinal());
        prefTileWidthProperty().bind(bg.fitWidthProperty());
        prefTileHeightProperty().bind(bg.fitHeightProperty());
        setTileAlignment(Pos.TOP_LEFT);
        setHgap(0);
        setVgap(0);
        setPadding(new Insets(0, 0, 0, 0));

        // Set up listeners
        /*setOnMouseMoved(e -> {
            Node target = (Node)e.getTarget();
            // System.out.println("Target is of class " + target.getClass());
            if(target != null) {
                if(target != lastMouseTarget) {                                 // TODO: This seems to fire a bit too readily, making the program run less efficiently. I am not sure why, though.
                    System.out.println("Hovering over something new!");
                    Integer col = getColumnIndex(target);
                    Integer row = getRowIndex(target);

                    if (col != null && row != null) {
                        StackPane targetSP = (StackPane)target;
                        this.board.printDebugInfo(col, row);

                        // System.out.println("Hover over " + col + " " + row);    // TODO: Remove in finished product
                        int stoneIndex = cellLayerIndices.BLACKHOVER.ordinal();
                        if (this.board.getCurColor() != StoneColor.BLACK) {
                            stoneIndex = cellLayerIndices.WHITEHOVER.ordinal();
                        }

                        ImageView iv = (ImageView)targetSP.getChildren().get(stoneIndex);
                        iv.setOpacity(0.5);
                        iv.setVisible(true);

                        // Remove old hover                                    // TODO: Remove in finished product
                        if(lastMouseHover != null) {
                            lastMouseHover.setVisible(false);
                        }
                        //System.out.println("Removed hover!");               // TODO: Remove in finished product
                        lastMouseTarget = target;
                        lastMouseHover = iv;
                    } else {
                        //System.out.println("Hover target is not a cell!");  // TODO: Remove in finished product
                        if(lastMouseHover != null) {
                            lastMouseHover.setVisible(false);
                        }
                        lastMouseTarget = null;
                        lastMouseHover = null;
                    }
                }
            } else {
                //System.out.println("Hover target is null!");                // TODO: Remove in finished product
                lastMouseTarget = null;
                lastMouseHover = null;
            }
        });

        setOnMouseClicked(e -> {
            if(e.getButton() == MouseButton.PRIMARY) { // This check is only for testing independently of the main UI.
                if (lastMouseTarget != null) {
                    Integer col = getColumnIndex(lastMouseTarget);
                    Integer row = getRowIndex(lastMouseTarget);
                    if (col != null && row != null) { // TODO: Remember to account for the inclusion of labels in the grid, which could potentially be at either end.
                        selectionTarget = lastMouseTarget;
                        selectionHover = lastMouseHover;
                        selectionHover.setOpacity(0.75);

                        lastMouseTarget = null;
                        lastMouseHover = null;

                        if (!needsMoveConfirmation) {
                            confirmMove();
                        }
                    } else {
                        System.out.println("Click outside of actual board on " + lastMouseTarget); // TODO: Remove in finished product
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
        });*/


    }

    private ImageView getCellImageView(Image i) {
        if(i == null) {
            throw new NullPointerException();
        }

        ImageView iv = new ImageView(i);
        iv.setPreserveRatio(true);
        iv.fitHeightProperty().bind(heightProperty().subtract(this.SIZE).divide(this.SIZE));
        iv.fitWidthProperty().bind(widthProperty().subtract(this.SIZE).divide(this.SIZE));
        iv.setMouseTransparent(true);
        iv.setSmooth(false);
        setMargin(iv, new Insets(0, 0, 0, 0));

        return iv;
    }

    public void setBoard(Board board) {
        this.board = board;

        board.addListener(new GoListener() {
            @Override
            public void stoneSet(StoneSetEvent e) {
                int stoneIndex = cellLayerIndices.BLACKSTONE.ordinal();
                Color labelColor = Color.rgb(255, 255, 255);
                if(e.getColor() != StoneColor.BLACK) {
                    stoneIndex = cellLayerIndices.WHITESTONE.ordinal();
                    labelColor = Color.rgb(0, 0, 0);
                }

                /*if(selectionHover != null) {
                    selectionHover.setVisible(false);
                }*/

                StackPane destinationSP = (StackPane)getChildren().get(e.getRow() * SIZE + e.getCol());
                ImageView iv = (ImageView)destinationSP.getChildren().get(stoneIndex);
                iv.setVisible(true);

                if(showsMoveNumbers) {
                    Label l = (Label) destinationSP.getChildren().get(cellLayerIndices.LABEL.ordinal());
                    l.setText("" + e.getMoveNumber());
                    l.setTextFill(labelColor);
                    l.toFront();
                    l.setVisible(true);
                }
            }

            @Override
            public void stoneRemoved(StoneRemovedEvent e) {
                StackPane destinationSP = (StackPane)getChildren().get(e.getRow() * SIZE + e.getCol());
                // TODO: Having to know the index of elements within the cell seems like a huge design flaw. Solution: Make a custom class extending StackPane?

                destinationSP.getChildren().get(cellLayerIndices.LABEL.ordinal()).setVisible(false);
                destinationSP.getChildren().get(cellLayerIndices.WHITESTONE.ordinal()).setVisible(false);
                destinationSP.getChildren().get(cellLayerIndices.BLACKSTONE.ordinal()).setVisible(false);

            }
        });
    }

    // TODO: Call this from the main UI if moves are to be confirmed.
    // TODO: Immediately change lastMouseHover on completion (esp. if a situation arises where the mouse might be on the board during confirmation)
    // TODO: Although it might be said that the model should remain unchanged until confirmation, I am not sure whether this is really the responsibility of the view.
    /*public void confirmMove() {
        if(selectionTarget != null) {
            if(selectionHover != null) {
                selectionHover.setVisible(false);
            }
            Integer col = getColumnIndex(selectionTarget);
            Integer row = getRowIndex(selectionTarget);
            if(col != null && row != null) { // Remember to account for the inclusion of labels in the grid, which could potentially be at either end.
                board.setStone(col, row, board.getCurColor());
            } else {
                System.out.println("Confirmation outside of actual board on " + lastMouseTarget); // TODO: Remove in finished product
            }
        }
    }*/

    // Getters and Setters
    public boolean needsMoveConfirmation() {
        return needsMoveConfirmation;
    }
    public void setMoveConfirmation(boolean needsMoveConfirmation) {
        this.needsMoveConfirmation = needsMoveConfirmation;
    }

    public boolean showsMoveNumbers() {
        return false;
        // TODO: Implement
    }

    public void setShowsMoveNumbers() {
        // TODO: Implement
    }

    public boolean showsCoordinates() {
        return false;
        // TODO: Implement
    }

    public void setShowsCoordinates() {
        // TODO: Implement
    }
}
