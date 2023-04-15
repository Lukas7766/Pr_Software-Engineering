package pr_se.gogame.view_controller;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import pr_se.gogame.model.Game;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import pr_se.gogame.model.GameCommand;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HeaderPane extends VBox {

    private final Color backColor;
    private final Application app;
    private final Stage stage;
    private final Game game;
    private final HashSet<FileChooser.ExtensionFilter> filterList;

    public HeaderPane(Color backcolor, Application app, Stage stage, Game game) {
        this.backColor = backcolor;
        this.app = app;
        this.stage = stage;
        this.game = game;

        this.filterList = Stream.of(new FileChooser.ExtensionFilter("Go Game", "*.goGame"))
                .collect(Collectors.toCollection(HashSet::new));

        this.setSpacing(5);

        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().add(fileSection());
        menuBar.getMenus().add(gameSection());
        menuBar.getMenus().add(helpSection());

        this.getChildren().add(menuBar);
        this.getChildren().add(shortMenu());
    }

    private HBox shortMenu() {
        HBox lane = new HBox();
        lane.setPrefHeight(35);

        lane.setBackground(new Background(new BackgroundFill(this.backColor, new CornerRadii(5), new Insets(0, 5, 0, 5))));

        List<Button> playbackControlList = new ArrayList<>();


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

        List<Button> gameShortCardList = new ArrayList<>();

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
            File f = fileDialog(false, filterList);
            if (f != null) game.importGame(f.toPath());
            else System.out.println("Import Dialog cancelled");
        });

        MenuItem exportFileItem = new MenuItem();
        exportFileItem.setText("Export Game");
        files.getItems().add(exportFileItem);
        exportFileItem.setOnAction(e -> {
            File f = fileDialog(true, filterList);
            if (f != null) game.exportGame(f.toPath());
            else System.out.println("Export Dialog cancelled");
        });

        MenuItem exitGameItem = new MenuItem();
        exitGameItem.setText("Exit Game");
        files.getItems().add(exitGameItem);
        exitGameItem.setOnAction(e -> onCloseAction());


        SeparatorMenuItem sep1 = new SeparatorMenuItem();
        SeparatorMenuItem sep2 = new SeparatorMenuItem();

        files.getItems().add(1, sep1);
        files.getItems().add(4, sep2);

        return files;
    }

    private void onCloseAction() {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Close Go Game");
        alert.setHeaderText("Do you really want to close your Game?");
        alert.setContentText("Choose your option:");
        alert.initOwner(stage);

        ButtonType noSaveBtn = new ButtonType("without save");
        ButtonType saveBtn = new ButtonType("with save");
        ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(saveBtn, noSaveBtn, cancelBtn);

        Optional<ButtonType> btnResult = alert.showAndWait();

        if (btnResult.get() == noSaveBtn) {
            Platform.exit();
        } else if (btnResult.get() == saveBtn) {
            if (game.saveGame()) Platform.exit();
            else throw new RuntimeException();
        }
    }

    private Menu gameSection() {
        Menu menu = new Menu();
        menu.setText("Game");

        List<MenuItem> gameSectionItems = new ArrayList<>();

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

        SeparatorMenuItem sep = new SeparatorMenuItem();

        menu.getItems().add(2, sep);

        return menu;
    }

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

    private File fileDialog(boolean isSave, HashSet<FileChooser.ExtensionFilter> filter) {

        FileChooser fileChooser = new FileChooser();

        if (filter != null && !filter.isEmpty()) filter.forEach(i -> fileChooser.getExtensionFilters().add(i));

        return (isSave) ? fileChooser.showSaveDialog(stage) : fileChooser.showOpenDialog(stage);
    }
}
