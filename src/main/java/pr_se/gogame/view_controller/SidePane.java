package pr_se.gogame.view_controller;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.*;
import pr_se.gogame.model.Game;
import pr_se.gogame.model.GameCommand;

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
        VBox gameInfo = gameInfo();
        this.getChildren().add(gameSetting);

        game.addListener(l -> {
            if(l.getGameCommand() == GameCommand.INIT){
                if(!this.getChildren().contains(gameSetting)) {
                    //this.getChildren().add(mainBox);
                    this.getChildren().add(gameSetting);
                    this.getChildren().remove(gameInfo);
                }
            } else {
                //this.getChildren().remove(mainBox);
                this.getChildren().remove(gameSetting);
                this.getChildren().add(gameInfo);
            }
        });

    }

    private VBox gameInfo() {
        VBox infoPane = new VBox();

        HBox playerInfo = new HBox();

        Label currentPlayer = new Label();
        currentPlayer.setFont(Font.font(null, FontWeight.BOLD, 13));
        currentPlayer.setText("Current Player: ");
        playerInfo.getChildren().add(currentPlayer);

        Label actualPlayer = new Label();
        actualPlayer.setFont(Font.font(null,FontWeight.NORMAL,13));
        actualPlayer.setText(GameCommand.BLACKsTURN.toString());
        playerInfo.getChildren().add(actualPlayer);

        infoPane.getChildren().add(playerInfo);

        /*game.addListener(l -> {
            GameCommand command = l.getGameCommand();
            if (!(command.equals(GameCommand.BLACKsTURN) || command.equals(GameCommand.WHITEsTURN))) return;

            currentPlayer.setText("Current Player: " + ((command == GameCommand.BLACKsTURN) ? "Black":"White"));

        });*/

        Pane spring1 = new Pane();
        spring1.minHeightProperty().bind(currentPlayer.heightProperty());
        infoPane.getChildren().add(spring1);

        Label explanationLabel = new Label();
        explanationLabel.setFont(Font.font(null, FontWeight.BOLD, 13));
        explanationLabel.setText("Turn Explanation:");
        infoPane.getChildren().add(explanationLabel);


        //TextArea area = new TextArea();
        //area.setPrefHeight(250);
        //area.setPrefWidth(250);
        //area.setText("Lorem ipsum dolor sit amet, consectetuer adipiscing elit,\n sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat. Ut wisi enim ad minim veniam, quis nostrud exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo consequat. Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero et accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi. Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat. Ut wisi enim ad minim veniam, quis nostrud exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo consequat. Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero et accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi. Nam liber tempor cum soluta nobis eleifend option congue nihil imperdiet doming id quod mazim placerat facer possim assum.");////
        //infoPane.getChildren().add(area);

        ScrollPane textArea = new ScrollPane();

        TextFlow textFlow = new TextFlow();
        textFlow.setPadding(new Insets(3));
        textFlow.setTextAlignment(TextAlignment.JUSTIFY);
        textFlow.setPrefWidth(this.getWidth());
        textFlow.prefHeightProperty().bind(heightProperty());

        Text explanation = new Text();
        explanation.setText("Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat. Ut wisi enim ad minim veniam, quis nostrud exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo consequat. Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero et accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi. Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat. Ut wisi enim ad minim veniam, quis nostrud exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo consequat. Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero et accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi. Nam liber tempor cum soluta nobis eleifend option congue nihil imperdiet doming id quod mazim placerat facer possim assum.");//
        textFlow.getChildren().add(explanation);

        textArea.setContent(textFlow);
        textArea.setFitToWidth(true);

        infoPane.getChildren().add(textArea);


        return infoPane;
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
            game.newGame(GameCommand.BLACKSTARTS, actualBoardSize, handicap);
        });

        //colum, row,
        gridPane.add(startGameBtn, 1, 15);

        return gridPane;
    }
}
