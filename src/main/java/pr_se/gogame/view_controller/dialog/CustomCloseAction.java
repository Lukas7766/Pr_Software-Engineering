package pr_se.gogame.view_controller.dialog;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import pr_se.gogame.model.Game;

/**
 * Generates a dialog for closing the game
 */
public final class CustomCloseAction {

    private CustomCloseAction() {
        // This private constructor solely exists to prevent instantiation.
    }

    /**
     * Handles the on close action<br>
     * -> save <br>
     * -> no save <br>
     * -> cancel <br>
     *
     * @param stage pass stage
     * @param game pass game
     * @param e Event
     */
    public static void onCloseAction(Stage stage, Game game, Event e) {
        if (game.getGameState() == Game.GameState.NOT_STARTED_YET) {
            Platform.exit();
            System.exit(0);
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Go Game - Close Game");
        alert.setHeaderText("Do you want to save your Game before closing?");
        alert.initOwner(stage);

        CustomSaveAction.onSaveAction(
            stage,
            alert,
            game,
            () -> {                 // onYes
                if(e != null) {
                    e.consume();
                }

                Platform.exit();
            },
            Platform::exit,         // onNo
            () -> {                 // onCancel
                if(e != null) {
                    e.consume();
                }
            }
        );
    }
}
