package pr_se.gogame.view_controller;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import pr_se.gogame.model.Game;

public class GoApplication extends Application {

    private static final int WIDTH = 760;
    private static final int HEIGHT = 580;

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Go Game - App");
        Game game = new Game();

        BorderPane root = new BorderPane();

        // Altered by Gerald to add the BoardPane
        final String path = "src/main/resources/pr_se/gogame/";

        // TODO: In the end product, the archive could be chosen by the user (though a default should still be set) and changed at runtime
        BoardPane bp = new BoardPane(game, path+"debug.zip");
        bp.setBackground(new Background(new BackgroundFill(Color.YELLOW, null, null)));

        root.setCenter(bp);
        root.setTop(new HeaderPane(this, stage, game));
        SidePane sp = new SidePane(game);
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
