package pr_se.gogame.view_controller;

import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Pair;
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
        if (game.getGameState() != GameCommand.GAME_WON){
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
