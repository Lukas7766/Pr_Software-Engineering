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

public class CustomWinAction {

    /**
     * Handles the win action<br>
     * -> without save <br>
     * -> with save <br>
     *
     * @param stage pass stage
     * @param game pass game
     * @param e Event
     * @param filterList pass list of Extension Filters
     */
    public static void winAction(Stage stage, Game game, HashSet<FileChooser.ExtensionFilter> filterList) {
        System.out.println("last game command: "+game.getGameState());
        if (!(game.getGameState() != GameCommand.WHITEWON || game.getGameState() != GameCommand.BLACKWON || game.getGameState() != GameCommand.DRAW)){
            throw new IllegalArgumentException("Game is not over yet!");
        }

        String gameResult = "Game result: ";
        switch (game.getGameState()){
            case WHITEWON -> gameResult += "White won!";
            case BLACKWON -> gameResult += "Black won!";
            case DRAW -> gameResult += "Draw";
            default -> throw new IllegalStateException("Unexpected value: " + game.getGameState());
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(gameResult);
        alert.setHeaderText(gameResult+"\nDo you want to save your Game?");
        alert.setContentText("Choose your option:");
        alert.initOwner(stage);

        ButtonType noSaveBtn = new ButtonType("without save");
        ButtonType saveBtn = new ButtonType("with save");

        alert.getButtonTypes().setAll(saveBtn, noSaveBtn);

        Optional<ButtonType> btnResult = alert.showAndWait();

        btnResult.ifPresent(er -> {
            switch (er.getText()) {
                case "without save" -> game.initGame();
                case "with save" -> {
                    File f = CustomFileDialog.getFile(stage,true, filterList);
                    if (f != null) {
                        if (game.saveGame(f.toPath())) {
                            game.initGame();
                        }
                    }
                    System.out.println("Info");
                    Alert info = new Alert(Alert.AlertType.INFORMATION);
                    info.setTitle("Go Game - Info");
                    info.setHeaderText("Saving your game didn't work.");
                    info.setContentText("Try it manually save your game or start a new one!");
                    info.initOwner(stage);
                    info.showAndWait();
                }
            }
        });
    }
}
