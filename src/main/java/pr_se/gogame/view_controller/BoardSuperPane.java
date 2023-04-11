package pr_se.gogame.view_controller;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import pr_se.gogame.model.Board;

public class BoardSuperPane extends OrbitalBorderPane {

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
        coordsRight.setBackground(new Background(new BackgroundFill(Color.BEIGE, null, null)));

        bp = new BoardPane(
            board,
            tile0,
            tile1,
            stone0,
            stone1,
            // widthProperty().subtract(this.BOARD_SIZE * 2),
            Bindings.min(widthProperty(), widthProperty()),
            // widthProperty().subtract(coordsLeft.widthProperty()).subtract(coordsRight.widthProperty()),
            // heightProperty().subtract(this.BOARD_SIZE * 2)
            Bindings.min(heightProperty(), heightProperty())
            // heightProperty().subtract(coordsAbove.heightProperty()).subtract(coordsBelow.heightProperty())
        );
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
        /*NumberAxis na = new NumberAxis(0.0, 19.0, 1.0);
        setRight(na);*/

        // setAlignment(bp, Pos.CENTER);
        setAlignment(getTop(), Pos.BOTTOM_CENTER);
        setAlignment(getBottom(), Pos.TOP_CENTER);
        setAlignment(getLeft(), Pos.CENTER_RIGHT);
        setAlignment(getRight(), Pos.CENTER_LEFT);

        /*coordsAbove.setPadding(new Insets(10.0, 0, 0, 0));
        coordsRight.setPadding(new Insets(0, 10.0, 0, 0));
        coordsBelow.setPadding(new Insets(0, 0, 10.0, 0));
        coordsLeft.setPadding(new Insets(0, 0, 0, 10.0));*/

        /*setMargin(coordsAbove, new Insets(10.0, 0, 0, 0));
        setMargin(coordsRight, new Insets(0, 10.0, 0, 0));
        setMargin(coordsBelow, new Insets(0, 0, 10.0, 0));
        setMargin(coordsLeft, new Insets(0, 0, 0, 10.0));*/

    }

    @Override
    protected void layoutChildren() {
        updateFontSize(coordsAbove, coordsAbove.getWidth());
        updateFontSize(coordsBelow, coordsBelow.getWidth());
        updateFontSize(coordsLeft, coordsLeft.getHeight());
        updateFontSize(coordsRight, coordsRight.getHeight());

        super.layoutChildren();
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

    public int getBOARD_SIZE() {
        return BOARD_SIZE;
    }

    public BoardPane getBp() {
        return bp;
    }

    public VBox getCoordsRight() {
        return coordsRight;
    }

    public VBox getCoordsLeft() {
        return coordsLeft;
    }

    public HBox getCoordsAbove() {
        return coordsAbove;
    }

    public HBox getCoordsBelow() {
        return coordsBelow;
    }
}
