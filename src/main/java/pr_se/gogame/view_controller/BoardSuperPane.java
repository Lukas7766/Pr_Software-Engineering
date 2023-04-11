package pr_se.gogame.view_controller;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import pr_se.gogame.model.Board;

import java.util.Objects;

public class BoardSuperPane extends BorderPane {

    private int BOARD_SIZE;
    BoardPane bp;
    VBox coordsRight, coordsLeft;
    HBox coordsAbove, coordsBelow;

    public BoardSuperPane(Board board, String tile0, String tile1, String stone0, String stone1) {
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
        /*final NumberBinding BOARD_ASPECT_RATIO = Bindings.min(
            widthProperty().subtract(coordsLeft.widthProperty()).subtract(coordsRight.widthProperty()),
            heightProperty().subtract(coordsAbove.heightProperty()).subtract(coordsBelow.heightProperty()));
        bp.maxHeightProperty().bind(BOARD_ASPECT_RATIO);
        bp.maxWidthProperty().bind(BOARD_ASPECT_RATIO);*/
        setCenter(bp);

        for(int i = 0; i < this.BOARD_SIZE; i++) {
            Label aboveLabel = new Label("" + (char)('A' + i));
            aboveLabel.setMaxWidth(Double.MAX_VALUE);
            aboveLabel.setAlignment(Pos.BOTTOM_CENTER);
            // makeLabelSizeDynamic(aboveLabel, coordsAbove.widthProperty());

            Label belowLabel = new Label("" + (char)('A' + i));
            belowLabel.setMaxWidth(Double.MAX_VALUE);
            belowLabel.setAlignment(Pos.TOP_CENTER);
            // makeLabelSizeDynamic(belowLabel, coordsBelow.widthProperty());

            Label leftLabel = new Label("" + (BOARD_SIZE - i));
            leftLabel.setMaxHeight(Double.MAX_VALUE);
            leftLabel.setAlignment(Pos.CENTER_RIGHT);
            // makeLabelSizeDynamic(leftLabel, coordsLeft.heightProperty());

            Label rightLabel = new Label("" + (BOARD_SIZE - i));
            rightLabel.setMaxHeight(Double.MAX_VALUE);
            rightLabel.setAlignment(Pos.CENTER_LEFT);
            // makeLabelSizeDynamic(rightLabel, coordsRight.heightProperty());

            coordsAbove.getChildren().add(aboveLabel);
            coordsBelow.getChildren().add(belowLabel);
            coordsLeft.getChildren().add(leftLabel);
            coordsRight.getChildren().add(rightLabel);

            coordsAbove.setHgrow(aboveLabel, Priority.ALWAYS);
            coordsBelow.setHgrow(belowLabel, Priority.ALWAYS);
            coordsLeft.setVgrow(leftLabel, Priority.ALWAYS);
            coordsRight.setVgrow(rightLabel, Priority.ALWAYS);
        }

        /*coordsAbove.maxWidthProperty().bind(bp.widthProperty());
        coordsBelow.maxWidthProperty().bind(bp.widthProperty());
        coordsLeft.maxHeightProperty().bind(bp.heightProperty());
        coordsRight.maxHeightProperty().bind(bp.heightProperty());*/

        setTop(coordsAbove);
        setBottom(coordsBelow);
        setLeft(coordsLeft);
        setRight(coordsRight);

        // setAlignment(bp, Pos.CENTER);
        setAlignment(coordsAbove, Pos.BOTTOM_CENTER);
        setAlignment(coordsBelow, Pos.TOP_CENTER);
        setAlignment(coordsLeft, Pos.CENTER_RIGHT);
        setAlignment(coordsRight, Pos.CENTER_LEFT);
    }

    @Override
    protected void layoutChildren() {
        updateFontSize(coordsAbove, coordsAbove.getWidth());
        updateFontSize(coordsBelow, coordsBelow.getWidth());
        updateFontSize(coordsLeft, coordsLeft.getHeight());
        updateFontSize(coordsRight, coordsRight.getHeight());

        super.layoutChildren();

        if(true) {
            getTop().resizeRelocate(
                getCenter().getLayoutX(),
                getCenter().getLayoutY() - getTop().getLayoutBounds().getHeight(),
                getCenter().getLayoutBounds().getWidth(),
                getTop().getLayoutBounds().getHeight());

            getBottom().resizeRelocate(
                getCenter().getLayoutX(),
                getCenter().getBoundsInParent().getMaxY(),
                getCenter().getLayoutBounds().getWidth(),
                getBottom().getLayoutBounds().getHeight());

            getLeft().resizeRelocate(
                getCenter().getLayoutX() - getLeft().getLayoutBounds().getWidth(),
                getCenter().getLayoutY(),
                getLeft().getLayoutBounds().getWidth(),
                getCenter().getLayoutBounds().getHeight());

            getRight().resizeRelocate(
                getCenter().getBoundsInParent().getMaxX(),
                getCenter().getLayoutY(),
                getRight().getLayoutBounds().getWidth(),
                getCenter().getLayoutBounds().getHeight());
        } else {
            layoutChildrenHack();
        }

    }

    private void layoutChildrenHack() {
        getTop().resizeRelocate(
            bp.getLayoutX() + bp.getDeadWidthAtLeft(),
            bp.getLayoutY() + bp.getDeadHeightAtTop() - getTop().getLayoutBounds().getHeight(),
            bp.getTotalContentWidth(),
            getTop().getLayoutBounds().getHeight());

        getBottom().resizeRelocate(
            bp.getLayoutX() + bp.getDeadWidthAtLeft(),
            bp.getBoundsInParent().getMaxY() - bp.getDeadHeightAtBottom(),
            bp.getTotalContentWidth(),
            getBottom().getLayoutBounds().getHeight());

        getLeft().resizeRelocate(
            bp.getLayoutX() + bp.getDeadWidthAtLeft() - getLeft().getLayoutBounds().getWidth(),
            bp.getLayoutY() + bp.getDeadHeightAtTop(),
            getLeft().getLayoutBounds().getWidth(),
            bp.getTotalContentHeight());

        getRight().resizeRelocate(
            bp.getBoundsInParent().getMaxX() - bp.getDeadWidthAtRight(),
            bp.getLayoutY() + bp.getDeadHeightAtTop(),
            getRight().getLayoutBounds().getWidth(),
            bp.getTotalContentHeight());
    }

    /*private void makeLabelSizeDynamic(Label l, ReadOnlyDoubleProperty dimProperty) {
        final DoubleProperty FONT_SIZE = new SimpleDoubleProperty(0);
        FONT_SIZE.bind(dimProperty.divide(2).divide(this.BOARD_SIZE).subtract(Bindings.length(l.textProperty())));
        l.styleProperty().bind(Bindings.concat("-fx-font-size: ", FONT_SIZE));
    }*/

    private void updateFontSize(Pane coords, double dimension) {
        for(Node node : coords.getChildren()) {
            Label l = (Label)node;
            double newFontSize = (dimension / 2) / this.BOARD_SIZE - l.getText().length();
            Font f = new Font(newFontSize);
            l.setFont(f);
        }
    }
}
