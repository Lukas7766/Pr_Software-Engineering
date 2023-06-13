package pr_se.gogame.view_controller;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import pr_se.gogame.model.Game;
import pr_se.gogame.model.StoneColor;

import java.io.File;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class contains the controller and view function of the game information panel.<br>
 * It is recommended to place the panel on the left or right side of the application.
 */
public class SidePane extends StackPane {

    /**
     * max size of custom board
     */
    private static final int MAX_CUSTOM_BOARD_SIZE = 25;

    /**
     * min size of custom board
     */
    private static final int MIN_CUSTOM_BOARD_SIZE = 5;

    /**
     * max handicap amount
     */
    private static final int MAX_HANDICAP_AMOUNT = 9;

    /**
     * min handicap amount
     */
    private static final int MIN_HANDICAP_AMOUNT = 0;

    /**
     * instance of actual game
     */
    private final Game game;
    /**
     * list of file extension filters for file chooser/saver
     */
    private final HashSet<FileChooser.ExtensionFilter> filterList;

    /**
     * stage of application
     */
    private final Stage stage;

    /**
     * Constructor to create a SidePane
     *
     * @param game instance of actual game -> needed for triggering and observing changes in model
     */
    public SidePane(Color backColor, Stage stage, Game game) {
        this.game = game;
        this.filterList = Stream.of(new FileChooser.ExtensionFilter("Go Game", "*.sgf"))
                .collect(Collectors.toCollection(HashSet::new));
        this.stage = stage;
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

                //case CONFIG_DEMO_MODE:
                //    if (game.isDemoMode()) {
                //        this.getChildren().remove(gameSetting);
                //        this.getChildren().add(gameInfo);
                //    }
                //    break;

                case GAME_WON:
                    CustomWinAction.winAction(stage, game);
                    break;
            }
        });
    }

    /**
     * GameInfomations contains a mechanism to show relevant information based on current GameCommand <br>
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

        ScrollPane textArea = new ScrollPane();
        textArea.setFitToWidth(true);
        textArea.prefHeightProperty().bind(infoPane.heightProperty());
        textArea.setPrefWidth(infoPane.getWidth());

        TextFlow textFlow = new TextFlow();
        textFlow.setPadding(new Insets(3));
        textFlow.setTextAlignment(TextAlignment.JUSTIFY);

        Text explanation = new Text();
        explanation.setFocusTraversable(false);
        //explanation.setText("Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat. Ut wisi enim ad minim veniam, quis nostrud exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo consequat. Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero et accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi. Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat. Ut wisi enim ad minim veniam, quis nostrud exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo consequat. Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero et accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi. Nam liber tempor cum soluta nobis eleifend option congue nihil imperdiet doming id quod mazim placerat facer possim assum.");//
        explanation.setText("");
        textFlow.getChildren().add(explanation);

        textArea.setContent(textFlow);
        explanationBoard.getChildren().add(textArea);

        /*
         * Adds listener to Game to update the currently displayed player name
         */
        game.addListener(l -> {

            switch (l.getGameCommand()) {
                case COLOR_HAS_CHANGED, NEW_GAME, GAME_WON -> {
                    scoreCountBlackLbl.setText(game.getScore(StoneColor.BLACK) + "");
                    scoreCountWhiteLbl.setText(game.getScore(StoneColor.WHITE) + "");
                    actualPlayer.setText(game.getCurColor().toString());

                }
                case CONFIG_DEMO_MODE -> {
                    System.out.println("Demo Mode: " + game.isDemoMode());

                    if (game.isDemoMode()) {
                        if (infoPane.getChildren().contains(explanationBoard)) break;
                        infoPane.getChildren().add(explanationBoard);
                    } else {
                        infoPane.getChildren().remove(explanationBoard);
                    }
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

        Spinner<Integer> customSize = new Spinner<>(MIN_CUSTOM_BOARD_SIZE, MAX_CUSTOM_BOARD_SIZE, MIN_CUSTOM_BOARD_SIZE, 2);
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

        Spinner<Integer> handicapCnt = new Spinner<>(MIN_HANDICAP_AMOUNT, MAX_HANDICAP_AMOUNT, MIN_HANDICAP_AMOUNT, 1);
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
        startGameBtn.setText("start Game");
        startGameBtn.setAlignment(Pos.BOTTOM_RIGHT);
        startGameBtn.setOnAction(event -> {
            //this.getChildren().remove(gridPane);
            RadioButton selected = (RadioButton) boardSize.getSelectedToggle();
            int handicap = handicapIntFactory.getValue();
            int actualBoardSize;

            if (selected.getId().equals("custom")) actualBoardSize = customSizeIntFactory.getValue();
            else actualBoardSize = Integer.parseInt(selected.getId());

            game.setDemoMode(false);
            game.newGame(StoneColor.BLACK, actualBoardSize, handicap);
        });
        //colum, row,
        gridPane.add(startGameBtn, 1, 15);

        Pane spacer = new Pane();
        spacer.setMinSize(10, 10);
        gridPane.add(spacer, 0, 16);

        Label demoLbl = new Label();
        demoLbl.setFont(Font.font(null, FontWeight.BOLD, 13));
        demoLbl.setText("Demo");
        gridPane.add(demoLbl, 0, 17);

        //start DemoGame
        Button startDemoGameBtn = new Button("start Demo");
        gridPane.add(startDemoGameBtn, 0, 18);

        startDemoGameBtn.setOnAction(event -> {

           File file = CustomFileDialog.getFile(stage, false,filterList);

            if (file != null) {
                game.loadGame(file.toPath());
                game.setDemoMode(true);
            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Information");
                alert.setHeaderText("No File selected!");
                alert.setContentText("Please select a file to continue!");
                alert.initOwner(stage);
                alert.showAndWait();
            }

        });

        return gridPane;
    }
}
