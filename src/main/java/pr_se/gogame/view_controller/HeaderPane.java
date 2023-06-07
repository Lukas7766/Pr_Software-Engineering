package pr_se.gogame.view_controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import pr_se.gogame.model.Game;

import javafx.application.Application;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import pr_se.gogame.model.GameCommand;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Path;
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
        files.setText("_File");

        MenuItem newGameItem = new MenuItem();
        newGameItem.setText("_New Game");
        newGameItem.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
        files.getItems().add(newGameItem);
        newGameItem.setOnAction(e -> {

            if (game.getGameState() == GameCommand.INIT) return;
            CustomNewGameAction.onSaveAction(stage, game, filterList);
        });

        MenuItem importFileItem = new MenuItem();
        importFileItem.setText("_Load Game");
        importFileItem.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
        files.getItems().add(importFileItem);
        importFileItem.setOnAction(e -> {
            File f = CustomFileDialog.getFile(stage, false, filterList);//fileDialog(false, filterList);
            if (f != null) game.loadGame(f.toPath());
            else System.out.println("Import Dialog cancelled");
        });

        MenuItem exportFileItem = new MenuItem();
        exportFileItem.setText("_Save");
        exportFileItem.setDisable(true);
        exportFileItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
        files.getItems().add(exportFileItem);
        exportFileItem.setOnAction(e -> saveGame(false));

        MenuItem exportFileItemAs = new MenuItem();
        exportFileItemAs.setText("_Save as");
        exportFileItemAs.setDisable(true);
        exportFileItemAs.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.SHIFT_DOWN, KeyCombination.CONTROL_DOWN));
        files.getItems().add(exportFileItemAs);
        exportFileItemAs.setOnAction(e -> saveGame(true));


        game.addListener(l -> {
            switch (l.getGameCommand()) {
                case BLACK_PLAYS, WHITE_PLAYS, BLACK_STARTS, WHITE_STARTS -> {
                    exportFileItem.setDisable(false);
                    exportFileItemAs.setDisable(false);
                }
                default -> {
                    exportFileItem.setDisable(true);
                    exportFileItemAs.setDisable(true);
                }
            }
        });

        MenuItem exitGameItem = new MenuItem();
        exitGameItem.setText("_Quit Game");
        exitGameItem.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN));
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
        menu.setText("_Game");

        CheckMenuItem moveConfirmationRequired = new CheckMenuItem("_Move confirmation required");
        moveConfirmationRequired.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.ALT_DOWN));
        gameSectionItems.add(moveConfirmationRequired);
        moveConfirmationRequired.setSelected(game.isConfirmationNeeded());
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
        passItem.setText("_Pass");
        passItem.setAccelerator(new KeyCodeCombination(KeyCode.P, KeyCombination.ALT_DOWN));
        gameSectionItems.add(passItem);
        passItem.setOnAction(e -> game.pass());

        MenuItem resignItem = new MenuItem();
        resignItem.setText("_Resign");
        resignItem.setAccelerator(new KeyCodeCombination(KeyCode.R, KeyCombination.ALT_DOWN));
        gameSectionItems.add(resignItem);
        resignItem.setOnAction(e -> game.resign());

        MenuItem scoreGameItem = new MenuItem();
        scoreGameItem.setText("_Score Game");
        scoreGameItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.ALT_DOWN));
        gameSectionItems.add(scoreGameItem);
        scoreGameItem.setOnAction(e -> game.scoreGame());

        menu.getItems().addAll(gameSectionItems);
        gameSectionItems.forEach(e -> e.setDisable(true));

        game.addListener(l -> {
            switch (l.getGameCommand()) {
                case INIT, WHITE_WON, BLACK_WON ->
                        gameSectionItems.stream().filter(e -> !e.isDisable()).forEach(e -> e.setDisable(true));

                case BLACK_PLAYS, WHITE_PLAYS, WHITE_STARTS, BLACK_STARTS ->
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
     * ->
     *
     * @return the view section for the menu bar
     */
    private Menu viewSection() {
        Menu menu = new Menu();
        menu.setText("_View");

        List<MenuItem> viewSectionItems = new ArrayList<>();

        CheckMenuItem showMoveNumbersCBtn = new CheckMenuItem("_Move Numbers");
        showMoveNumbersCBtn.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN, KeyCombination.ALT_DOWN));
        viewSectionItems.add(showMoveNumbersCBtn);
        showMoveNumbersCBtn.setSelected(game.isShowMoveNumbers());
        showMoveNumbersCBtn.setOnAction(e -> game.setShowMoveNumbers(showMoveNumbersCBtn.isSelected()));

        CheckMenuItem showCoordinatesCBtn = new CheckMenuItem("_Coordinates");
        showCoordinatesCBtn.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN, KeyCombination.ALT_DOWN));
        viewSectionItems.add(showCoordinatesCBtn);
        showCoordinatesCBtn.setSelected(game.isShowCoordinates());
        showCoordinatesCBtn.setOnAction(e -> game.setShowCoordinates(showCoordinatesCBtn.isSelected()));
        menu.getItems().addAll(viewSectionItems);

        MenuItem loadGraphicsBtn = new MenuItem("_Load Graphics Set");
        loadGraphicsBtn.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN, KeyCombination.ALT_DOWN));
        menu.getItems().addAll(loadGraphicsBtn);

        loadGraphicsBtn.setOnAction(e -> {
            File workingDirectory = new File(System.getProperty("user.dir") + "/Grafiksets/");
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(workingDirectory);
            fileChooser.setTitle("Load Graphics Set");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("ZIP", "*.zip")
            );
            File selectedFile = fileChooser.showOpenDialog(stage);
            if (selectedFile != null) {
                game.setGraphicsPath(selectedFile.getAbsolutePath());
            }
        });

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
        menu.setText("_Help");

        MenuItem helpItem = new MenuItem();
        helpItem.setText("_Help");
        helpItem.setAccelerator(new KeyCodeCombination(KeyCode.H, KeyCombination.CONTROL_DOWN, KeyCombination.ALT_DOWN));
        menu.getItems().add(helpItem);
        helpItem.setOnAction(ev -> {
            String url = "https://en.wikipedia.org/wiki/Go_(game)";
            app.getHostServices().showDocument(url);
        });

        MenuItem aboutUs = new MenuItem();
        aboutUs.setText("_About Us");
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
            if (l.getGameCommand() != GameCommand.CONFIG_DEMO_MODE) return;
            playbackControlList.forEach(e -> e.setDisable(!game.isDemoMode()));

        });

        lane.getChildren().add(playbackControl);

        HBox gameShortCards = new HBox();
        gameShortCards.setPrefWidth(4 * 100);
        // gameShortCards.prefWidthProperty().bind(this.widthProperty().subtract(playbackControl.getPrefWidth()));
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
        confirm.setVisible(game.isConfirmationNeeded());
        confirm.setOnAction(e -> game.confirmChoice());
        gameShortCardList.add(confirm);

        gameShortCards.getChildren().addAll(gameShortCardList);
        gameShortCardList.forEach(e -> e.setDisable(true));

        game.addListener(l -> {
            if (l.getGameCommand() == GameCommand.INIT || l.getGameCommand() == GameCommand.WHITE_WON || l.getGameCommand() == GameCommand.BLACK_WON) {
                gameShortCardList.forEach(e -> e.setDisable(true));
            } else {
                gameShortCardList.forEach(e -> e.setDisable(false));
            }

        });

        lane.getChildren().add(gameShortCards);

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
                System.out.println(f.getName());
            }
        }
        System.out.println();

        ComboBox graphicsPackSelectorComboBox = new ComboBox(comboBoxItems);
        graphicsPackSelectorComboBox.setValue("default.zip");
        graphicsPackSelectorComboBox.setTooltip(new Tooltip("Select the graphics pack zip file."));
        graphicsPackSelectorComboBox.setMaxWidth(100);
        graphicsPackSelectorComboBox.setOnAction((e) -> {
            System.out.println(graphicsPackSelectorComboBox.getValue());
            game.setGraphicsPath(GRAPHICS_FOLDER + "/" + graphicsPackSelectorComboBox.getValue());
        });

        lane.getChildren().add(graphicsPackSelectorComboBox);

        lane.setAlignment(Pos.CENTER_LEFT);

        return lane;

    }

    private void saveGame(boolean as) {
        Path saveGamePath = game.getSavePath();

        if (as || saveGamePath == null) {
            File f = CustomFileDialog.getFile(stage, true, filterList);
            if (f == null) return;
            game.setSavePath(f.toPath());
        }
        if (!game.saveGame()) System.out.println("Export did not work!");
    }

}
