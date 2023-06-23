package pr_se.gogame.view_controller;

import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Stage;
import javafx.util.Pair;
import pr_se.gogame.model.Game;
import pr_se.gogame.model.GameState;

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
        if (game.getGameState() != GameState.GAME_OVER){
            throw new IllegalStateException("Game is not over yet!");
        }

        Dialog<Pair<String,String>> dialog = new Dialog<>();

        dialog.setTitle("Game Result");
        dialog.setHeaderText(game.getGameResult().getGameResult());
        dialog.initOwner(stage);

        ButtonType loginButtonType = new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType);

        dialog.showAndWait();

    }
}
