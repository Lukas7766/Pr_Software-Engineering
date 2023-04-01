package pr_se.gogame.view_controller;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import pr_se.gogame.model.Game;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HeaderPane extends StackPane {

    private final Application app;
    private final Stage stage;

    private final Game game;
    private final HashSet<FileChooser.ExtensionFilter> filterList;

    public HeaderPane(Application app, Stage stage, Game game) {
        this.app = app;
        this.stage = stage;
        this.game = game;

        this.filterList = Stream.of(new FileChooser.ExtensionFilter("Go Game", "*.goGame"))
                .collect(Collectors.toCollection(HashSet::new));

        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().add(fileSection());
        menuBar.getMenus().add(gameSection());
        menuBar.getMenus().add(helpSection());
        menuBar.setBackground(Background.fill(Color.LIGHTGRAY));





        Rectangle r = new Rectangle();
        r.setFill(Color.LIGHTGRAY);
        r.setHeight(35);
        r.setArcHeight(10.0);
        r.setArcWidth(10.0);


        this.getChildren().add(r);
        this.getChildren().add(menuBar);

        //necessary otherwise padding is not working -> scale up does but scale down doesn't
        //setMinSize(0,0);

        //top, right, bottom, left
        setPadding(new Insets(5, 5, 5, 5));
        r.widthProperty().bind(widthProperty().subtract(10));
    }

    private Menu fileSection() {
        Menu files = new Menu();
        files.setText("File");

        MenuItem newGameItem = new MenuItem();
        newGameItem.setText("New Game");
        files.getItems().add(newGameItem);
        newGameItem.setOnAction(e -> game.newGame());

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

        MenuItem passItem = new MenuItem();
        passItem.setText("Pass");
        menu.getItems().add(passItem);
        passItem.setOnAction(e -> game.pass());

        MenuItem resignItem = new MenuItem();
        resignItem.setText("Resign");
        menu.getItems().add(resignItem);
        resignItem.setOnAction(e -> game.resign());

        MenuItem scoreGameItem = new MenuItem();
        scoreGameItem.setText("Score Game");
        menu.getItems().add(scoreGameItem);
        scoreGameItem.setOnAction(e -> game.scoreGame());

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
