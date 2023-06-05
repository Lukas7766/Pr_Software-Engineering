package pr_se.gogame.view_controller;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.control.Button;

import javafx.stage.Screen;
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


        final String iconPath = "file:src/main/resources/pr_se/gogame/";
        
        stage.getIcons().add(new Image(iconPath+"go.png"));
        BoardPane bp = new BoardPane(game);

        root.setCenter(bp);
        HeaderPane hp = new HeaderPane(Color.LIGHTGRAY, this, stage, game);
        root.setTop(hp);
        SidePane sp = new SidePane(Color.LIGHTGRAY, stage, game);
        root.setLeft(sp);
        //root.setRight(debugButtons);

        Scene scene = new Scene(root, WIDTH, HEIGHT);
        stage.setMinHeight(HEIGHT + 40);
        stage.setMinWidth(WIDTH + 20);

        stage.setScene(scene);

        Screen screen = Screen.getPrimary();
        System.out.println("DPI = " + screen.getDpi());
        System.out.println("Output scale X = " + screen.getOutputScaleX());
        System.out.println("Output scale Y = " + screen.getOutputScaleY());

        /*
         * This is necessary for keeping the rows and columns of the board together if Windows's DPI Scaling is set
         * above 100 %.
         */
        stage.setForceIntegerRenderScale(true);
        stage.show();

        /*
         * If this is active, dragging onto the playable area of the board is possible from anywhere within the window,
         * except, for some reason, the menu bar. This might be desirable.
         */
        scene.setOnDragDetected((e) -> {
            scene.startFullDrag();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
