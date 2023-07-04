package pr_se.gogame.view_controller;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import pr_se.gogame.model.Game;
import pr_se.gogame.model.History;
import pr_se.gogame.model.file.LoadingGameException;
import pr_se.gogame.view_controller.dialog.*;
import pr_se.gogame.view_controller.observer.ViewListener;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static javafx.scene.input.KeyCode.A;
import static pr_se.gogame.model.History.HistoryNode.AbstractSaveToken.*;

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

    private final List<Button> playbackControlList = new LinkedList<>();

    private final List<Button> gameShortCutList = new LinkedList<>();

    private final List<MenuItem> gameSectionItems = new LinkedList<>();

    /**
     * Constructor to create a Header Pane
     *
     * @param backcolor Background Color
     * @param app       instance of actual application -> needed to open URL in Browser
     * @param stage     instance of actual stage -> needed to show file dialog
     * @param game      instance of actual game -> needed for triggering and observing changes in model
     */
    public HeaderPane(Color backcolor, Application app, Scene scene, Stage stage, Game game) throws IOException {
        this.backColor = backcolor;
        this.app = app;
        this.stage = stage;
        this.game = game;
        this.scene = scene;

        this.setSpacing(5);

        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().add(fileSection());
        menuBar.getMenus().add(historySection());
        menuBar.getMenus().add(gameSection());
        menuBar.getMenus().add(viewSection());
        menuBar.getMenus().add(helpSection());

        this.getChildren().add(menuBar);
        this.getChildren().add(shortMenu());
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
            if (game.getGameState() == Game.GameState.NOT_STARTED_YET) {
                return;
            }
            CustomNewGameAction.onNewGameAction(stage, game);
        });

        final String loadingErrorMsg = "Failed to load the game!";

        MenuItem importFileItem = new MenuItem("L_oad Game");
        importFileItem.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
        files.getItems().add(importFileItem);
        importFileItem.setOnAction(e -> {
            File f = CustomFileDialog.getFile(stage, false);
            if (f != null) {
                try {
                    if(!game.getFileHandler().loadFile(f)) {
                        CustomExceptionDialog.show(new IOException(), loadingErrorMsg);
                    } else {
                        game.getHistory().goToFirstMove();
                    }
                } catch (NoSuchFileException nsfException) {
                    CustomExceptionDialog.show(nsfException, loadingErrorMsg, nsfException.getMessage());
                } catch (LoadingGameException | IOException exception) {
                    CustomExceptionDialog.show(exception, loadingErrorMsg, exception.getMessage());
                    game.initGame();
                }
            }
        });

        MenuItem exportFileItem = new MenuItem("_Save");
        exportFileItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
        files.getItems().add(exportFileItem);
        exportFileItem.setOnAction(e -> CustomSaveAction.saveGame(game, stage, false));

        MenuItem exportFileItemAs = new MenuItem("Save _as");
        exportFileItemAs.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.SHIFT_DOWN, KeyCombination.CONTROL_DOWN));
        files.getItems().add(exportFileItemAs);
        exportFileItemAs.setOnAction(e -> CustomSaveAction.saveGame(game, stage, true));


        game.addListener(e -> {
            switch (e.getGameCommand()) {
                case STONE_WAS_SET -> {
                    exportFileItem.setDisable(false);
                    exportFileItemAs.setDisable(false);
                }
                case INIT -> {
                    exportFileItem.setDisable(true);
                    exportFileItemAs.setDisable(true);
                }
                default -> {
                    // This comment is here to fill the default case, otherwise SonarQube will complain (as it would in the absence of a default case).
                }
            }
        });

        MenuItem exitGameItem = new MenuItem("_Quit Game");
        exitGameItem.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN));
        files.getItems().add(exitGameItem);
        exitGameItem.setOnAction(e -> CustomCloseAction.onCloseAction(stage, game, null)); //onCloseAction(null)


        SeparatorMenuItem sep1 = new SeparatorMenuItem();
        SeparatorMenuItem sep2 = new SeparatorMenuItem();

        files.getItems().add(1, sep1);
        files.getItems().add(5, sep2);

        return files;
    }

    private Menu historySection() {
        Menu menu = new Menu("_History");

        List<MenuItem> historySectionItems = new LinkedList<>();

        MenuItem rewindItem = new MenuItem("_Rewind");
        rewindItem.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN));
        historySectionItems.add(rewindItem);
        rewindItem.setOnAction(e -> rewind());

        MenuItem undoItem = new MenuItem("_Undo");
        undoItem.setAccelerator(new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN));
        historySectionItems.add(undoItem);
        undoItem.setOnAction(e -> game.getHistory().stepBack());

        MenuItem redoItem = new MenuItem("Red_o");
        redoItem.setAccelerator(new KeyCodeCombination(KeyCode.Y, KeyCombination.CONTROL_DOWN));
        historySectionItems.add(redoItem);
        redoItem.setOnAction(e -> game.getHistory().stepForward());

        MenuItem fastForwardItem = new MenuItem("F_ast Forward");
        fastForwardItem.setAccelerator(new KeyCodeCombination(KeyCode.LESS, KeyCombination.CONTROL_DOWN));
        historySectionItems.add(fastForwardItem);
        fastForwardItem.setOnAction(e -> fastForward());

        menu.getItems().addAll(historySectionItems);

        game.addListener(e -> {
            switch (e.getGameCommand()) {
                case INIT -> menu.getItems().forEach(menuItem -> menuItem.setDisable(true));
                case NEW_GAME, UPDATE -> menu.getItems().forEach(menuItem -> menuItem.setDisable(false));
                default -> {
                    // Nothing else matters.
                }
            }
        });

        return menu;
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

        CheckMenuItem setupMode = new CheckMenuItem("Set_up mode");
        CheckMenuItem moveConfirmationRequired = new CheckMenuItem("Move _confirmation required");
        MenuItem passItem = new MenuItem("_Pass");
        MenuItem resignItem = new MenuItem("_Resign");
        MenuItem confirmItem = new MenuItem("Confirm _move");
        MenuItem scoreGameItem = new MenuItem("Sc_ore Game");

        // This item probably doesn't need a separate accelerator.
        gameSectionItems.add(setupMode);
        setupMode.setAccelerator(new KeyCodeCombination(KeyCode.M, KeyCombination.ALT_DOWN));
        setupMode.setSelected(game.isSetupMode());
        setupMode.setOnAction(e -> {
            game.setSetupMode(setupMode.isSelected());

            passItem.setText(game.isSetupMode() ? "Switch co_lor" : "_Pass");
            resignItem.setDisable(game.getGameState() != Game.GameState.RUNNING);
            scoreGameItem.setDisable(game.getGameState() != Game.GameState.RUNNING);

            GlobalSettings.update();
        });

        gameSectionItems.add(moveConfirmationRequired);
        moveConfirmationRequired.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.ALT_DOWN));
        moveConfirmationRequired.setSelected(GlobalSettings.isConfirmationNeeded());
        moveConfirmationRequired.setOnAction(e -> GlobalSettings.setConfirmationNeeded(moveConfirmationRequired.isSelected()));

        passItem.setAccelerator(new KeyCodeCombination(KeyCode.P));
        gameSectionItems.add(passItem);
        passItem.setOnAction(e -> game.pass());

        resignItem.setAccelerator(new KeyCodeCombination(KeyCode.R, KeyCombination.ALT_DOWN));
        gameSectionItems.add(resignItem);
        resignItem.setOnAction(e -> {
            try {
                game.resign();
            } catch (IllegalStateException ie) {
                CustomExceptionDialog.show(ie, ie.getMessage());
            }
        });

        confirmItem.setAccelerator(new KeyCodeCombination(KeyCode.ENTER));
        gameSectionItems.add(confirmItem);
        confirmItem.setOnAction(e -> GlobalSettings.confirmMove());
        confirmItem.setVisible(false);

        scoreGameItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.ALT_DOWN));
        gameSectionItems.add(scoreGameItem);
        scoreGameItem.setOnAction(e -> {
            try {
                game.scoreGame();
            } catch (IllegalStateException ie) {
                CustomExceptionDialog.show(ie, ie.getMessage());
            }
        });

        menu.getItems().addAll(gameSectionItems);

        game.addListener(e -> {
            switch (e.getGameCommand()) {
                case INIT:
                    playbackControlList.forEach(button -> button.setDisable(true));
                case GAME_WON:
                    gameSectionItems.forEach(menuItem -> menuItem.setDisable(true));
                    break;

                case NEW_GAME:
                    playbackControlList.forEach(button -> button.setDisable(false));
                    setupMode.setSelected(false);
                case UPDATE:
                    gameSectionItems.forEach(menuItem -> menuItem.setDisable(false));
                    if(game.getGameState() != Game.GameState.RUNNING) {
                        resignItem.setDisable(true);
                        scoreGameItem.setDisable(true);
                    }
                    break;
                default:
                    break;
            }

        });

        GlobalSettings.addListener(new ViewListener() {
            @Override
            public void onSettingsUpdated() {
                confirmItem.setVisible(GlobalSettings.isConfirmationNeeded());
            }

            @Override
            public void onMoveConfirmed() {
                // Does not pertain to this
            }
        });

        SeparatorMenuItem sep1 = new SeparatorMenuItem();
        menu.getItems().add(2, sep1);

        SeparatorMenuItem sep2 = new SeparatorMenuItem();
        menu.getItems().add(6, sep2);

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

        CheckMenuItem showMoveNumbersCBtn = new CheckMenuItem("Move _Numbers");
        showMoveNumbersCBtn.setAccelerator(new KeyCodeCombination(KeyCode.N));
        menu.getItems().add(showMoveNumbersCBtn);
        showMoveNumbersCBtn.setSelected(GlobalSettings.isShowMoveNumbers());
        showMoveNumbersCBtn.setOnAction(e -> GlobalSettings.setShowMoveNumbers(showMoveNumbersCBtn.isSelected()));

        CheckMenuItem showCoordinatesCBtn = new CheckMenuItem("_Coordinates");
        showCoordinatesCBtn.setAccelerator(new KeyCodeCombination(KeyCode.C));
        menu.getItems().add(showCoordinatesCBtn);
        showCoordinatesCBtn.setSelected(GlobalSettings.isShowCoordinates());
        showCoordinatesCBtn.setOnAction(e -> GlobalSettings.setShowCoordinates(showCoordinatesCBtn.isSelected()));

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
        Menu menu = new Menu("H_elp");

        MenuItem helpItem = new MenuItem("Hel_p");
        helpItem.setAccelerator(new KeyCodeCombination(KeyCode.H, KeyCombination.CONTROL_DOWN));
        menu.getItems().add(helpItem);
        helpItem.setOnAction(ev -> app.getHostServices().showDocument("https://en.wikipedia.org/wiki/Go_(game)"));

        MenuItem aboutUs = new MenuItem("Abo_ut Us");
        aboutUs.setAccelerator(new KeyCodeCombination(A, KeyCombination.CONTROL_DOWN));
        menu.getItems().add(aboutUs);
        aboutUs.setOnAction(e -> {

            Alert box = new Alert(Alert.AlertType.INFORMATION);
            box.setTitle("About us");
            box.setHeaderText(null);
            box.setContentText("This Go Game was developed by Gerald, Lukas, and Sebastian.");
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
    private HBox shortMenu() throws IOException {
        HBox hotbar = new HBox();
        hotbar.setPrefHeight(35);

        hotbar.setBackground(new Background(new BackgroundFill(this.backColor, new CornerRadii(5), new Insets(0, 5, 0, 5))));

        HBox playbackControl = new HBox();
        playbackControl.setPrefWidth(250);
        playbackControl.setAlignment(Pos.CENTER);
        playbackControl.setSpacing(5);

        Button rewind = new Button("<<");
        rewind.setFocusTraversable(false);
        playbackControlList.add(rewind);
        rewind.setOnAction(e -> rewind());

        Button undo = new Button("<");
        undo.setFocusTraversable(false);
        playbackControlList.add(undo);
        undo.setOnAction(e -> game.getHistory().stepBack());

        Button redo = new Button(">");
        redo.setFocusTraversable(false);
        playbackControlList.add(redo);
        redo.setOnAction(e -> game.getHistory().stepForward());

        Button fastForward = new Button(">>");
        fastForward.setFocusTraversable(false);
        playbackControlList.add(fastForward);
        fastForward.setOnAction(e -> fastForward());

        playbackControl.getChildren().addAll(playbackControlList);

        //Key Bindings for the playback control
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.DIGIT1), rewind::fire);
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.DIGIT2), undo::fire);
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.DIGIT3), redo::fire);
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.DIGIT4), fastForward::fire);

        hotbar.getChildren().add(playbackControl);

        HBox gameCards = new HBox();
        gameCards.prefWidthProperty().bind(this.widthProperty().subtract(250));
        gameCards.setAlignment(Pos.CENTER_LEFT);

        HBox gameShortCuts = new HBox();
        gameShortCuts.prefWidthProperty().bind(this.widthProperty().subtract(250));
        gameShortCuts.setAlignment(Pos.CENTER);
        gameShortCuts.setSpacing(25);

        final String passText = "Pass";

        Button pass = new Button(passText);
        pass.setFocusTraversable(false);
        pass.setOnAction(e -> game.pass());
        gameShortCutList.add(pass);

        Button resign = new Button("Resign");
        resign.setFocusTraversable(false);
        resign.setOnAction(e -> {
            try {
                game.resign();
            } catch (IllegalStateException ie) {
                CustomExceptionDialog.show(ie, ie.getMessage());
            }
        });
        gameShortCutList.add(resign);

        Button scoreGame = new Button("Score Game");
        scoreGame.setFocusTraversable(false);
        scoreGame.setOnAction(e -> {
            try {
                game.scoreGame();
            } catch (IllegalStateException ie) {
            CustomExceptionDialog.show(ie, ie.getMessage());
        }
        });
        gameShortCutList.add(scoreGame);

        Button confirm = new Button("Confirm");
        confirm.setFocusTraversable(false);
        confirm.setVisible(GlobalSettings.isConfirmationNeeded());
        confirm.setOnAction(e -> GlobalSettings.confirmMove());
        gameShortCutList.add(confirm);

        gameShortCuts.getChildren().addAll(gameShortCutList);

        game.addListener(e -> {
            switch (e.getGameCommand()){
                case INIT:
                    pass.setText(passText);
                case GAME_WON:
                    gameShortCutList.forEach(button -> button.setDisable(true));
                    break;
                case NEW_GAME, UPDATE:
                    gameShortCutList.forEach(button -> button.setDisable(false));
                    if(game.getGameState() != Game.GameState.RUNNING) {
                        scoreGame.setDisable(true);
                        resign.setDisable(true);
                    }
                    break;
                default:
                    break;
            }
        });

        gameCards.getChildren().addAll(gameShortCuts);


        // Create combo box for selecting graphics packs
        ObservableList<String> comboBoxItems = FXCollections.observableArrayList();
        File graphicsFolder = new File(GlobalSettings.GRAPHICS_PACK_FOLDER);
        FileFilter zipFilter = pathname -> pathname.getName().toLowerCase().endsWith(".zip");
        File[] directoryListing = graphicsFolder.listFiles(zipFilter);
        if(directoryListing == null) {
            throw new IOException("Couldn't find graphics pack folder!");
        }
        // We still need to filter out non-files because you could have a folder whose name ends with ".zip".
        for (File f : Arrays.stream(directoryListing).filter(File::isFile).toList()) {
            comboBoxItems.add(f.getName());
        }


        ComboBox<String> graphicsPackSelectorComboBox = new ComboBox<>(comboBoxItems);
        graphicsPackSelectorComboBox.setValue(GlobalSettings.getGraphicsPackFileName());
        graphicsPackSelectorComboBox.setTooltip(new Tooltip("Select the graphics pack zip file."));
        graphicsPackSelectorComboBox.setMinWidth(125);
        graphicsPackSelectorComboBox.setTranslateX(-15);
        graphicsPackSelectorComboBox.setFocusTraversable(false);
        graphicsPackSelectorComboBox.setOnAction(e -> GlobalSettings.setGraphicsPackFileName(graphicsPackSelectorComboBox.getValue()));

        GlobalSettings.addListener(new ViewListener() {
            @Override
            public void onSettingsUpdated() {
                confirm.setVisible(GlobalSettings.isConfirmationNeeded());
                pass.setText(game.isSetupMode() ? "Switch color" : passText);
                resign.setDisable(game.getGameState() != Game.GameState.RUNNING);
                scoreGame.setDisable(game.getGameState() != Game.GameState.RUNNING);

                EventHandler<ActionEvent> comboBoxHandler = graphicsPackSelectorComboBox.getOnAction();
                graphicsPackSelectorComboBox.setOnAction(null);
                graphicsPackSelectorComboBox.setValue(GlobalSettings.getGraphicsPackFileName());
                graphicsPackSelectorComboBox.setOnAction(comboBoxHandler);
            }

            @Override
            public void onMoveConfirmed() {
                // Move confirmation does not pertain to this.
            }
        });

        gameCards.getChildren().add(graphicsPackSelectorComboBox);
        hotbar.getChildren().add(gameCards);

        return hotbar;

    }

    private void rewind() {
        History history = game.getHistory();
        if(!history.isAtBeginning()) {
            if((history.getCurrentNode().getSaveToken() == HANDICAP || history.getCurrentNode().getSaveToken() == SETUP)) {
                history.goToBeginning();
            } else {
                history.goBeforeFirstMove();
            }
        }
    }

    private void fastForward() {
        History history = game.getHistory();
        History.HistoryNode n = history.getCurrentNode();
        if(n.getSaveToken() == BEGINNING_OF_HISTORY || n.getSaveToken() == HANDICAP || n.getSaveToken() == SETUP) {
            history.goBeforeFirstMove();
            if(history.getCurrentNode() == n) {
                history.goToEnd();
            }
        } else {
            history.goToEnd();
        }
    }
}
