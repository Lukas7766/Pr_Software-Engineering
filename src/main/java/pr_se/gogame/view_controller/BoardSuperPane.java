package pr_se.gogame.view_controller;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.NumberBinding;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
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
        // bp.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        final NumberBinding BOARD_ASPECT_RATIO = Bindings.min(
            widthProperty().subtract(coordsLeft.widthProperty()).subtract(coordsRight.widthProperty()),
            heightProperty().subtract(coordsAbove.heightProperty()).subtract(coordsBelow.heightProperty()));
        bp.maxHeightProperty().bind(BOARD_ASPECT_RATIO);
        bp.maxWidthProperty().bind(BOARD_ASPECT_RATIO);
        /*bp.prefWidthProperty().bind(bp.widthProperty());
        bp.prefHeightProperty().bind(bp.heightProperty());*/
        this.setCenter(bp);

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

        coordsAbove.maxWidthProperty().bind(bp.widthProperty());
        coordsBelow.maxWidthProperty().bind(bp.widthProperty());
        coordsLeft.maxHeightProperty().bind(bp.heightProperty());
        coordsRight.maxHeightProperty().bind(bp.heightProperty());

        setTop(coordsAbove);
        setBottom(coordsBelow);
        setLeft(coordsLeft);
        setRight(coordsRight);

        // setAlignment(bp, Pos.CENTER);
        setAlignment(coordsAbove, Pos.BOTTOM_CENTER);
        setAlignment(coordsBelow, Pos.TOP_CENTER);
        setAlignment(coordsLeft, Pos.CENTER_RIGHT);
        setAlignment(coordsRight, Pos.CENTER_LEFT);

        System.out.println("bL: " + coordsLeft.minWidthProperty().get());
        System.out.println("bA: " + coordsAbove.minWidthProperty().get());
        System.out.println("bR: " + coordsRight.minWidthProperty().get());
        // maxWidthProperty().bind(coordsLeft.maxWidthProperty().add(coordsAbove.maxWidthProperty()).add(coordsRight.maxWidthProperty()));

        // coordsRight.prefWidthProperty().bind(this.widthProperty().subtract(bp.widthProperty()).subtract(coordsLeft.widthProperty()));

        coordsRight.setMinWidth(100);
    }
}
