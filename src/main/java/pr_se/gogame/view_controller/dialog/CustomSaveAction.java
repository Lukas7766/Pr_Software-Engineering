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
        ButtonType noBtn = new ButtonType("no");
        ButtonType saveBtn = new ButtonType("yes");
        ButtonType cancelBtn = new ButtonType("cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(saveBtn, noBtn, cancelBtn);

        Optional<ButtonType> btnResult = alert.showAndWait();

        btnResult.ifPresent(er -> {
            switch (er.getText()) {
                case "no" -> onNo.use();
                case "yes" -> {
                    File f = game.getFileHandler().getCurrentFile();
                    if (f == null) {
                        f = CustomFileDialog.getFile(stage, true);
                        if (f == null) {
                            return;
                        }
                    }

                    if (!game.getFileHandler().saveFile(f)) {
                        CustomExceptionDialog.show(new IOException(), "Could not save the game!");
                    }

                    onYes.use();
                }
                case "cancel" -> onCancel.use();
                default -> {
                    // This comment is here to fill the default case, otherwise SonarQube will complain (as it would in the absence of a default case).
                }
            }
        });
    }
}
