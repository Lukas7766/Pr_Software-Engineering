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
import pr_se.gogame.model.StoneColor;

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
     */
    public static void winAction(Stage stage, Game game) {
        System.out.println("last game command: "+game.getGameState());
        if (!(game.getGameState() != GameCommand.WHITEWON || game.getGameState() != GameCommand.BLACKWON || game.getGameState() != GameCommand.DRAW)){
            throw new IllegalArgumentException("Game is not over yet!");
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Result");
        alert.setHeaderText(game.getGameResult().getGameResult());
        alert.initOwner(stage);
        alert.showAndWait();

    }
}
