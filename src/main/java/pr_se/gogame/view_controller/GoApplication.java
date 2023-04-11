package pr_se.gogame.view_controller;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
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

        BorderPane root = new BorderPane();

        // Altered by Gerald to add the BoardPane
        Board board = new Board(5);
        final String path = "file:src/main/resources/pr_se/gogame/";

        BoardSuperPane bsp = new BoardSuperPane(board,
                path+"tile_0.png",
                path+"tile_0.png",
                path+"stone_0_square.png",
                path+"stone_1.png");
        /*final NumberBinding BSP_ASPECT_RATIO = Bindings.min(root.widthProperty(), root.heightProperty().subtract(menuBar.heightProperty()));
        bsp.maxWidthProperty().bind(BSP_ASPECT_RATIO);
        bsp.maxHeightProperty().bind(BSP_ASPECT_RATIO);*/
        /*BoardSuperPane2 bsp = new BoardSuperPane2(board,
                path+"tile_0.png",
                path+"tile_0.png",
                path+"stone_0_square.png",
                path+"stone_1.png");*/
        Pane dummyPane = new StackPane();
        /*dummyPane.minWidthProperty().bind(bsp.getCoordsLeft().widthProperty().add(bsp.getCoordsAbove().widthProperty()).add(bsp.getCoordsRight().widthProperty()));
        dummyPane.minHeightProperty().bind(bsp.getCoordsAbove().heightProperty().add(bsp.getCoordsLeft().heightProperty()).add(bsp.getCoordsBelow().heightProperty()));
        dummyPane.prefWidthProperty().bind(root.widthProperty().subtract(bsp.getCoordsLeft().widthProperty()).subtract(bsp.getCoordsRight().widthProperty()));
        dummyPane.prefHeightProperty().bind(root.heightProperty().subtract(bsp.getCoordsAbove().heightProperty()).subtract(bsp.getCoordsBelow().heightProperty()));
        dummyPane.maxWidthProperty().bind(Bindings.createDoubleBinding(() -> 960.0));
        dummyPane.maxHeightProperty().bind(Bindings.createDoubleBinding(() -> 600.0));*/
        dummyPane.getChildren().add(bsp);
        Rectangle testRectangle = new Rectangle();
        // testRectangle.widthProperty().bind(bsp.getCoordsLeft().widthProperty().add(bsp.getCoordsAbove().widthProperty()).add(bsp.getCoordsRight().widthProperty()));
        // testRectangle.heightProperty().bind(bsp.getCoordsAbove().heightProperty().add(bsp.getCoordsLeft().heightProperty()).add(bsp.getCoordsBelow().heightProperty()));
        // root.setCenter(dummyPane);
        // root.setLeft(testRectangle);

        BoardPane2 bp2 = new BoardPane2(board,
            path+"tile_0.png",
            path+"tile_0.png",
            path+"stone_0_square.png",
            path+"stone_1.png");
        bp2.setBackground(new Background(new BackgroundFill(Color.YELLOW, null, null)));

        root.setCenter(bsp);
        root.setTop(vBox);

        StackPane visualAidLeft = new StackPane();
        visualAidLeft.setBackground(new Background(new BackgroundFill(Color.PINK, null, null)));

        Label l = new Label("Left");
        visualAidLeft.getChildren().add(l);

        StackPane visualAidTop = new StackPane();
        visualAidTop.setBackground(new Background(new BackgroundFill(Color.GREENYELLOW, null, null)));

        Label m = new Label("Top");
        visualAidTop.getChildren().add(m);

        Image i = new Image(path + "stone_0_square.png");
        ImageView iv = new ResizableImageView(i);
        iv.setPreserveRatio(true);

        StackPane visualAidC = new StackPane();
        visualAidC.setBackground(new Background(new BackgroundFill(Color.RED, null, null)));
        visualAidC.getChildren().add(iv);

        BorderPane pane = new BorderPane();
        // pane.setCenter(visualAidC);
        pane.setCenter(bp2);
        pane.setLeft(visualAidLeft);
        pane.setTop(visualAidTop);

        // Scene scene = new Scene(root, 960, 600);
        Scene scene = new Scene(pane, 960, 600);
        // Scene scene = new Scene(bp2, 960, 600);

        stage.setMinWidth(320);
        stage.setMinHeight(200);

        // Same as before from here on
        stage.setScene(scene);
        stage.show();

        // Or is it?
        /*System.out.println("root width/height: " + root.getWidth() + "/" + root.getHeight());
        System.out.println("menuBar width/height: " + menuBar.getWidth() + "/" + menuBar.getHeight());
        System.out.println("bsp width/height: " + bsp.getWidth() + "/" + bsp.getHeight());
        System.out.println("-------------------------------------------------------------");*/

        // bsp.printDebugInfo();
        // VBox test = (VBox)bsp.getChildren().get(4);

        root.widthProperty().addListener((o, n, t) -> System.out.println("New width: " + t));
        root.heightProperty().addListener((o, n, t) -> System.out.println("New height: " + t));
    }

    private Parent createContent() {
        return new StackPane(new Text("Hello World"));
    }

    public static void main(String[] args) {
        launch();
    }
}
