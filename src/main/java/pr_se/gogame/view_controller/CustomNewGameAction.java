package pr_se.gogame.view_controller;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import pr_se.gogame.model.Game;

import java.io.File;
import java.util.HashSet;
import java.util.Optional;

public class CustomNewGameAction {

    /**
     * Handles the on new game action<br>
     * -> save <br>
     * -> no save <br>
     * -> cancel <br>
     *
     * @param stage pass stage
     * @param game pass game
     * @param filterList pass list of Extension Filters
     */
    public static void onSaveAction(Stage stage, Game game, HashSet<FileChooser.ExtensionFilter> filterList) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

        alert.setTitle("Go Game - Save Game");
        alert.setHeaderText("Do you want to save your game before starting a new one?");
        alert.setContentText("Choose your option:");
        alert.initOwner(stage);

        ButtonType noBtn = new ButtonType("no");
        ButtonType saveBtn = new ButtonType("save");
        ButtonType cancelBtn = new ButtonType("cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(saveBtn, noBtn, cancelBtn);

        Optional<ButtonType> btnResult = alert.showAndWait();

        btnResult.ifPresent(er -> {
            switch (er.getText()){
                case "no" -> game.initGame();
                case "save" -> {
                    File f;
                    if(game.getSaveFile() == null){
                        f = CustomFileDialog.getFile(stage,true, filterList);
                        if(f != null) game.setSaveFile(f);
                    }

                    if (!game.saveGame()) {
                        System.out.println("Export did not work!");
                        Alert info = new Alert(Alert.AlertType.INFORMATION);
                        info.setTitle("Go Game - Save Game Info");
                        info.setHeaderText("Game was not saved! Try it again.");
                        info.initOwner(stage);
                        info.showAndWait();
                    }
                }
            }
        });
    }

}
