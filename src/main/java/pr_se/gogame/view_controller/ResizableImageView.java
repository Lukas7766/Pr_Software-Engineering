package pr_se.gogame.view_controller;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Inspired by https://www.reddit.com/r/JavaFX/comments/l2yzwg/resize_imageview_to_fit_parent_pane/
 */
public class ResizableImageView extends ImageView {

    public ResizableImageView() {
        super();
    }

    public ResizableImageView(String s) {
        super(s);
    }

    public ResizableImageView(Image image) {
        super(image);
    }

    @Override
    public boolean isResizable() {
        return true;
    }

    @Override
    public void resize(double width, double height) {
        super.resize(width, height);
        setFitWidth(width);
        setFitHeight(height);
    }

    @Override
    public double maxWidth(double height) {
        return Double.MAX_VALUE;
    }

    @Override
    public double maxHeight(double width) {
        return Double.MAX_VALUE;
    }

    @Override
    public double minWidth(double height) {
        return 0.0;
    }

    @Override
    public double minHeight(double width) {
        return 0.0;
    }
}
