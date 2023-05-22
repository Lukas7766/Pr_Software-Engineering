package pr_se.gogame.view_controller;

import javafx.scene.control.Alert;
import javafx.stage.Stage;
import pr_se.gogame.model.Game;
import pr_se.gogame.model.GameCommand;

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
        if (!(game.getGameState() != GameCommand.WHITE_WON || game.getGameState() != GameCommand.BLACK_WON)){
            throw new IllegalArgumentException("Game is not over yet!");
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Result");
        alert.setHeaderText(game.getGameResult().getGameResult());
        alert.initOwner(stage);
        alert.showAndWait();

    }
}
