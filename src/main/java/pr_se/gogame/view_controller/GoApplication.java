package pr_se.gogame.view_controller;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import pr_se.gogame.model.Board;

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

        BorderPane root = new BorderPane();

        // Altered by Gerald to add the BoardPane
        Board board = new Board(19);
        final String path = "file:src/main/resources/pr_se/gogame/";

        BoardPane bp = new BoardPane(board,
            path+"tile_0.png",
            path+"tile_0.png",
            path+"edge.png",
            path+"corner.png",
            path+"stone_0_square.png",
            path+"stone_1.png");
        bp.setBackground(new Background(new BackgroundFill(Color.YELLOW, null, null)));

        root.setTop(vBox);
        root.setCenter(bp);

        Scene scene = new Scene(root, 960, 600);

        stage.setMinWidth(320);
        stage.setMinHeight(200);

        root.widthProperty().addListener((o, n, t) -> System.out.println("New width: " + t));
        root.heightProperty().addListener((o, n, t) -> System.out.println("New height: " + t));

        // Same as before from here on
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
