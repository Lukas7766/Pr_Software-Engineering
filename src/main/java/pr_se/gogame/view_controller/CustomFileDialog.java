package pr_se.gogame.view_controller;

import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.HashSet;
import java.util.Optional;

public class CustomFileDialog {


    /**
     * Creates a parameterizes File Dialog
     * @param stage pass stage
     * @param isSave true for saving a file, false for opening a file
     * @param filter pass list of Extension Filters
     * @return where to save or open the FILE
     */
    public static File getFile(Stage stage, boolean isSave, HashSet<FileChooser.ExtensionFilter> filter) {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        if (filter != null && !filter.isEmpty()) filter.forEach(i -> fileChooser.getExtensionFilters().add(i));

        return (isSave) ? fileChooser.showSaveDialog(stage) : fileChooser.showOpenDialog(stage);
    }
}
