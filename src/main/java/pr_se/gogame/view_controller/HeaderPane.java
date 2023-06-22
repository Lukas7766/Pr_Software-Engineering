package pr_se.gogame.view_controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import pr_se.gogame.model.file.FileHandler;
import pr_se.gogame.model.Game;

import javafx.application.Application;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import pr_se.gogame.model.GameCommand;

import java.io.File;
import java.io.FileFilter;
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
     * Scene of application
     */
    private final Scene scene;
    private final HashSet<FileChooser.ExtensionFilter> filterList;

    private final List<Button> playbackControlList = new ArrayList<>();

    private final List<Button> gameShortCardList = new ArrayList<>();

    private final List<MenuItem> gameSectionItems = new ArrayList<>();

    /**
     * Constructor to create a Header Pane
     *
     * @param backcolor Background Color
     * @param app       instance of actual application -> needed to open URL in Browser
     * @param stage     instance of actual stage -> needed to show file dialog
     * @param game      instance of actual game -> needed for triggering and observing changes in model
     */
    public HeaderPane(Color backcolor, Application app, Scene scene, Stage stage, Game game) {
        this.backColor = backcolor;
        this.app = app;
        this.stage = stage;
        this.game = game;
        this.scene = scene;

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
        Menu files = new Menu("_File");

        MenuItem newGameItem = new MenuItem("_New Game");
        newGameItem.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
        files.getItems().add(newGameItem);
        newGameItem.setOnAction(e -> {

            if (game.getGameState() == GameCommand.INIT) return;
            CustomNewGameAction.onSaveAction(stage, game, filterList);
        });

        MenuItem importFileItem = new MenuItem("L_oad Game");
        importFileItem.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
        files.getItems().add(importFileItem);
        importFileItem.setOnAction(e -> {
            File f = CustomFileDialog.getFile(stage, false, filterList);//fileDialog(false, filterList);
            if (f == null) {
                System.out.println("Import Dialog cancelled");
            } else if(!game.loadGame(f)) {
                System.out.println("Failed to load game!");
            }
        });

        MenuItem exportFileItem = new MenuItem("_Save");
        exportFileItem.setDisable(true);
        exportFileItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
        files.getItems().add(exportFileItem);
        exportFileItem.setOnAction(e -> saveGame(false));

        MenuItem exportFileItemAs = new MenuItem("Save _as");
        exportFileItemAs.setDisable(true);
        exportFileItemAs.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.SHIFT_DOWN, KeyCombination.CONTROL_DOWN));
        files.getItems().add(exportFileItemAs);
        exportFileItemAs.setOnAction(e -> saveGame(true));


        game.addListener(e -> {
            switch (e.getGameCommand()) {
                case STONE_WAS_SET -> {
                    if(e.getColor() != null) {
                        exportFileItem.setDisable(false);
                        exportFileItemAs.setDisable(false);
                    }
                }
                case INIT -> {
                    exportFileItem.setDisable(true);
                    exportFileItemAs.setDisable(true);
                }
            }
        });

        MenuItem exitGameItem = new MenuItem("_Quit Game");
        exitGameItem.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN));
        files.getItems().add(exitGameItem);
        exitGameItem.setOnAction(e -> CustomCloseAction.onCloseAction(stage, game, null, filterList)); //onCloseAction(null)


        SeparatorMenuItem sep1 = new SeparatorMenuItem();
        SeparatorMenuItem sep2 = new SeparatorMenuItem();

        files.getItems().add(1, sep1);
        files.getItems().add(5, sep2);

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
        Menu menu = new Menu("_Game");

        CheckMenuItem moveConfirmationRequired = new CheckMenuItem("Move _confirmation required");
        moveConfirmationRequired.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.ALT_DOWN));
        gameSectionItems.add(moveConfirmationRequired);
        moveConfirmationRequired.setSelected(GlobalSettings.isConfirmationNeeded());
        moveConfirmationRequired.setOnAction(e -> {
            var k = this.gameShortCardList.stream().filter(i -> i.getText().equals("Confirm")).findFirst();
            if (moveConfirmationRequired.isSelected()) {
                k.ifPresent(button -> button.setVisible(true));
                GlobalSettings.setConfirmationNeeded(true);

            } else {
                k.ifPresent(button -> button.setVisible(false));
                GlobalSettings.setConfirmationNeeded(false);
            }
        });

        MenuItem passItem = new MenuItem("_Pass");
        passItem.setAccelerator(new KeyCodeCombination(KeyCode.P, KeyCombination.ALT_DOWN));
        gameSectionItems.add(passItem);
        passItem.setOnAction(e -> game.pass());

        MenuItem resignItem = new MenuItem("_Resign");
        resignItem.setAccelerator(new KeyCodeCombination(KeyCode.R, KeyCombination.ALT_DOWN));
        gameSectionItems.add(resignItem);
        resignItem.setOnAction(e -> game.resign());

        MenuItem scoreGameItem = new MenuItem("_Score Game");
        scoreGameItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.ALT_DOWN));
        gameSectionItems.add(scoreGameItem);
        scoreGameItem.setOnAction(e -> game.scoreGame());

        menu.getItems().addAll(gameSectionItems);
        gameSectionItems.forEach(e -> e.setDisable(true));

        game.addListener(l -> {
            switch (l.getGameCommand()) {
                case INIT, GAME_WON ->
                    gameSectionItems.stream().filter(e -> !e.isDisable()).forEach(e -> e.setDisable(true));

                case NEW_GAME, COLOR_HAS_CHANGED ->
                    gameSectionItems.stream().filter(MenuItem::isDisable).forEach(e -> e.setDisable(false));
            }

        });

        SeparatorMenuItem sep1 = new SeparatorMenuItem();
        menu.getItems().

                add(1, sep1);

        SeparatorMenuItem sep2 = new SeparatorMenuItem();
        menu.getItems().

                add(4, sep2);

        return menu;
    }

    /**
     * Creates the view section for the menu bar <br>
     * contains at least: <br>
     * -> show Move Numbers <br>
     * -> show Coordinates <br>
     *
     * @return the view section for the menu bar
     */
    private Menu viewSection() {
        Menu menu = new Menu("_View");

        List<MenuItem> viewSectionItems = new ArrayList<>();

        CheckMenuItem showMoveNumbersCBtn = new CheckMenuItem("Move _Numbers");
        showMoveNumbersCBtn.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN, KeyCombination.ALT_DOWN));
        viewSectionItems.add(showMoveNumbersCBtn);
        showMoveNumbersCBtn.setSelected(GlobalSettings.isShowMoveNumbers());
        showMoveNumbersCBtn.setOnAction(e -> GlobalSettings.setShowMoveNumbers(showMoveNumbersCBtn.isSelected()));

        CheckMenuItem showCoordinatesCBtn = new CheckMenuItem("_Coordinates");
        showCoordinatesCBtn.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN, KeyCombination.ALT_DOWN));
        viewSectionItems.add(showCoordinatesCBtn);
        showCoordinatesCBtn.setSelected(GlobalSettings.isShowCoordinates());
        showCoordinatesCBtn.setOnAction(e -> GlobalSettings.setShowCoordinates(showCoordinatesCBtn.isSelected()));
        menu.getItems().addAll(viewSectionItems);

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
        Menu menu = new Menu("_Help");

        MenuItem helpItem = new MenuItem("_Help");
        helpItem.setAccelerator(new KeyCodeCombination(KeyCode.H, KeyCombination.CONTROL_DOWN, KeyCombination.ALT_DOWN));
        menu.getItems().add(helpItem);
        helpItem.setOnAction(ev -> {
            String url = "https://en.wikipedia.org/wiki/Go_(game)";
            app.getHostServices().showDocument(url);
        });

        MenuItem aboutUs = new MenuItem("_About Us");
        aboutUs.setAccelerator(new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN, KeyCombination.ALT_DOWN));
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

        Button fastBackward = new Button("<<");
        fastBackward.setFocusTraversable(false);
        fastBackward.setOnAction(e -> System.out.println("fastBackward"));
        playbackControlList.add(fastBackward);
        fastBackward.setOnAction(e -> game.rewind());

        Button backward = new Button("<");
        backward.setFocusTraversable(false);
        backward.setOnAction(e -> System.out.println("backward"));
        playbackControlList.add(backward);
        backward.setOnAction(e -> game.undo());

        Button forward = new Button(">");
        forward.setFocusTraversable(false);
        forward.setOnAction(e -> System.out.println("forward"));
        playbackControlList.add(forward);
        forward.setOnAction(e -> game.redo());

        Button fastForward = new Button(">>");
        fastForward.setOnAction(e -> System.out.println("fastForward"));
        fastForward.setFocusTraversable(false);
        playbackControlList.add(fastForward);
        fastForward.setOnAction(e -> game.goToEnd());

        playbackControl.getChildren().addAll(playbackControlList);
        /*playbackControlList.forEach(e -> e.setDisable(true));*/

        //Key Bindings for the playback control
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.F), () -> {
            if (backward.isDisabled()) return;
            System.out.println("backward");
        });

        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.H), () -> {
            if (forward.isDisabled()) return;
            System.out.println("forward");
        });
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.T), () -> {
            if (fastForward.isDisabled()) return;
            System.out.println("fastForward");
        });
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.G), () -> {
            if (fastBackward.isDisabled()) return;
            System.out.println("fastBackward");
        });


        lane.getChildren().add(playbackControl);

        HBox gameCards = new HBox();
        gameCards.prefWidthProperty().bind(this.widthProperty().subtract(250));
        gameCards.setAlignment(Pos.CENTER_LEFT);

        HBox gameShortCuts = new HBox();
        gameShortCuts.prefWidthProperty().bind(this.widthProperty().subtract(250));
        gameShortCuts.setAlignment(Pos.CENTER);
        gameShortCuts.setSpacing(25);


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
        confirm.setVisible(GlobalSettings.isConfirmationNeeded());
        confirm.setOnAction(e -> GlobalSettings.confirmMove());
        gameShortCardList.add(confirm);

        gameShortCuts.getChildren().addAll(gameShortCardList);
        gameShortCardList.forEach(e -> e.setDisable(true));

        game.addListener(l -> {
            switch (l.getGameCommand()){
                case INIT, GAME_WON -> gameShortCardList.forEach(e -> e.setDisable(true));
                case NEW_GAME, COLOR_HAS_CHANGED -> gameShortCardList.forEach(e -> e.setDisable(false));
            }
        });

        gameCards.getChildren().addAll(gameShortCuts);


        // Create combo box for selecting graphics packs
        ObservableList<String> comboBoxItems = FXCollections.observableArrayList();
        final String GRAPHICS_FOLDER = "./Grafiksets";
        File graphicsFolder = new File(GRAPHICS_FOLDER);
        FileFilter zipFilter = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().toLowerCase().endsWith(".zip");
            }
        };
        for(File f : graphicsFolder.listFiles(zipFilter)) {
            if(f.isFile()) { // We still need to check this because you could have a folder whose name ends with ".zip".
                comboBoxItems.add(f.getName());
            }
        }

        ComboBox graphicsPackSelectorComboBox = new ComboBox(comboBoxItems);
        graphicsPackSelectorComboBox.setValue("default.zip");
        graphicsPackSelectorComboBox.setTooltip(new Tooltip("Select the graphics pack zip file."));
        graphicsPackSelectorComboBox.setMinWidth(125);
        graphicsPackSelectorComboBox.setTranslateX(-15);
        graphicsPackSelectorComboBox.setFocusTraversable(false);
        graphicsPackSelectorComboBox.setOnAction((e) -> {
            System.out.println(graphicsPackSelectorComboBox.getValue());
            GlobalSettings.setGraphicsPath(GRAPHICS_FOLDER + "/" + graphicsPackSelectorComboBox.getValue());
        });

        gameShortCuts.getChildren().add(graphicsPackSelectorComboBox);

        gameCards.getChildren().add(graphicsPackSelectorComboBox);
        lane.getChildren().add(gameCards);

        return lane;

    }

    private void saveGame(boolean as) {
        File saveGameFile = FileHandler.getCurrentFile();

        if (as || saveGameFile == null) {
            saveGameFile = CustomFileDialog.getFile(stage, true, filterList);
            if (saveGameFile == null) {
                return;
            }
        }
        if (!game.saveGame(saveGameFile)) System.out.println("Export did not work!");
    }

}
