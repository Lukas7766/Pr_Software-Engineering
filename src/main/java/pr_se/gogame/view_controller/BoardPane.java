package pr_se.gogame.view_controller;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import pr_se.gogame.model.Board;
import pr_se.gogame.model.StoneColor;

/**
 * View/Controller
 * Go Board that uses image files for its (checkerboard) tiles and stones
 */
public class BoardPane extends GridPane {

    private final int SIZE;                         // number of columns and rows, respectively
    private boolean needsMoveConfirmation = false;  // whether moves have to be confirmed separately; TODO: might need a better name
    private Board board;                      // Model Dummy for MVC-adherence; might also be called "game"

    /*
     * Custom resources
     */
    private final Image[] tiles = new Image [2];
    private final Image[] stones = new Image [2];

    // TODO: This sort of thing would ideally be cleaned up by using some sort of data structure.
    private Node lastMouseTarget = null;

    private StackPane lastTargetSP = null;
    private ImageView lastMouseHover = null;
    private Node selectionTarget = null;
    private ImageView selectionHover = null;

    // TODO: Maybe move constructor content into an init() method, especially with regards to loading images (as those might be changed during a game).
    public BoardPane(Board board, String tile0, String tile1, String stone0, String stone1) {
        setBoard(board);
        this.SIZE = board.getSize();

        // TODO: In the end product, the files would be chosen by the user (and perhaps packaged in an archive)
        tiles[0] = new Image(tile0, true);
        tiles[1] = new Image(tile1, true);

        stones[0] = new Image(stone0, true);
        stones[1] = new Image(stone1, true);

        // Graphical details of this board pane
        setAlignment(Pos.CENTER);
        setHgap(0);
        setVgap(0);
        setPadding(new Insets(0, 0, 2.5, 0));
        setMinSize(0, 0);

        // setGridLinesVisible(true);

        // Fill the grid with ImageViews of alternating tiles
        for(int i = 0; i < this.SIZE; i++) {
            for(int j = 0; j < this.SIZE; j++) {
                StackPane sp = new StackPane();

                ImageView iv = new ImageView(tiles[(j % 2 + i % 2) % 2]);
                iv.setPreserveRatio(true);
                iv.fitHeightProperty().bind(heightProperty().subtract(this.SIZE).divide(this.SIZE));
                iv.fitWidthProperty().bind(widthProperty().subtract(this.SIZE).divide(this.SIZE));
                iv.setMouseTransparent(true);

                sp.getChildren().add(iv);

                Label l = new Label("0");
                l.setVisible(false);
                sp.getChildren().add(l);

                add(sp, j, i);
            }
        }

        // Set up listeners
        setOnMouseMoved(e -> {
            Node target = (Node)e.getTarget();
            System.out.println("Target is of class " + target.getClass());
            if(target != null) {
                if(target != lastMouseTarget) {                                 // TODO: This seems to fire a bit too readily, making the program run less efficiently. I am not sure why, though.
                    Integer col = getColumnIndex(target);
                    Integer row = getRowIndex(target);

                    if (col != null && row != null) {
                        StackPane targetSP = (StackPane)target;
                        this.board.printDebugInfo(col, row);

                        System.out.println("Hover over " + col + " " + row);    // TODO: Remove in finished product
                        Image stoneImg;
                        if (this.board.getCurColor() == StoneColor.BLACK) {
                            stoneImg = stones[0];
                        } else {
                            stoneImg = stones[1];
                        }

                        ImageView iv = new ImageView(stoneImg);
                        iv.fitHeightProperty().bind(heightProperty().subtract(this.SIZE).divide(this.SIZE));
                        iv.fitWidthProperty().bind(widthProperty().subtract(this.SIZE).divide(this.SIZE));
                        iv.setPreserveRatio(true);
                        iv.setOpacity(0.5);
                        iv.setMouseTransparent(true);

                        targetSP.getChildren().add(iv);

                        // Remove old hover
                        if(lastTargetSP != null) {
                            lastTargetSP.getChildren().remove(lastMouseHover);
                        }
                        //System.out.println("Removed hover!");               // TODO: Remove in finished product
                        lastMouseTarget = target;
                        lastTargetSP = targetSP;
                        lastMouseHover = iv;
                    } else {
                        //System.out.println("Hover target is not a cell!");  // TODO: Remove in finished product
                        if(lastTargetSP != null) {
                            lastTargetSP.getChildren().remove(lastMouseHover);
                        }
                        lastMouseTarget = null;
                        // lastTargetSP = null;
                        lastMouseHover = null;
                    }
                }
            } else {
                //System.out.println("Hover target is null!");                // TODO: Remove in finished product
                lastMouseTarget = null;
                // lastTargetSP = null;
                lastMouseHover = null;
            }
        });

        setOnMouseClicked(e -> {
            if(e.getButton() == MouseButton.PRIMARY) { // This check is only for testing independently of the main UI.
                if (lastMouseTarget != null) {
                    Integer col = getColumnIndex(lastMouseTarget);
                    Integer row = getRowIndex(lastMouseTarget);
                    if (col != null && row != null) { // TODO: Remember to account for the inclusion of labels in the grid, which could potentially be at either end.
                        if (selectionHover != null && lastTargetSP != null) {
                            lastTargetSP.getChildren().remove(selectionHover);
                        }
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
        });


    }

    public void setBoard(Board board) {
        this.board = board;

        board.addListener(new GoListener() {
            @Override
            public void stoneSet(StoneSetEvent e) {
                Image stoneImg;
                Color labelColor = null;
                if(e.getColor() == StoneColor.BLACK) {
                    stoneImg = stones[0];
                    labelColor = Color.rgb(255, 255, 255);
                } else {
                    stoneImg = stones[1];
                    labelColor = Color.rgb(0, 0, 0);
                }

                ImageView iv = new ImageView(stoneImg);
                iv.fitHeightProperty().bind(heightProperty().subtract(SIZE).divide(SIZE));
                iv.fitWidthProperty().bind(widthProperty().subtract(SIZE).divide(SIZE));
                iv.setPreserveRatio(true);
                iv.setMouseTransparent(true);

                StackPane destinationSP = (StackPane)getChildren().get(e.getRow() * SIZE + e.getCol());
                destinationSP.getChildren().add(iv);

                Label l = (Label)destinationSP.getChildren().get(destinationSP.getChildren().size() - 2);
                l.setText("" + e.getMoveNumber());
                l.setTextFill(labelColor);
                l.toFront();
                l.setVisible(true);
            }

            @Override
            public void stoneRemoved(StoneRemovedEvent e) {
                ImageView needle = null;
                StackPane destinationSP = (StackPane)getChildren().get(e.getRow() * SIZE + e.getCol());
                // TODO: Having to know the index of elements within the cell seems like a huge design flaw. Solution: Make a custom class extending StackPane?
                destinationSP.getChildren().remove(destinationSP.getChildren().size() - 1);
                // destinationSP.getChildren().get(destinationSP.getChildren().size() - 1).toBack();

                /*for(Node iv : getChildren()) {
                    if(getRowIndex(iv) == e.getRow() && getColumnIndex(iv) == e.getCol()) {
                        needle = (ImageView)iv;
                        if(needle.getImage() == stones[0] || needle.getImage() == stones[1]) {
                            break;
                        } else {
                            needle = null;
                        }
                    }
                }*/

                /*if(needle != null) {
                    getChildren().remove(needle);
                }*/
            }
        });
    }

    // TODO: Call this from the main UI if moves are to be confirmed.
    // TODO: Immediately change lastMouseHover on completion (esp. if a situation arises where the mouse might be on the board during confirmation)
    // TODO: Although it might be said that the model should remain unchanged until confirmation, I am not sure whether this is really the responsibility of the view.
    public void confirmMove() {
        if(selectionTarget != null) {
            if(lastTargetSP != null) {
                lastTargetSP.getChildren().remove(selectionHover);
            }
            Integer col = getColumnIndex(selectionTarget);
            Integer row = getRowIndex(selectionTarget);
            if(col != null && row != null) { // Remember to account for the inclusion of labels in the grid, which could potentially be at either end.
                board.setStone(col, row, board.getCurColor());
            } else {
                System.out.println("Confirmation outside of actual board on " + lastMouseTarget); // TODO: Remove in finished product
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
