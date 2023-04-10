package pr_se.gogame.view_controller;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import pr_se.gogame.model.Board;
import pr_se.gogame.model.Position;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

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

        // Altered by Gerald to add the BoardPane
        Board board = new Board(19);
        final String path = "file:src/main/resources/pr_se/gogame/";
        /*BoardSuperPane bsp = new BoardSuperPane(board,
                path+"tile_0.png",
                path+"tile_0.png",
                path+"stone_0_square.png",
                path+"stone_1.png");*/
        BoardSuperPane2 bsp = new BoardSuperPane2(board,
                path+"tile_0.png",
                path+"tile_0.png",
                path+"stone_0_square.png",
                path+"stone_1.png");
        BorderPane root = new BorderPane();
        root.setCenter(bsp);
        final NumberBinding BSP_ASPECT_RATIO = Bindings.min(root.widthProperty(), root.heightProperty().subtract(menuBar.heightProperty()));
        /*bsp.maxWidthProperty().bind(BSP_ASPECT_RATIO);
        bsp.maxHeightProperty().bind(BSP_ASPECT_RATIO);*/

        root.setTop(vBox);

        Scene scene = new Scene(root, 960, 600);

        stage.setMinWidth(320);
        stage.setMinHeight(200);

        // Same as before from here on
        stage.setScene(scene);
        stage.show();

        // Or is it?
        System.out.println("root width/height: " + root.getWidth() + "/" + root.getHeight());
        System.out.println("menuBar width/height: " + menuBar.getWidth() + "/" + menuBar.getHeight());
        System.out.println("bsp width/height: " + bsp.getWidth() + "/" + bsp.getHeight());
        System.out.println("-------------------------------------------------------------");

        // bsp.printDebugInfo();
        VBox test = (VBox)bsp.getChildren().get(4);
    }

    private Parent createContent() {
        return new StackPane(new Text("Hello World"));
    }

    public static void main(String[] args) {
        launch();
    }
}
