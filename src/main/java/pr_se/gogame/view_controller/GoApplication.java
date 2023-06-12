package pr_se.gogame.view_controller;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import pr_se.gogame.model.Game;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GoApplication extends Application {

    private static final int WIDTH = 760;
    private static final int HEIGHT = 580;

    @Override
    public void start(Stage stage) throws Exception {
        //Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
        //    CustomExceptionDialog.show(e);
        //});
        CustomExceptionDialog.stage = stage;
        // Generate the necessary folder and extract the default the graphics pack if it is not present.
        String graphicsDir = "./Grafiksets";
        String graphicsPack = "/default.zip";
        Path path = Paths.get(graphicsDir + graphicsPack);
        if(Files.notExists(path, LinkOption.NOFOLLOW_LINKS)) {
            System.out.println("The default graphics pack doesn't exist.");

            InputStream link = this.getClass().getResourceAsStream(graphicsPack);
            if(link == null) {
                throw new NullPointerException("Default graphics pack is not in JAR file!");
            }

            File dir = new File(graphicsDir);
            dir.mkdirs();

            File file = new File(graphicsDir + graphicsPack);
            Files.copy(link, file.getAbsoluteFile().toPath());

            link.close();
        } else if(!Files.exists(path)) {
            throw new IllegalStateException("Java could not determine whether the path exists!");
        }

        // Set up the actual program
        stage.setTitle("Go Game - App");
        Game game = new Game();

        BorderPane root = new BorderPane();

        InputStream iconStream = this.getClass().getResourceAsStream("/go.png");
        if(iconStream != null) {
            stage.getIcons().add(new Image(iconStream));
            iconStream.close();
        }



        Scene scene = new Scene(root, WIDTH, HEIGHT);
        stage.setMinHeight(HEIGHT + 40);
        stage.setMinWidth(WIDTH + 20);

        stage.setScene(scene);

        root.setCenter(bp);
        HeaderPane hp = new HeaderPane(Color.LIGHTGRAY, this, scene, stage, game);
        root.setTop(hp);
        SidePane sp = new SidePane(Color.LIGHTGRAY, stage, game);
        root.setLeft(sp);

        /*
         * If this is active, dragging onto the playable area of the board is possible from anywhere within the window,
         * except, for some reason, the menu bar. This might be desirable.
         */
        scene.setOnDragDetected((e) -> scene.startFullDrag());

        /*
         * This is necessary for keeping the rows and columns of the board together if Windows's DPI Scaling is set
         * above 100 %.
         */
        stage.setForceIntegerRenderScale(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
