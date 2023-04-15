package pr_se.gogame.view_controller;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
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

        BorderPane root = new BorderPane();

        final String path = "file:src/main/resources/pr_se/gogame/";

        stage.getIcons().add(new Image(path+"go.png"));

        BoardPane bp = new BoardPane(game,
                path + "tile_0.png",
                path + "tile_0.png",
                path + "edge.png",
                path + "corner.png",
                path + "stone_0_square.png",
                path + "stone_1.png");
        bp.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, new CornerRadii(5), new Insets(5, 5, 5, 2.5))));

        HeaderPane hp = new HeaderPane(Color.LIGHTGRAY, this, stage, game);

        SidePane sp = new SidePane(Color.LIGHTGRAY, game);

        root.setCenter(bp);
        root.setTop(hp);
        root.setLeft(sp);

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
