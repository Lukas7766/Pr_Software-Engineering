package pr_se.gogame.view_controller;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * This subclass of ImageView - unlike said superclass - can be automatically resized by its parents like other nodes
 * can.
 * <p>
 * Inspired by <a href="https://www.reddit.com/r/JavaFX/comments/l2yzwg/resize_imageview_to_fit_parent_pane/">...</a>
 */
public class ResizableImageView extends ImageView {
    /**
     * Creates a new ResizableImageView
     */
    public ResizableImageView() {
        super();
    }

    /**
     * Creates a new ResizableImageView
     * @param s URL of the image
     */
    public ResizableImageView(String s) {
        super(s);
    }

    /**
     * Creates a new ResizableImageView
     * @param image Image to be used
     */
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
