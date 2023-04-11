package pr_se.gogame.view_controller;

import javafx.scene.layout.BorderPane;

public class OrbitalBorderPane extends BorderPane {
    @Override
    protected void layoutChildren() {
        super.layoutChildren();

        // if(getHeight() - getCenter().getLayoutBounds().getHeight() < getTop().getLayoutBounds().getHeight() + getBottom().getLayoutBounds().getHeight())

        getTop().resizeRelocate(
                getCenter().getLayoutX(),
                getCenter().getLayoutY() - getTop().getLayoutBounds().getHeight(),
                getCenter().getLayoutBounds().getWidth(),
                getTop().getLayoutBounds().getHeight());

        getBottom().resizeRelocate(
                getCenter().getLayoutX(),
                getCenter().getBoundsInParent().getMaxY(),
                getCenter().getLayoutBounds().getWidth(),
                getBottom().getLayoutBounds().getHeight());

        getLeft().resizeRelocate(
                getCenter().getLayoutX() - getLeft().getLayoutBounds().getWidth(),
                getCenter().getLayoutY(),
                getLeft().getLayoutBounds().getWidth(),
                getCenter().getLayoutBounds().getHeight());

        getRight().resizeRelocate(
                getCenter().getBoundsInParent().getMaxX(),
                getCenter().getLayoutY(),
                getRight().getLayoutBounds().getWidth(),
                getCenter().getLayoutBounds().getHeight());
    }
}
