package pr_se.gogame.view_controller.dialog;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import pr_se.gogame.model.Game;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

/**
 * Handles dialogues that ask whether the user would like to save the game
 */
public class CustomSaveAction {
    private CustomSaveAction() {
        // This private constructor solely exists to prevent instantiation.
    }

    /**
     * Shows the supplied alert and asks whether the game should be saved; Executes the passed functional interfaces
     * @param stage the stage which this dialog is supposed to be contained in
     * @param alert the alert which this dialog should be based on
     * @param game the game that would be saved
     * @param onYes additional actions to perform if the "Yes" button is clicked
     * @param onNo additional actions to perform if the "No" button is clicked
     * @param onCancel additional actions to perform if the "Cancel" button is clicked
     */
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

    /**
     * Saves the supplied game, showing a file chooser if no file is selected or the as parameter is true
     * @param game the game that is to be saved
     * @param stage the stage in which this dialog should be contained
     * @param as whether a file chooser should be displayed regardless of the presence of a loaded file
     * @return whether saving the game was successful
     */
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
