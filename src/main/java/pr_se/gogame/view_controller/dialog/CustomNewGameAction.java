package pr_se.gogame.view_controller.dialog;

import javafx.scene.control.Alert;
import javafx.stage.Stage;
import pr_se.gogame.model.Game;

public final class CustomNewGameAction {

    private CustomNewGameAction() {
        // This private constructor solely exists to prevent instantiation.
    }

    /**
     * Handles the on new game action<br>
     * -> save <br>
     * -> no save <br>
     * -> cancel <br>
     *
     * @param stage pass stage
     * @param game pass game
     */
    public static void onNewGameAction(Stage stage, Game game) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Go Game - New Game");
        alert.setHeaderText("Do you want to save your game before starting a new one?");
        alert.setContentText("Choose your option:");
        alert.initOwner(stage);

        CustomSaveAction.onSaveAction(
            stage,
            alert,
            game,
            game::initGame,      // onYes
            game::initGame,      // onNo
            () -> {}             // onCancel
        );
    }
}
