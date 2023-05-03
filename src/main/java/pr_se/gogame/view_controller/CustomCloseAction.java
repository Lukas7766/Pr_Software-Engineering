package pr_se.gogame.view_controller;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import pr_se.gogame.model.Game;
import pr_se.gogame.model.GameCommand;

import java.io.File;
import java.util.HashSet;
import java.util.Optional;

public class CustomCloseAction {

    /**
     * Handles the on close action<br>
     * -> without save <br>
     * -> with save <br>
     * -> cancel <br>
     *
     * @param stage pass stage
     * @param game pass game
     * @param e Event
     * @param filterList pass list of Extension Filters
     */
    public static void onCloseAction(Stage stage, Game game, Event e, HashSet<FileChooser.ExtensionFilter> filterList) {

        if (game.getGameState() == GameCommand.INIT) {
            Platform.exit();
            System.exit(0);
        }
        System.out.println(game.getGameState());
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

        btnResult.ifPresent(er -> {
            switch (er.getText()) {
                case "without save" -> Platform.exit();
                case "with save" -> {
                    File f = CustomFileDialog.getFile(stage,true, filterList);//fileDialog(true, filterList)
                    if (f != null) {
                        if (game.saveGame(f.toPath())) {
                            Platform.exit();
                            System.exit(0);
                        }
                    }
                    System.out.println("Info");
                    Alert info = new Alert(Alert.AlertType.INFORMATION);
                    info.setTitle("Close Go Game - Info");
                    info.setHeaderText("Saving your game didn't work.");
                    info.setContentText("Try it again!");
                    info.initOwner(stage);
                    info.showAndWait();
                    if (e != null) e.consume();
                }
                case "Cancel" ->  {if (e != null) e.consume();}
            }
        });
    }
}
