package pr_se.gogame.view_controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import pr_se.gogame.model.Board;
import javafx.scene.text.Font;

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
        coordsRight.setBackground(new Background(new BackgroundFill(Color.LIGHTPINK, null, null)));

        bp = null; // new BoardPane(board, tile0, tile1, stone0, stone1);

        getChildren().add(bp);

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
        getChildren().add(coordsBelow);
        getChildren().add(coordsLeft);
        getChildren().add(coordsRight);
    }

    public void fitItSnuggly() {
        coordsRight.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                double offset = t1.doubleValue();
                // System.out.println("Right anchor before: " + getRightAnchor(bp));
                // System.out.println("-----------------Debug info just before: ");
                // bp.printDebugInfo();
                // System.out.println("Setting it to " + offset);
                setRightAnchor(bp, offset);
                // System.out.println("Right anchor now: " + getRightAnchor(bp));
            }
        });

        coordsLeft.widthProperty().addListener((o, n, t) -> {
            double offset = t.doubleValue();
            setLeftAnchor(bp, offset);
        });

        coordsAbove.heightProperty().addListener((o, n, t) -> {
            double offset = t.doubleValue();
            setTopAnchor(bp, offset);
        });

        coordsBelow.heightProperty().addListener((o, n, t) -> {
            double offset = t.doubleValue();
            setBottomAnchor(bp, offset);
        });

        bp.addActualChangeListener((o, n, t) -> {
            if(getRightAnchor(bp) != null && getLeftAnchor(bp) != null && getTopAnchor(bp) != null && getBottomAnchor(bp) != null) {
                double newRightAnchor = bp.getDeadWidthAtRight() + getRightAnchor(bp);
                setRightAnchor(coordsRight, newRightAnchor - coordsRight.getWidth());
                setRightAnchor(coordsAbove, newRightAnchor);
                setRightAnchor(coordsBelow, newRightAnchor);

                double newLeftAnchor = bp.getDeadWidthAtLeft() + getLeftAnchor(bp);
                setLeftAnchor(coordsLeft, newLeftAnchor - coordsLeft.getWidth());
                setLeftAnchor(coordsAbove, newLeftAnchor);
                setLeftAnchor(coordsBelow, newLeftAnchor);

                updateFontSize(coordsAbove, coordsAbove.getWidth());
                updateFontSize(coordsBelow, coordsBelow.getWidth());

                double newTopAnchor = bp.getDeadHeightAtTop() + getTopAnchor(bp);
                setTopAnchor(coordsAbove, newTopAnchor - coordsAbove.getHeight());
                setTopAnchor(coordsLeft, newTopAnchor);
                setTopAnchor(coordsRight, newTopAnchor);

                double newBottomAnchor = bp.getDeadHeightAtBottom() + getBottomAnchor(bp);
                setBottomAnchor(coordsBelow, newBottomAnchor - coordsBelow.getHeight());
                setBottomAnchor(coordsLeft, newBottomAnchor);
                setBottomAnchor(coordsRight, newBottomAnchor);

                updateFontSize(coordsLeft, coordsLeft.getHeight());
                updateFontSize(coordsRight, coordsRight.getHeight());
            }
        });
    }

    private void updateFontSize(Pane coords, double dimension) {
        for(Node node : coords.getChildren()) {
            Label l = (Label)node;
            double newFontSize = (dimension / 2) / this.BOARD_SIZE - l.getText().length();
            Font f = new Font(newFontSize);
            l.setFont(f);
        }
    }

    // TODO: Remove in finished product
    public void printDebugInfo() {
        bp.printDebugInfo();
    }
}
