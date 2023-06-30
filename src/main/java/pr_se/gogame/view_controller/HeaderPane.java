package pr_se.gogame.view_controller;

import javafx.application.Application;
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
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import pr_se.gogame.model.Game;
import pr_se.gogame.model.file.LoadingGameException;
import pr_se.gogame.view_controller.dialog.CustomCloseAction;
import pr_se.gogame.view_controller.dialog.CustomExceptionDialog;
import pr_se.gogame.view_controller.dialog.CustomFileDialog;
import pr_se.gogame.view_controller.dialog.CustomNewGameAction;
import pr_se.gogame.view_controller.observer.ViewListener;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    private final List<Button> playbackControlList = new ArrayList<>();

    private final List<Button> gameShortCutList = new ArrayList<>();

    private final List<MenuItem> gameSectionItems = new ArrayList<>();

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

        MenuItem importFileItem = new MenuItem("L_oad Game");
        importFileItem.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
        files.getItems().add(importFileItem);
        importFileItem.setOnAction(e -> {
            File f = CustomFileDialog.getFile(stage, false);
            if (f != null) {
                try {
                    if(!game.getFileHandler().loadFile(f)) {
                        CustomExceptionDialog.show(new IOException(), "Failed to load game!\n\nThis is probably due to a file system error.");
                    }
                } catch (LoadingGameException lgException) {
                    CustomExceptionDialog.show(lgException, "Failed to load game!\n\nThis is probably because the file contains unsupported SGF features.");
                } catch (java.nio.file.NoSuchFileException nsfException) {
                    CustomExceptionDialog.show(nsfException, "File does not exist!");
                }
            }
        });

        MenuItem exportFileItem = new MenuItem("_Save");
        exportFileItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
        files.getItems().add(exportFileItem);
        exportFileItem.setOnAction(e -> saveGame(false));

        MenuItem exportFileItemAs = new MenuItem("Save _as");
        exportFileItemAs.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.SHIFT_DOWN, KeyCombination.CONTROL_DOWN));
        files.getItems().add(exportFileItemAs);
        exportFileItemAs.setOnAction(e -> saveGame(true));


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
        MenuItem scoreGameItem = new MenuItem("_Score Game");

        // This item probably doesn't need a separate accelerator.
        gameSectionItems.add(setupMode);
        setupMode.setSelected(game.isSetupMode());
        setupMode.setOnAction(e -> {
            game.setSetupMode(setupMode.isSelected());

            passItem.setText(game.isSetupMode() ? "Switch co_lor" : "_Pass");

            GlobalSettings.update();
        });


        moveConfirmationRequired.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.ALT_DOWN));
        gameSectionItems.add(moveConfirmationRequired);
        moveConfirmationRequired.setSelected(GlobalSettings.isConfirmationNeeded());
        moveConfirmationRequired.setOnAction(e -> GlobalSettings.setConfirmationNeeded(moveConfirmationRequired.isSelected()));

        passItem.setAccelerator(new KeyCodeCombination(KeyCode.P, KeyCombination.ALT_DOWN));
        gameSectionItems.add(passItem);
        passItem.setOnAction(e -> game.pass());

        resignItem.setAccelerator(new KeyCodeCombination(KeyCode.R, KeyCombination.ALT_DOWN));
        gameSectionItems.add(resignItem);
        resignItem.setOnAction(e -> game.resign());

        scoreGameItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.ALT_DOWN));
        gameSectionItems.add(scoreGameItem);
        scoreGameItem.setOnAction(e -> game.scoreGame());

        menu.getItems().addAll(gameSectionItems);

        game.addListener(e -> {
            switch (e.getGameCommand()) {
                case INIT:
                    playbackControlList.forEach(button -> button.setDisable(true));

                case GAME_WON:
                    gameSectionItems.forEach(menuItem -> menuItem.setDisable(true));
                    break;

                case NEW_GAME, UPDATE:
                    gameSectionItems.forEach(menuItem -> menuItem.setDisable(false));
                    setupMode.setSelected(false);
                    playbackControlList.forEach(button -> button.setDisable(false));
                    break;
                default:
                    break;
            }

        });

        SeparatorMenuItem sep1 = new SeparatorMenuItem();
        menu.getItems().add(2, sep1);

        SeparatorMenuItem sep2 = new SeparatorMenuItem();
        menu.getItems().add(5, sep2);

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
    private HBox shortMenu() throws IOException {
        HBox lane = new HBox();
        lane.setPrefHeight(35);

        lane.setBackground(new Background(new BackgroundFill(this.backColor, new CornerRadii(5), new Insets(0, 5, 0, 5))));

        HBox playbackControl = new HBox();
        playbackControl.setPrefWidth(250);
        playbackControl.setAlignment(Pos.CENTER);
        playbackControl.setSpacing(5);

        Button fastBackward = new Button("<<");
        fastBackward.setFocusTraversable(false);
        playbackControlList.add(fastBackward);
        fastBackward.setOnAction(e -> game.rewind());

        Button backward = new Button("<");
        backward.setFocusTraversable(false);
        playbackControlList.add(backward);
        backward.setOnAction(e -> game.undo());

        Button forward = new Button(">");
        forward.setFocusTraversable(false);
        playbackControlList.add(forward);
        forward.setOnAction(e -> game.redo());

        Button fastForward = new Button(">>");
        fastForward.setFocusTraversable(false);
        playbackControlList.add(fastForward);
        fastForward.setOnAction(e -> game.fastForward());

        playbackControl.getChildren().addAll(playbackControlList);

        //Key Bindings for the playback control
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.F), () -> {
            if (!backward.isDisabled()) {
                backward.fire();
            }
        });
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.H), () -> {
            if (!forward.isDisabled()) {
                forward.fire();
            }
        });
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.T), () -> {
            if (!fastForward.isDisabled()) {
                fastForward.fire();
            }
        });
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.G), () -> {
            if (!fastBackward.isDisabled()) {
                fastBackward.fire();
            }
        });


        lane.getChildren().add(playbackControl);

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
        resign.setOnAction(e -> game.resign());
        gameShortCutList.add(resign);

        Button scoreGame = new Button("Score Game");
        scoreGame.setFocusTraversable(false);
        scoreGame.setOnAction(e -> game.scoreGame());
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
                    break;
                default:
                    break;
            }
        });

        GlobalSettings.addListener(new ViewListener() {
            @Override
            public void onSettingsUpdated() {
                confirm.setVisible(GlobalSettings.isConfirmationNeeded());
                pass.setText(game.isSetupMode() ? "Switch color" : passText);
            }

            @Override
            public void onMoveConfirmed() {
                // Move confirmation does not pertain to this.
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
        graphicsPackSelectorComboBox.setOnAction(e -> GlobalSettings.setGraphicsPackFileName("/" + graphicsPackSelectorComboBox.getValue()));

        gameShortCuts.getChildren().add(graphicsPackSelectorComboBox);

        gameCards.getChildren().add(graphicsPackSelectorComboBox);
        lane.getChildren().add(gameCards);

        return lane;

    }

    private void saveGame(boolean as) {
        File saveGameFile = game.getFileHandler().getCurrentFile();

        if (as || saveGameFile == null) {
            saveGameFile = CustomFileDialog.getFile(stage, true);
            if (saveGameFile == null) {
                return;
            }
        }
        try {
            if (!game.getFileHandler().saveFile(saveGameFile)) {
                CustomExceptionDialog.show(new IOException(), "Could not save the game!");
            }
        } catch (IOException e) {
            CustomExceptionDialog.show(e, "Could not save the game!");
        } catch(IllegalStateException isE) {
            CustomExceptionDialog.show(isE, "An error occurred while saving the game.");
        }
    }

}
