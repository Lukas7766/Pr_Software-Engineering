package pr_se.gogame.view_controller;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
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
        BoardPane bp = new BoardPane(game, path+"default.zip");

        Button changeGFX = new Button("Change graphics set");
        changeGFX.setOnAction((e) -> bp.setGraphicsPath(path + "inverted.zip"));
        Button toggleCoords = new Button("Toggle Coordinates");
        toggleCoords.setOnAction((e) -> bp.setShowsCoordinates(!bp.showsCoordinates()));
        Button toggleMoveNos = new Button("Toggle Move Numbers");
        toggleMoveNos.setOnAction((e) -> bp.setShowsMoveNumbers(!bp.showsMoveNumbers()));
        VBox debugButtons = new VBox();
        debugButtons.getChildren().addAll(changeGFX, toggleCoords, toggleMoveNos);

        root.setCenter(bp);
        root.setTop(new HeaderPane(this, stage, game));
        SidePane sp = new SidePane(game);
        root.setLeft(sp);
        root.setRight(debugButtons);

        Scene scene = new Scene(root, WIDTH, HEIGHT);
        stage.setMinHeight(HEIGHT + 40);
        stage.setMinWidth(WIDTH + 20);

        stage.setScene(scene);
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
        launch();
    }
}
