package pr_se.gogame.view_controller.dialog;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import pr_se.gogame.model.Game;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class CustomSaveAction {
    private CustomSaveAction() {
        // This private constructor solely exists to prevent instantiation.
    }

    public static void onSaveAction(Stage stage, Alert alert, Game game, Procedure onYes, Procedure onNo, Procedure onCancel) {
        ButtonType noBtn = new ButtonType("No");
        ButtonType saveBtn = new ButtonType("Yes");
        ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(saveBtn, noBtn, cancelBtn);

        Optional<ButtonType> btnResult = alert.showAndWait();

        btnResult.ifPresent(er -> {
            switch (er.getText()) {
                case "No" -> onNo.use();
                case "Yes" -> {
                    if(saveGame(game, stage, false)) {
                        onYes.use();
                    }
                }
                case "Cancel" -> onCancel.use();
                default -> {
                    // This comment is here to fill the default case, otherwise SonarQube will complain (as it would in the absence of a default case).
                }
            }
        });
    }

    public static boolean saveGame(Game game, Stage stage, boolean as) {
        File saveGameFile = game.getFileHandler().getCurrentFile();

        if (as || saveGameFile == null) {
            saveGameFile = CustomFileDialog.getFile(stage, true);
            if (saveGameFile == null) {
                return false;
            }
        }
        try {
            if (!game.getFileHandler().saveFile(saveGameFile)) {
                CustomExceptionDialog.show(new IOException(), "Failed to save the game!");
            }
        } catch (IOException e) {
            CustomExceptionDialog.show(e, "Could not save the game!", e.getMessage());
        } catch(IllegalStateException isE) {
            CustomExceptionDialog.show(isE, "An error occurred while saving the game.", isE.getMessage());
        }

        return true;
    }
}
