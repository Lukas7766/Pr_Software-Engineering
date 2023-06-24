package pr_se.gogame.view_controller.dialog;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import pr_se.gogame.model.file.FileHandler;
import pr_se.gogame.model.Game;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

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
    public static void onSaveAction(Stage stage, Game game) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

        alert.setTitle("Go Game - Save Game");
        alert.setHeaderText("Do you want to save your game before starting a new one?");
        alert.setContentText("Choose your option:");
        alert.initOwner(stage);

        ButtonType noBtn = new ButtonType("no");
        ButtonType saveBtn = new ButtonType("yes");
        ButtonType cancelBtn = new ButtonType("cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(saveBtn, noBtn, cancelBtn);

        Optional<ButtonType> btnResult = alert.showAndWait();

        btnResult.ifPresent(er -> {
            switch (er.getText()){
                case "no" -> game.initGame();
                case "yes" -> {
                    File f = FileHandler.getCurrentFile();
                    if(f == null){
                        f = CustomFileDialog.getFile(stage,true);
                        if(f == null) {
                            return;
                        }
                    }

                    if (!game.saveGame(f)) {
                        CustomExceptionDialog.show(new IOException(), "Could not save the game!");
                    }
                }
                default -> {
                    // This comment is here to fill the default case, otherwise SonarQube will complain (as it would in the absence of a default case).
                }
            }
        });
    }

}
