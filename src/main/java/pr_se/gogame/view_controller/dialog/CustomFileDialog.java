package pr_se.gogame.view_controller.dialog;

import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

/**
 * Generates a custom file chooser
 */
public final class CustomFileDialog {
    private CustomFileDialog() {
        // This private constructor solely exists to prevent instantiation.
    }

    /**
     * Creates a parameterizes File Dialog
     * @param stage pass stage
     * @param isSave true for saving a file, false for opening a file
     * @return where to save or open the FILE
     */
    public static File getFile(Stage stage, boolean isSave) {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Go Game", "*.sgf");
        fileChooser.getExtensionFilters().add(filter);

        return (isSave) ? fileChooser.showSaveDialog(stage) : fileChooser.showOpenDialog(stage);
    }
}
