package pr_se.gogame.view_controller;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import pr_se.gogame.model.Game;
import pr_se.gogame.model.GameCommand;
import pr_se.gogame.model.StoneColor;

/**
 * This class contains the controller and view function of the game information panel.<br>
 * It is recommended to place the panel on the left or right side of the application.
 */
public class SidePane extends StackPane {

    private final int maxCustomBoardSize = 26;
    private final int minCustomBoardSize = 0;
    private final int maxKomiAmount = 9;
    private final int minKomiAmount = 0;
    private final Game game;
    private final Color backColor;

    /**
     * Constructor to create a SidePane
     *
     * @param game instance of actual game -> needed for triggering and observing changes in model
     */
    public SidePane(Color backColor, Game game) {
        this.backColor = backColor;
        this.game = game;

        this.setBackground(new Background(new BackgroundFill(this.backColor, new CornerRadii(5), new Insets(5, 2.5, 5, 5))));
        this.setMinWidth(250);
        setPadding(new Insets(5, 5, 5, 5)); //top, right, bottom, left


        GridPane gameSetting = newGame();
        VBox gameInfo = gameInfo();
        this.getChildren().add(gameSetting);

        game.addListener(l -> {
            if (l.getGameCommand() != GameCommand.INIT) return;
            if (!this.getChildren().contains(gameSetting)) {
                this.getChildren().add(gameSetting);
                this.getChildren().remove(gameInfo);
            }
        });

        game.addListener(l -> {
            if (!(l.getGameCommand() == GameCommand.BLACKSTARTS || l.getGameCommand() == GameCommand.WHITSTARTS))
                return;

            this.getChildren().remove(gameSetting);
            this.getChildren().add(gameInfo);

        });
    }

    /**
     * GameInfomations contains a mechanism to show relevant information based on current GameCommand <br>
     * contains at least: <br>
     * -> Current Player <br>
     * -> Turn Explanation <br>
     *
     * @return a VBox which contains items to show relevant game info.
     */
    private VBox gameInfo() {
        VBox infoPane = new VBox();
        infoPane.setPadding(new Insets(5));

        HBox playerInfo = new HBox();

        Label currentPlayer = new Label();
        currentPlayer.setFont(Font.font(null, FontWeight.BOLD, 13));
        currentPlayer.setText("Current Player: ");
        playerInfo.getChildren().add(currentPlayer);

        Label actualPlayer = new Label();
        actualPlayer.setFont(Font.font(null, FontWeight.NORMAL, 13));
        actualPlayer.setText(game.getBoard().getCurColor() == StoneColor.BLACK ? "Black" : "White");
        playerInfo.getChildren().add(actualPlayer);

        infoPane.getChildren().add(playerInfo);

        game.addListener(l -> {
            if (!(l.getGameCommand() == GameCommand.BLACKSTARTS || l.getGameCommand() == GameCommand.WHITSTARTS))
                return;
            game.getBoard().addListener(new GoListener() {
                @Override
                public void stoneSet(StoneSetEvent e) {
                    actualPlayer.setText((e.getColor() == StoneColor.WHITE) ? "Black" : "White");
                }

                @Override
                public void stoneRemoved(StoneRemovedEvent e) {

                }

                @Override
                public void debugInfoRequested(int x, int y, int StoneGroupPtrNO, int StoneGroupSerialNo) {

                }
            });
        });

        Pane spring1 = new Pane();
        spring1.minHeightProperty().bind(currentPlayer.heightProperty());
        infoPane.getChildren().add(spring1);

        //ToDo proper integration postponed to Sprint 2
        Label explanationLabel = new Label();
        explanationLabel.setFont(Font.font(null, FontWeight.BOLD, 13));
        explanationLabel.setText("Turn Explanation:");
        infoPane.getChildren().add(explanationLabel);

        ScrollPane textArea = new ScrollPane();
        textArea.setFitToWidth(true);
        textArea.prefHeightProperty().bind(infoPane.heightProperty());
        textArea.setPrefWidth(infoPane.getWidth());

        TextFlow textFlow = new TextFlow();
        textFlow.setPadding(new Insets(3));
        textFlow.setTextAlignment(TextAlignment.JUSTIFY);

        Text explanation = new Text();
        explanation.setFocusTraversable(false);
        explanation.setText("Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat. Ut wisi enim ad minim veniam, quis nostrud exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo consequat. Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero et accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi. Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat. Ut wisi enim ad minim veniam, quis nostrud exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo consequat. Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero et accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi. Nam liber tempor cum soluta nobis eleifend option congue nihil imperdiet doming id quod mazim placerat facer possim assum.");//
        textFlow.getChildren().add(explanation);

        textArea.setContent(textFlow);
        infoPane.getChildren().add(textArea);

        return infoPane;
    }

    /**
     * newGame creates a panel that is a dialog for creating a new game<br>
     * At least it contains: <br>
     * -> Board size options <br>
     * -> Komi spinner <br>
     *
     * @return a GridPane which contains items for creating a new game.
     */
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

        Spinner<Integer> customSize = new Spinner<>(minCustomBoardSize, maxCustomBoardSize, minCustomBoardSize, 1);
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

        Spinner<Integer> komiCnt = new Spinner<>(minKomiAmount, maxKomiAmount, minKomiAmount, 1);
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
