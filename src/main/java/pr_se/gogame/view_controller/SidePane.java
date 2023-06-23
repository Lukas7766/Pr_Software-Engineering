package pr_se.gogame.view_controller;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import pr_se.gogame.model.Game;
import pr_se.gogame.model.StoneColor;
import pr_se.gogame.model.ruleset.JapaneseRuleset;

/**
 * This class contains the controller and view function of the game information panel.<br>
 * It is recommended to place the panel on the left or right side of the application.
 */
public class SidePane extends StackPane {

    /**
     * instance of actual game
     */
    private final Game game;

    /**
     * Constructor to create a SidePane
     *
     * @param game instance of actual game -> needed for triggering and observing changes in model
     */
    public SidePane(Color backColor, Stage stage, Game game) {
        this.game = game;
        this.setBackground(new Background(new BackgroundFill(backColor, new CornerRadii(5), new Insets(5, 2.5, 5, 5))));
        this.setMinWidth(250);
        setPadding(new Insets(5, 5, 5, 5)); //top, right, bottom, left


        GridPane gameSetting = newGame();
        VBox gameInfo = gameInfo();
        this.getChildren().add(gameSetting);

        game.addListener(e -> {
            if (e == null) {
                throw new NullPointerException();
            }

            switch (e.getGameCommand()) {
                case INIT:
                    if (!this.getChildren().contains(gameSetting)) {
                        this.getChildren().add(gameSetting);
                        this.getChildren().remove(gameInfo);
                    }
                    break;

                case NEW_GAME:
                    if (!this.getChildren().contains(gameInfo)) {
                        this.getChildren().remove(gameSetting);
                        this.getChildren().add(gameInfo);
                    }
                    break;

                case GAME_WON:
                    CustomWinAction.winAction(stage, game);
                    break;
            }
        });
    }

    /**
     * GameInformation contains a mechanism to show relevant information based on current GameCommand <br>
     * contains at least: <br>
     * -> Game Board <br>
     * -> Turn Explanation <br>
     *
     * @return a VBox which contains items to show relevant game info.
     */
    private VBox gameInfo() {
        VBox infoPane = new VBox();
        infoPane.setPadding(new Insets(5));

        Label gameBoardLbl = new Label("Game Board");
        gameBoardLbl.setFont(Font.font(null, FontWeight.BOLD, 24));
        infoPane.getChildren().add(gameBoardLbl);

        //-----------------------------------------
        //Player Info
        HBox playerInfo = new HBox();

        Label currentPlayer = new Label();
        currentPlayer.setFont(Font.font(null, FontWeight.BOLD, 13));
        currentPlayer.setText("Current Player: ");
        playerInfo.getChildren().add(currentPlayer);

        Label actualPlayer = new Label();
        actualPlayer.setFont(Font.font(null, FontWeight.NORMAL, 13));

        playerInfo.getChildren().add(actualPlayer);

        infoPane.getChildren().add(playerInfo);

        Pane spring1 = new Pane();
        spring1.minHeightProperty().bind(currentPlayer.heightProperty());
        infoPane.getChildren().add(spring1);

        //-----------------------------------------
        //Score Board
        VBox scoreboard = new VBox();

        Label scoreLbl = new Label("Score");
        scoreLbl.setFont(Font.font(null, FontWeight.BOLD, 20));
        scoreboard.getChildren().add(scoreLbl);

        GridPane scorePane = new GridPane();

        Label p1 = new Label(StoneColor.BLACK + ":");
        p1.setFont(Font.font(null, FontWeight.BOLD, 13));
        scorePane.add(p1, 0, 0);

        Label scoreCountBlackLbl = new Label(game.getScore(StoneColor.BLACK) + "");
        scoreCountBlackLbl.setPadding(new Insets(0, 0, 0, 15));
        scoreCountBlackLbl.setFont(Font.font(null, FontWeight.NORMAL, 13));
        scorePane.add(scoreCountBlackLbl, 1, 0);


        Label p2 = new Label(StoneColor.WHITE + ":");
        p2.setFont(Font.font(null, FontWeight.BOLD, 13));
        scorePane.add(p2, 0, 1);

        Label scoreCountWhiteLbl = new Label(game.getScore(StoneColor.WHITE) + "");
        scoreCountWhiteLbl.setPadding(new Insets(0, 0, 0, 15));
        scoreCountWhiteLbl.setFont(Font.font(null, FontWeight.NORMAL, 13));
        scorePane.add(scoreCountWhiteLbl, 1, 1);

        scoreboard.getChildren().add(scorePane);
        infoPane.getChildren().add(scoreboard);

        Pane spring2 = new Pane();
        spring2.setMinSize(10, 10);
        infoPane.getChildren().add(spring2);


        VBox explanationBoard = new VBox();
        Label explanationLabel = new Label();
        explanationLabel.setFont(Font.font(null, FontWeight.BOLD, 13));
        explanationLabel.setText("Turn Explanation:");
        explanationBoard.getChildren().add(explanationLabel);

        ScrollPane textScrollPane = new ScrollPane();
        textScrollPane.setFitToWidth(true);
        textScrollPane.prefHeightProperty().bind(infoPane.heightProperty());
        textScrollPane.setPrefWidth(infoPane.getWidth());

        TextArea textArea = new TextArea();
        textArea.setPadding(new Insets(3));
        textArea.setFocusTraversable(false);
        textArea.setWrapText(true);
        textArea.prefHeightProperty().bind(explanationBoard.heightProperty());
        textArea.setPrefWidth(explanationBoard.getWidth());

        explanationBoard.getChildren().add(textArea);

        Button saveCommentButton = new Button("Save Explanation");
        saveCommentButton.setFocusTraversable(false);
        saveCommentButton.prefWidthProperty().bind(explanationBoard.widthProperty());
        saveCommentButton.setAlignment(Pos.CENTER);
        saveCommentButton.setOnAction(e -> {
            game.commentCurrentMove(textArea.getText());
        });

        explanationBoard.getChildren().add(saveCommentButton);

        infoPane.getChildren().add(explanationBoard);

        /*
         * This listener updates the currently displayed game info
         */
        game.addListener(e -> {

            switch (e.getGameCommand()) {
                case STONE_WAS_SET, STONE_WAS_REMOVED, UPDATE, NEW_GAME, GAME_WON -> {
                    System.out.println("Received " + e.getGameCommand());
                    scoreCountBlackLbl.setText(game.getScore(StoneColor.BLACK) + "");
                    scoreCountWhiteLbl.setText(game.getScore(StoneColor.WHITE) + "");
                    actualPlayer.setText(game.getCurColor().toString());
                    textArea.setText(game.getComment());
                }
            }
        });

        return infoPane;
    }

    /**
     * newGame creates a panel that is a dialog for creating a new game<br>
     * At least it contains: <br>
     * -> Board size options <br>
     * -> Handicap spinner <br>
     * -> Komi <br>
     *
     * @return a GridPane which contains items for creating a new game.
     */
    private GridPane newGame() {

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(5));

        Label headline = new Label();
        headline.setText("Game Setup");
        headline.setFont(Font.font(null, FontWeight.BOLD, 15));

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

        Spinner<Integer> customSize = new Spinner<>(Game.MIN_CUSTOM_BOARD_SIZE, Game.MAX_CUSTOM_BOARD_SIZE, Game.MIN_CUSTOM_BOARD_SIZE, 2);
        customSize.setDisable(true);
        customSize.setMaxSize(55, 15);
        SpinnerValueFactory.IntegerSpinnerValueFactory customSizeIntFactory =
                (SpinnerValueFactory.IntegerSpinnerValueFactory) customSize.getValueFactory();
        gridPane.add(customSize, 1, 5);

        //change Event
        boardSize.selectedToggleProperty().addListener((observableValue, toggle, t1) -> {
            customSize.setDisable(t1.hashCode() != custom.hashCode());
            System.out.println(((RadioButton) t1).getText());
        });

        //Handicap
        Label handicapLbl = new Label();
        handicapLbl.setFont(Font.font(null, FontWeight.NORMAL, 13));
        handicapLbl.setText("Handicap");
        handicapLbl.setPadding(new Insets(5));
        gridPane.add(handicapLbl, 0, 6);

        Spinner<Integer> handicapCnt = new Spinner<>(Game.MIN_HANDICAP_AMOUNT, Game.MAX_HANDICAP_AMOUNT, Game.MIN_HANDICAP_AMOUNT, 1);
        handicapCnt.setMaxSize(55, 15);
        SpinnerValueFactory.IntegerSpinnerValueFactory handicapIntFactory =
                (SpinnerValueFactory.IntegerSpinnerValueFactory) handicapCnt.getValueFactory();
        gridPane.add(handicapCnt, 1, 6);

        //Komi
        Label komiLbl = new Label();
        komiLbl.setFont(Font.font(null, FontWeight.NORMAL, 13));
        komiLbl.setText("Komi");
        komiLbl.setPadding(new Insets(5));
        gridPane.add(komiLbl, 0, 7);

        Label komiCntLbl = new Label();
        komiCntLbl.setFont(Font.font(null, FontWeight.NORMAL, 13));
        komiCntLbl.setText("" + game.getKomi());
        komiCntLbl.setPadding(new Insets(5));
        komiCntLbl.setDisable(true);
        gridPane.add(komiCntLbl, 1, 7);

        //start Button
        Button startGameBtn = new Button();
        startGameBtn.setText("Start game");
        startGameBtn.setAlignment(Pos.BOTTOM_RIGHT);
        startGameBtn.setOnAction(event -> {
            //this.getChildren().remove(gridPane);
            RadioButton selected = (RadioButton) boardSize.getSelectedToggle();
            int handicap = handicapIntFactory.getValue();
            int actualBoardSize;

            if (selected.getId().equals("custom")) actualBoardSize = customSizeIntFactory.getValue();
            else actualBoardSize = Integer.parseInt(selected.getId());

            game.newGame(StoneColor.BLACK, actualBoardSize, handicap, new JapaneseRuleset());
        });
        //colum, row,
        gridPane.add(startGameBtn, 1, 15);

        return gridPane;
    }
}
