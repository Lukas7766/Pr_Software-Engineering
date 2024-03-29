package pr_se.gogame.view_controller.dialog;

import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Generates an error dialog
 */
public final class CustomExceptionDialog {

    /**
     * The stage to which the dialog is attached.
     */
    private static Stage stage;

    private CustomExceptionDialog() {
        // This private constructor solely exists to prevent instantiation.
    }

    /**
     * Creates a customised Exception Dialog
     * @param e     the exception to be included in the dialog
     */
    public static void show(Throwable e) {
        show(e, null, null);
    }

    /**
     * Creates a customised Exception Dialog
     * @param e         the exception to be included in the dialog
     * @param message   an additional error message to be displayed in a large font
     */
    public static void show(Throwable e, String message) {
        show(e, message, null);
    }

    /**
     * Creates a customised Exception Dialog
     * @param e         the exception to be included in the dialog
     * @param headerMessage   an additional error message to be displayed in a large font
     * @param contentMessage  an additional error message to be displayed in a slightly smaller font
     */
    public static void show(Throwable e, String headerMessage, String contentMessage) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Exception Dialog");

        alert.setHeaderText(headerMessage != null ? headerMessage : "An error occurred!");

        alert.setContentText(contentMessage != null ? contentMessage : "Developers may see the exception stacktrace for details.");

        if (stage != null) {
            alert.initOwner(stage);
        }

        // Create expandable Exception.
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String exceptionText = sw.toString();

        Label label = new Label("The exception stacktrace was:");

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        // Set expandable Exception into the dialog pane.
        alert.getDialogPane().setExpandableContent(expContent);

        alert.showAndWait();
    }

    /**
     * Sets the stage within which this dialog is displayed
     * @param stage The stage within which this dialog is to be shown
     */
    public static void setStage(Stage stage) {
        CustomExceptionDialog.stage = stage;
    }
}
