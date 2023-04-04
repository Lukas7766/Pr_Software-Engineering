package pr_se.gogame.view_controller;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import pr_se.gogame.model.Game;

public class SidePane extends StackPane {

    private final Game game;

    public SidePane(Game game) {
        this.game = game;

        Rectangle mainBox = new Rectangle();
        mainBox.setFill(Color.LIGHTGRAY);

        //mainBox.setHeight(495);
        mainBox.setWidth(250);
        mainBox.heightProperty().bind(heightProperty().subtract(10));
        //mainBox.widthProperty().bind(widthProperty());

        setAlignment(mainBox, Pos.TOP_LEFT);
        setPadding(new Insets(2.5, 5, 5, 5)); //top, right, bottom, left
        setMinSize(0, 0);

        this.getChildren().add(mainBox);


        GridPane gameSetting = newGame();
        this.getChildren().add(gameSetting);

        game.addListener(l -> {
            this.getChildren().remove(gameSetting);
        });

    }

    private GridPane newGame() {

        GridPane gridPane = new GridPane();
        Label headline = new Label();
        headline.setText("Game Setup");
        headline.setFont(Font.font(null, FontWeight.BOLD, 15));
        headline.setPadding(new Insets(5));

        gridPane.add(headline, 0, 0);

        //Board Size
        ToggleGroup boardSize = new ToggleGroup();

        RadioButton tiny = new RadioButton("Tiny 9x9");
        tiny.setId("9");
        tiny.setFont(Font.font(null, FontWeight.NORMAL, 13));
        tiny.setToggleGroup(boardSize);
        tiny.setPadding(new Insets(5));

        RadioButton small = new RadioButton("Small 13x13");
        small.setId("13");
        small.setFont(Font.font(null, FontWeight.NORMAL, 13));
        small.setToggleGroup(boardSize);
        small.setPadding(new Insets(5));

        RadioButton normal = new RadioButton("Normal 19x19");
        normal.setId("19");
        normal.setFont(Font.font(null, FontWeight.NORMAL, 13));
        normal.setToggleGroup(boardSize);
        normal.setPadding(new Insets(5));
        normal.setSelected(true);


        RadioButton custom = new RadioButton("Custom");
        custom.setId("custom");
        custom.setFont(Font.font(null, FontWeight.NORMAL, 13));
        custom.setToggleGroup(boardSize);
        custom.setPadding(new Insets(5));

        gridPane.add(tiny, 0, 2);
        gridPane.add(small, 0, 3);
        gridPane.add(normal, 0, 4);
        gridPane.add(custom, 0, 5);

        Spinner<Integer> customSize = new Spinner<>(9, 25, 9, 1);
        customSize.setDisable(true);
        customSize.setMaxSize(55, 15);
        SpinnerValueFactory.IntegerSpinnerValueFactory customSizeIntFactory =
                (SpinnerValueFactory.IntegerSpinnerValueFactory) customSize.getValueFactory();
        //TextField customSize = new TextField();
        //customSize.setTextFormatter(new TextFormatter<>(new NumberStringConverter()));
        gridPane.add(customSize, 1, 5);

        //change Event
        boardSize.selectedToggleProperty().addListener((observableValue, toggle, t1) -> {
            customSize.setDisable(t1.hashCode() != custom.hashCode());
            System.out.println(((RadioButton) t1).getText());
        });
        //Komi
        Label komi = new Label();
        komi.setFont(Font.font(null, FontWeight.NORMAL, 13));
        komi.setText("Komi");
        komi.setPadding(new Insets(5));
        gridPane.add(komi, 0, 6);

        Spinner<Integer> komiCnt = new Spinner<>(0, 9, 0, 1);
        //customSize.setDisable(true);
        komiCnt.setMaxSize(55, 15);
        SpinnerValueFactory.IntegerSpinnerValueFactory komiIntFactory =
                (SpinnerValueFactory.IntegerSpinnerValueFactory) komiCnt.getValueFactory();
        //TextField customSize = new TextField();
        //customSize.setTextFormatter(new TextFormatter<>(new NumberStringConverter()));
        gridPane.add(komiCnt, 1, 6);


        //start Button
        Button startGameBtn = new Button();
        startGameBtn.setText("start Game");
        startGameBtn.setAlignment(Pos.BOTTOM_RIGHT);
        startGameBtn.setOnAction(event -> {
            //this.getChildren().remove(gridPane);
            RadioButton selected = (RadioButton) boardSize.getSelectedToggle();
            int handicap = komiIntFactory.getValue();
            int actualBoardSize;

            if (selected.getId().equals("custom")) actualBoardSize = customSizeIntFactory.getValue();
            else actualBoardSize = Integer.parseInt(selected.getId());

            System.out.println("BoardSize: " + actualBoardSize + " Handicap: " + handicap);
            game.newGame(actualBoardSize, handicap);
        });

        //colum, row,
        gridPane.add(startGameBtn, 1, 15);

        return gridPane;
    }
}
