package pr_se.gogame.view_controller;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import pr_se.gogame.model.Game;

import javafx.application.Application;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import pr_se.gogame.model.GameCommand;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class contains the controller and view function of the game header panel.<br>
 * It is recommended to place the panel on the top of the application.
 */
public class HeaderPane extends VBox {

    /**
     * Background color of pane
     */
    private final Color backColor;

    /**
     * Instance of application
     */
    private final Application app;

    /**
     * Instance of stage
     */
    private final Stage stage;

    /**
     * Instance of game
     */
    private final Game game;

    /**
     * List of FileChooser Extensions
     */
    private final HashSet<FileChooser.ExtensionFilter> filterList;

    private final List<Button> playbackControlList = new ArrayList<>();

    List<Button> gameShortCardList = new ArrayList<>();

    List<MenuItem> gameSectionItems = new ArrayList<>();

    /**
     * Constructor to create a Header Pane
     *
     * @param backcolor Background Color
     * @param app       instance of actual application -> needed to open URL in Browser
     * @param stage     instance of actual stage -> needed to show file dialog
     * @param game      instance of actual game -> needed for triggering and observing changes in model
     */
    public HeaderPane(Color backcolor, Application app, Stage stage, Game game) {
        this.backColor = backcolor;
        this.app = app;
        this.stage = stage;
        this.game = game;

        this.filterList = Stream.of(new FileChooser.ExtensionFilter("Go Game", "*.sgf"))
                .collect(Collectors.toCollection(HashSet::new));

        this.setSpacing(5);

        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().add(fileSection());
        menuBar.getMenus().add(gameSection());
        menuBar.getMenus().add(viewSection());
        menuBar.getMenus().add(helpSection());

        this.getChildren().add(menuBar);
        this.getChildren().add(shortMenu());

        stage.setOnCloseRequest(e -> CustomCloseAction.onCloseAction(stage, game, e, filterList));
    }

    /**
     * The short menu contains at least: <br>
     * -> playback control buttons <br>
     * -> pass button <br>
     * -> resign button <br>
     * -> score game button <br>
     * -> confirm move button
     *
     * @return a horizontal box layout object which includes all needed elements of the short menu
     */
    private HBox shortMenu() {
        HBox lane = new HBox();
        lane.setPrefHeight(35);

        lane.setBackground(new Background(new BackgroundFill(this.backColor, new CornerRadii(5), new Insets(0, 5, 0, 5))));

        HBox playbackControl = new HBox();
        playbackControl.setPrefWidth(250);
        playbackControl.setAlignment(Pos.CENTER);
        playbackControl.setSpacing(5);

        Button fastBackward = new Button();
        fastBackward.setText("<<");
        fastBackward.setFocusTraversable(false);
        playbackControlList.add(fastBackward);

        Button backward = new Button();
        backward.setText("<");
        backward.setFocusTraversable(false);
        playbackControlList.add(backward);

        Button forward = new Button();
        forward.setText(">");
        forward.setFocusTraversable(false);
        playbackControlList.add(forward);

        Button fastForward = new Button();
        fastForward.setText(">>");
        fastForward.setFocusTraversable(false);
        playbackControlList.add(fastForward);

        playbackControl.getChildren().addAll(playbackControlList);
        playbackControlList.forEach(e -> e.setDisable(true));

        game.addListener(l -> {
            if (l.getGameCommand() != GameCommand.PLAYBACK) return;
            playbackControlList.forEach(e -> e.setDisable(false));

        });

        lane.getChildren().add(playbackControl);

        HBox gameShortCards = new HBox();
        gameShortCards.prefWidthProperty().bind(this.widthProperty().subtract(250));
        gameShortCards.setAlignment(Pos.CENTER);
        gameShortCards.setSpacing(25);


        Button pass = new Button("Pass");
        pass.setFocusTraversable(false);
        pass.setOnAction(e -> game.pass());
        gameShortCardList.add(pass);

        Button resign = new Button("Resign");
        resign.setFocusTraversable(false);
        resign.setOnAction(e -> game.resign());
        gameShortCardList.add(resign);

        Button scoreGame = new Button("Score Game");
        scoreGame.setFocusTraversable(false);
        scoreGame.setOnAction(e -> game.scoreGame());
        gameShortCardList.add(scoreGame);

        Button confirm = new Button("Confirm");
        confirm.setFocusTraversable(false);
        confirm.setVisible(false);
        confirm.setOnAction(e -> game.confirmChoice());
        gameShortCardList.add(confirm);

        gameShortCards.getChildren().addAll(gameShortCardList);
        gameShortCardList.forEach(e -> e.setDisable(true));

        game.addListener(l -> {
            if (l.getGameCommand() == GameCommand.INIT) {
                gameShortCardList.forEach(e -> e.setDisable(true));
            } else {
                gameShortCardList.forEach(e -> e.setDisable(false));
            }

        });

        lane.getChildren().add(gameShortCards);

        return lane;

    }

    /**
     * Creates the file section for the menu bar <br>
     * contains at least: <br>
     * -> New Game <br>
     * -> Import Game <br>
     * -> Export Game <br>
     * -> Exit Game
     *
     * @return the file section for the menu bar
     */
    private Menu fileSection() {
        Menu files = new Menu();
        files.setText("File");

        MenuItem newGameItem = new MenuItem();
        newGameItem.setText("New Game");
        files.getItems().add(newGameItem);
        newGameItem.setOnAction(e -> game.initGame());

        MenuItem importFileItem = new MenuItem();
        importFileItem.setText("Import Game");
        files.getItems().add(importFileItem);
        importFileItem.setOnAction(e -> {
            File f = CustomFileDialog.getFile(stage, false, filterList);//fileDialog(false, filterList);
            if (f != null) game.importGame(f.toPath());
            else System.out.println("Import Dialog cancelled");
        });

        MenuItem exportFileItem = new MenuItem();
        exportFileItem.setText("Export Game");
        files.getItems().add(exportFileItem);
        exportFileItem.setOnAction(e -> {
            File f = CustomFileDialog.getFile(stage, true, filterList);//fileDialog(true, filterList);
            if (f != null) game.exportGame(f.toPath());
            else System.out.println("Export Dialog cancelled");
        });

        MenuItem exitGameItem = new MenuItem();
        exitGameItem.setText("Exit Game");
        files.getItems().add(exitGameItem);
        exitGameItem.setOnAction(e -> CustomCloseAction.onCloseAction(stage, game, null, filterList)); //onCloseAction(null)


        SeparatorMenuItem sep1 = new SeparatorMenuItem();
        SeparatorMenuItem sep2 = new SeparatorMenuItem();

        files.getItems().add(1, sep1);
        files.getItems().add(4, sep2);

        return files;
    }

    /**
     * Creates the game section for the menu bar <br>
     * contains at least: <br>
     * -> Pass <br>
     * -> Resign <br>
     * -> Score Game
     *
     * @return the game section for the menu bar
     */
    private Menu gameSection() {
        Menu menu = new Menu();
        menu.setText("Game");

        CheckMenuItem moveConfirmationRequired = new CheckMenuItem("Move confirmation required");
        gameSectionItems.add(moveConfirmationRequired);
        moveConfirmationRequired.setSelected(false);
        moveConfirmationRequired.setOnAction(e -> {
            var k = this.gameShortCardList.stream().filter(i -> i.getText().equals("Confirm")).findFirst();
            if (moveConfirmationRequired.isSelected()) {
                k.ifPresent(button -> button.setVisible(true));
                game.setConfirmationNeeded(true);

            } else {
                k.ifPresent(button -> button.setVisible(false));
                game.setConfirmationNeeded(false);
            }
        });

        MenuItem passItem = new MenuItem();
        passItem.setText("Pass");
        //menu.getItems().add(passItem);
        gameSectionItems.add(passItem);
        passItem.setOnAction(e -> game.pass());

        MenuItem resignItem = new MenuItem();
        resignItem.setText("Resign");
        //menu.getItems().add(resignItem);
        gameSectionItems.add(resignItem);
        resignItem.setOnAction(e -> game.resign());

        MenuItem scoreGameItem = new MenuItem();
        scoreGameItem.setText("Score Game");
        //menu.getItems().add(scoreGameItem);
        gameSectionItems.add(scoreGameItem);
        scoreGameItem.setOnAction(e -> game.scoreGame());

        menu.getItems().addAll(gameSectionItems);
        gameSectionItems.forEach(e -> e.setDisable(true));

        game.addListener(l -> {
            if (l.getGameCommand() == GameCommand.INIT) {
                gameSectionItems.forEach(e -> e.setDisable(true));
            } else {
                gameSectionItems.forEach(e -> e.setDisable(false));
            }
        });

        SeparatorMenuItem sep1 = new SeparatorMenuItem();
        menu.getItems().add(1, sep1);

        SeparatorMenuItem sep2 = new SeparatorMenuItem();
        menu.getItems().add(4, sep2);

        return menu;
    }

    /**
     * Creates the view section for the menu bar <br>
     * contains at least: <br>
     * ->
     *
     * @return the view section for the menu bar
     */
    private Menu viewSection() {
        Menu menu = new Menu();
        menu.setText("View");

        List<MenuItem> viewSectionItems = new ArrayList<>();

        CheckMenuItem showMoveNumbersCBtn = new CheckMenuItem("Show Move Numbers");
        viewSectionItems.add(showMoveNumbersCBtn);
        showMoveNumbersCBtn.setSelected(false);
        showMoveNumbersCBtn.setOnAction(e -> {
            game.setShowMoveNumbers(showMoveNumbersCBtn.isSelected());
        });

        CheckMenuItem showCoordinatesCBtn = new CheckMenuItem("Show Coordinates");
        viewSectionItems.add(showCoordinatesCBtn);
        showCoordinatesCBtn.setSelected(true);
        showCoordinatesCBtn.setOnAction(e -> {
            game.setShowCoordinates(showCoordinatesCBtn.isSelected());
        });
        menu.getItems().addAll(viewSectionItems);

        viewSectionItems.forEach(e -> e.setDisable(true));

        game.addListener(l -> {
            if (l.getGameCommand() == GameCommand.INIT) {
                viewSectionItems.forEach(e -> e.setDisable(true));
            } else {
                viewSectionItems.forEach(e -> e.setDisable(false));
            }
        });


        return menu;
    }

    /**
     * Creates the help section for the menu bar <br>
     * contains at least: <br>
     * -> Help -> Link to WebSite <br>
     * -> About us -> Information ab the developer
     *
     * @return the help section for the menu bar
     */
    private Menu helpSection() {
        Menu menu = new Menu();
        menu.setText("Help");

        MenuItem helpItem = new MenuItem();
        helpItem.setText("Help");
        menu.getItems().add(helpItem);
        helpItem.setOnAction(ev -> {
            String url = "https://en.wikipedia.org/wiki/Go_(game)";
            app.getHostServices().showDocument(url);
        });

        MenuItem aboutUs = new MenuItem();
        aboutUs.setText("About Us");
        menu.getItems().add(aboutUs);
        aboutUs.setOnAction(e -> {

            Alert box = new Alert(Alert.AlertType.INFORMATION);
            box.setTitle("About us");
            box.setHeaderText(null);
            box.setContentText("This Go Game was developed by Gerald, Lukas and Sebastian.");
            box.initStyle(StageStyle.UTILITY);
            box.initOwner(stage);
            box.show();
        });

        return menu;
    }

}
