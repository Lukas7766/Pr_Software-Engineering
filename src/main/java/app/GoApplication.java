package app;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class GoApplication extends Application {
    @Override
    public void start(Stage stage) throws Exception {

        stage.setTitle("Go Game - App");

        //Menu test - start
        Menu files = new Menu();
        files.setText("files");

        MenuItem importFile = new MenuItem();
        importFile.setText("import file");
        files.getItems().add(importFile);

        MenuItem exportFile = new MenuItem();
        exportFile.setText("export file");
        files.getItems().add(exportFile);
        exportFile.setOnAction(e -> System.out.println("hihi"));

        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().add(files);
        //Menu test - end

        VBox vBox = new VBox(menuBar);

        Scene scene = new Scene(vBox, 960, 600);

        stage.setScene(scene);
        stage.show();
    }

    private Parent createContent() {
        return new StackPane(new Text("Hello World"));
    }

    public static void main(String[] args) {
        launch();
    }
}
