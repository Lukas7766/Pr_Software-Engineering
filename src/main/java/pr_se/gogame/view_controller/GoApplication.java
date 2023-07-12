package pr_se.gogame.view_controller;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import pr_se.gogame.model.Game;
import pr_se.gogame.model.GameInterface;
import pr_se.gogame.view_controller.dialog.CustomCloseAction;
import pr_se.gogame.view_controller.dialog.CustomExceptionDialog;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Sets up the GUI and instantiates the model
 */
public class GoApplication extends Application {

    private static final int WIDTH = 760;
    private static final int HEIGHT = 580;

    @Override
    public void start(Stage stage) throws Exception {
        CustomExceptionDialog.setStage(stage);

        // Generate the necessary folder and extract the default the graphics pack if it is not present.
        String graphicsDir = GlobalSettings.GRAPHICS_PACK_FOLDER;
        String graphicsPack = "/" + GlobalSettings.getGraphicsPackFileName();
        Path path = Paths.get(GlobalSettings.getGraphicsPath());
        if(Files.notExists(path, LinkOption.NOFOLLOW_LINKS)) {
            InputStream link = this.getClass().getResourceAsStream(graphicsPack);
            if(link == null) {
                throw new NullPointerException("Default graphics pack is not in JAR file!");
            }

            File dir = new File(graphicsDir);
            if(!dir.mkdirs()) {
                throw new IOException("Couldn't create graphics pack folder!");
            }

            File file = new File(graphicsDir + graphicsPack);
            Files.copy(link, file.getAbsoluteFile().toPath());

            link.close();
        } else if(!Files.exists(path)) {
            throw new IllegalStateException("Java could not determine whether the path exists!");
        }

        // Set up the actual program
        stage.setTitle("Go Game");
        GameInterface game = new Game();

        BorderPane root = new BorderPane();

        InputStream iconStream = this.getClass().getResourceAsStream("/go.png");
        if(iconStream != null) {
            stage.getIcons().add(new Image(iconStream));
            iconStream.close();
        }



        Scene scene = new Scene(root, WIDTH, HEIGHT);
        stage.setMinHeight(HEIGHT + 40.0);
        stage.setMinWidth(WIDTH + 20.0);

        stage.setScene(scene);
        BoardPane bp = new BoardPane(game);
        root.setCenter(bp);
        HeaderPane hp = new HeaderPane(Color.LIGHTGRAY, this, scene, stage, game);
        root.setTop(hp);
        SidePane sp = new SidePane(Color.LIGHTGRAY, stage, game);
        root.setLeft(sp);

        stage.setOnCloseRequest(e -> CustomCloseAction.onCloseAction(stage, game, e));

        /*
         * If this is active, dragging onto the playable area of the board is possible from anywhere within the window,
         * except, for some reason, the menu bar. This might be desirable.
         */
        scene.setOnDragDetected(e -> scene.startFullDrag());

        /*
         * This is necessary for keeping the rows and columns of the board together if Windows's DPI Scaling is set
         * above 100 %.
         */
        stage.setForceIntegerRenderScale(true);

        game.initGame();
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
