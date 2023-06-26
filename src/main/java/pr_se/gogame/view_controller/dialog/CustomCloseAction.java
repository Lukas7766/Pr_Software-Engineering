package pr_se.gogame.view_controller.dialog;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import pr_se.gogame.model.Game;
import pr_se.gogame.model.file.FileHandler;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

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
        alert.setContentText("Choose your option:");
        alert.initOwner(stage);

        ButtonType noSaveBtn = new ButtonType("no");
        ButtonType saveBtn = new ButtonType("yes");
        ButtonType cancelBtn = new ButtonType("cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(saveBtn, noSaveBtn, cancelBtn);

        Optional<ButtonType> btnResult = alert.showAndWait();

        btnResult.ifPresent(er -> {
            switch (er.getText()) {
                case "no" -> Platform.exit();
                case "yes" -> {
                    File f = FileHandler.getCurrentFile();
                    if(f == null){
                        f = CustomFileDialog.getFile(stage,true);
                        if(f == null) {
                            return;
                        }
                    }

                    if (game.saveGame(f)) {
                        Platform.exit();
                        System.exit(0);
                    }

                    CustomExceptionDialog.show(new IOException(), "Could not save the game!");
                    if (e != null) e.consume();
                }
                case "cancel" ->  {if (e != null) e.consume();}
                default -> {
                    // This comment is here to fill the default case, otherwise SonarQube will complain (as it would in the absence of a default case).
                }
            }
        });
    }
}
