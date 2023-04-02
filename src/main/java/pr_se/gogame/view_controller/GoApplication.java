package pr_se.gogame.view_controller;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import pr_se.gogame.model.Board;
import pr_se.gogame.model.Game;

public class GoApplication extends Application {

    private static final int WIDTH = 760;
    private static final int HEIGHT = 580;

    @Override
    public void start(Stage stage) throws Exception {

        stage.setTitle("Go Game - App");
        Game game = new Game();

        // Altered by Gerald to add the BoardPane
        Board board = new Board(19);
        final String path = "file:src/main/resources/pr_se/gogame/";
        BoardPane boardPane = new BoardPane(board,
                path + "tile_0.png",
                path + "tile_1.png",
                path + "stone_0.png",
                path + "stone_1.png");

        BorderPane root = new BorderPane();
        root.setCenter(boardPane);
        root.setTop(new HeaderPane(this, stage, game));
        root.setLeft(new SidePane());

        Scene scene = new Scene(root, WIDTH, HEIGHT);
        stage.setMinHeight(HEIGHT + 40);
        stage.setMinWidth(WIDTH + 20);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
