package pr_se.gogame.view_controller;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import pr_se.gogame.model.Board;

public class BoardSuperPane2 extends AnchorPane {

    private int BOARD_SIZE;

    private BoardPane bp;

    VBox coordsRight, coordsLeft;
    HBox coordsAbove, coordsBelow;
    public BoardSuperPane2(Board board, String tile0, String tile1, String stone0, String stone1) {
        setMinSize(0, 0);
        this.BOARD_SIZE = board.getSize();

        this.coordsRight = new VBox();
        this.coordsLeft = new VBox();
        this.coordsAbove = new HBox();
        this.coordsBelow = new HBox();

        // TODO: Remove Debug code
        setBackground(new Background(new BackgroundFill(Color.DARKGRAY, null, null)));
        coordsAbove.setBackground(new Background(new BackgroundFill(Color.CYAN, null, null)));
        coordsBelow.setBackground(new Background(new BackgroundFill(Color.YELLOW, null, null)));
        coordsLeft.setBackground(new Background(new BackgroundFill(Color.GREEN, null, null)));
        coordsRight.setBackground(new Background(new BackgroundFill(Color.BLUE, null, null)));

        bp = new BoardPane(board, tile0, tile1, stone0, stone1);
        // bp.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        final NumberBinding BOARD_ASPECT_RATIO = Bindings.min(
                widthProperty().subtract(coordsLeft.widthProperty()).subtract(coordsRight.widthProperty()),
                heightProperty().subtract(coordsAbove.heightProperty()).subtract(coordsBelow.heightProperty()));
        /*bp.maxHeightProperty().bind(BOARD_ASPECT_RATIO);
        bp.maxWidthProperty().bind(BOARD_ASPECT_RATIO);*/

        getChildren().add(bp);
        /*setTopAnchor(bp, 0.0);
        setBottomAnchor(bp, 0.0);
        setLeftAnchor(bp, 0.0);
        setRightAnchor(bp, 0.0);*/

        for(int i = 0; i < this.BOARD_SIZE; i++) {
            Label aboveLabel = new Label("" + (char)('A' + i));
            aboveLabel.setMaxWidth(Double.MAX_VALUE);
            aboveLabel.setAlignment(Pos.BOTTOM_CENTER);
            Label belowLabel = new Label("" + (char)('A' + i));
            belowLabel.setMaxWidth(Double.MAX_VALUE);
            belowLabel.setAlignment(Pos.TOP_CENTER);
            Label leftLabel = new Label("" + (BOARD_SIZE - i));
            leftLabel.setMaxHeight(Double.MAX_VALUE);
            leftLabel.setAlignment(Pos.CENTER_RIGHT);
            Label rightLabel = new Label("" + (BOARD_SIZE - i));
            rightLabel.setMaxHeight(Double.MAX_VALUE);
            rightLabel.setAlignment(Pos.CENTER_LEFT);

            coordsAbove.getChildren().add(aboveLabel);
            coordsBelow.getChildren().add(belowLabel);
            coordsLeft.getChildren().add(leftLabel);
            coordsRight.getChildren().add(rightLabel);

            coordsAbove.setHgrow(aboveLabel, Priority.ALWAYS);
            coordsBelow.setHgrow(belowLabel, Priority.ALWAYS);
            coordsLeft.setVgrow(leftLabel, Priority.ALWAYS);
            coordsRight.setVgrow(rightLabel, Priority.ALWAYS);
        }

        fitItSnuggly();

        coordsAbove.maxWidthProperty().bind(bp.widthProperty());
        coordsBelow.maxWidthProperty().bind(bp.widthProperty());
        coordsLeft.maxHeightProperty().bind(bp.heightProperty());
        coordsRight.maxHeightProperty().bind(bp.heightProperty());

        getChildren().add(coordsAbove);
        setTopAnchor(coordsAbove, 0.0);
        getChildren().add(coordsBelow);
        setBottomAnchor(coordsBelow, 0.0);
        getChildren().add(coordsLeft);
        setLeftAnchor(coordsLeft, 0.0);
        getChildren().add(coordsRight);
        setRightAnchor(coordsRight, 0.0);

        // setAlignment(bp, Pos.CENTER);
        /*setAlignment(coordsAbove, Pos.BOTTOM_CENTER);
        setAlignment(coordsBelow, Pos.TOP_CENTER);
        setAlignment(coordsLeft, Pos.CENTER_RIGHT);
        setAlignment(coordsRight, Pos.CENTER_LEFT);*/

        System.out.println("bL: " + coordsLeft.minWidthProperty().get());
        System.out.println("bA: " + coordsAbove.minWidthProperty().get());
        System.out.println("bR: " + coordsRight.minWidthProperty().get());
        // maxWidthProperty().bind(coordsLeft.maxWidthProperty().add(coordsAbove.maxWidthProperty()).add(coordsRight.maxWidthProperty()));

        // coordsRight.prefWidthProperty().bind(this.widthProperty().subtract(bp.widthProperty()).subtract(coordsLeft.widthProperty()));

        // coordsRight.setMinWidth(100);

        /*maxWidthProperty().bind(coordsLeft.widthProperty().add(coordsRight.widthProperty()).add(coordsAbove.widthProperty()));
        maxHeightProperty().bind(coordsLeft.heightProperty().add(coordsAbove.heightProperty()).add(coordsBelow.heightProperty()));*/
    }

    public void fitItSnuggly() {
        coordsRight.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                double offset = t1.doubleValue();
                System.out.println("Right anchor before: " + getRightAnchor(bp));
                // System.out.println("First element bounds before: " + bp.getFirstBC().getBoundsInParent());
                System.out.println("-----------------Debug info just before: ");
                bp.printDebugInfo();
                System.out.println("Setting it to " + offset + " - " + bp.getDeadWidthAtRight());
                setRightAnchor(bp, offset/* - bp.getDeadWidthAtRight()*/);
                System.out.println("Right anchor now: " + getRightAnchor(bp));
                setRightAnchor(coordsAbove, offset);
                setRightAnchor(coordsBelow, offset);
                // setRightAnchor(coordsRight, bp.getDeadWidthAtRight());
            }
        });
        coordsLeft.widthProperty().addListener((o, n, t) -> {
            double offset = t.doubleValue();
            setLeftAnchor(bp, offset);
            // System.out.println("Left anchor now: " + getLeftAnchor(bp));
            setLeftAnchor(coordsAbove, offset);
            setLeftAnchor(coordsBelow, offset);
        });

        coordsAbove.heightProperty().addListener((o, n, t) -> {
            double offset = t.doubleValue();
            setTopAnchor(bp, offset);
            setTopAnchor(coordsLeft, offset);
            setTopAnchor(coordsRight, offset);
        });
        coordsBelow.heightProperty().addListener((o, n, t) -> {
            double offset = t.doubleValue();
            setBottomAnchor(bp, offset);
            setBottomAnchor(coordsLeft, offset);
            setBottomAnchor(coordsRight, offset);
        });

        /*bp.widthProperty().addListener((o, n, t) -> {
            System.out.println("----------Board width has changed:");
            bp.printDebugInfo();
            System.out.println("Dead width at right is now: " + bp.getDeadWidthAtRight());
        });*/

        bp.addActualChangeListener((o, n, t) -> {
            // if(counter == 0) {
                System.out.println("-----------------Actual Board size has changed:");
                bp.printDebugInfo();
                System.out.println("Dead width now: " + bp.getDeadWidthAtRight());
                System.out.println("Offset is " + getRightAnchor(bp));
                // setRightAnchor(bp, getRightAnchor(bp) - bp.getDeadWidthAtRight());
                System.out.println("******* Right bar is currently at: " + getRightAnchor(coordsRight));
                setRightAnchor(coordsRight, (bp.getDeadWidthAtRight() + getRightAnchor(bp)) - coordsRight.getWidth());
                System.out.println("******* Right bar is afterwards at: " + getRightAnchor(coordsRight));
                setLeftAnchor(coordsLeft, (bp.getDeadWidthAtLeft() + getLeftAnchor(bp)) - coordsLeft.getWidth());
                counter++;
            // }
        });
    }

    private int counter = 0;

    public void printDebugInfo() {
        bp.printDebugInfo();
    }
}
