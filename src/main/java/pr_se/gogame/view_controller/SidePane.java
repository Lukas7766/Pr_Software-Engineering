package pr_se.gogame.view_controller;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.converter.NumberStringConverter;

public class SidePane extends StackPane {

    public SidePane(){

        Rectangle mainBox = new Rectangle();
        mainBox.setFill(Color.LIGHTGRAY);

        //mainBox.setHeight(495);
        mainBox.setWidth(250);
        mainBox.heightProperty().bind(heightProperty().subtract(10));
        //mainBox.widthProperty().bind(widthProperty());

        setAlignment(mainBox, Pos.TOP_LEFT);
        setPadding(new Insets(2.5, 5,5,5)); //top, right, bottom, left
        setMinSize(0,0);

        this.getChildren().add(mainBox);


        this.getChildren().add(newGame());

    }

    private GridPane newGame(){

        GridPane gridPane = new GridPane();
        Label headline = new Label();
        headline.setText("Game Setup");
        headline.setFont(Font.font(null, FontWeight.BOLD, 15));
        headline.setPadding(new Insets(5));

        gridPane.add(headline,0,0);



        Button startGameBtn = new Button();
        startGameBtn.setText("start Game");
        startGameBtn.setAlignment(Pos.BOTTOM_RIGHT);

        //colum, row,
        gridPane.add(startGameBtn,1,15);

        ToggleGroup boardSize = new ToggleGroup();

        RadioButton tiny = new RadioButton("Tiny 9x9");
        tiny.setToggleGroup(boardSize);
        tiny.setPadding(new Insets(5));

        RadioButton small = new RadioButton("Small 13x13");
        small.setToggleGroup(boardSize);
        small.setPadding(new Insets(5));

        RadioButton normal = new RadioButton("Normal 19x19");
        normal.setToggleGroup(boardSize);
        normal.setPadding(new Insets(5));
        normal.setSelected(true);

        RadioButton custom = new RadioButton("Custom");
        custom.setToggleGroup(boardSize);
        custom.setPadding(new Insets(5));

        gridPane.add(tiny,0,2);
        gridPane.add(small,0,3);
        gridPane.add(normal,0,4);
        gridPane.add(custom,0,5);

        Spinner<Integer> customSize = new Spinner<>(0,25,0,1);
        //customSize.setDisable(true);
        customSize.setMaxSize(55,15);
        SpinnerValueFactory.IntegerSpinnerValueFactory intFactory =
                (SpinnerValueFactory.IntegerSpinnerValueFactory) customSize.getValueFactory();
        //TextField customSize = new TextField();
        //customSize.setTextFormatter(new TextFormatter<>(new NumberStringConverter()));
        gridPane.add(customSize,1,5);



        return gridPane;
    }
}
