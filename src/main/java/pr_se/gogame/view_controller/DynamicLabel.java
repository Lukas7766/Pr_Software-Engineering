package pr_se.gogame.view_controller;

import javafx.scene.Node;
import javafx.scene.control.Label;

public class DynamicLabel extends Label {

    public DynamicLabel() {
        super();
    }

    public DynamicLabel(String s) {
        super(s);
    }

    public DynamicLabel(String s, Node node) {
        super(s, node);
    }

    @Override
    public void resize(double width, double height) {
        super.resize(width, height);

        final double FONT_SIZE = Math.min(getWidth() / getText().length(), getHeight());
        setStyle("-fx-font-size: " + FONT_SIZE);
    }
}
