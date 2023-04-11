package pr_se.gogame.view_controller;

import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import pr_se.gogame.model.Board;

public class BoardPane2 extends GridPane {

    private final int SIZE;

    private final Board board;

    private final Image[] tiles = new Image [2];
    private final Image[] stones = new Image [2];

    public BoardPane2(Board board, String tile0, String tile1, String stone0, String stone1) {
        this.board = board;
        this.SIZE = board.getSize();

        // TODO: In the end product, the files would be chosen by the user (and perhaps packaged in an archive)
        final int DEFAULT_IMAGE_SIZE = 128;
        final boolean SMOOTH_IMAGES = false;

        tiles[0] = new Image(
                tile0,              // URL
                DEFAULT_IMAGE_SIZE, // requestedWidth
                DEFAULT_IMAGE_SIZE, // requestedHeight
                true,               // preserveRation
                SMOOTH_IMAGES,      // smooth
                true);              // backgroundLoading
        tiles[1] = new Image(tile1, DEFAULT_IMAGE_SIZE, DEFAULT_IMAGE_SIZE, true, SMOOTH_IMAGES, true);

        stones[0] = new Image(
                stone0,     // URL
                true);      // backgroundLoading
        stones[1] = new Image(stone1, true);

        setAlignment(Pos.CENTER);

        // Fill the grid with alternating tiles
        for(int i = 0; i < this.SIZE; i++) {
            for(int j = 0; j < this.SIZE; j++) {
                BoardPane2.BoardCell bc = new BoardPane2.BoardCell(tiles[(j % 2 + i % 2) % 2]);
                add(bc, j, i);
            }
        }
    }

    private class BoardCell extends StackPane {
        private final ImageView TILE;
        private final ImageView BLACK_HOVER;
        private final ImageView WHITE_HOVER;
        private final ImageView BLACK_STONE;
        private final ImageView WHITE_STONE;
        // private final Label LABEL;

        private boolean isSelected = false;
        private boolean isSet = false;

        private BoardCell(Image tile) {
            this.setMinSize(0, 0);

            BackgroundSize bgSz = new BackgroundSize(
                    100,     // width
                    100,        // height
                    true,       // widthAsPercentage
                    true,       // heightAsPercentage
                    false,      // contain
                    true);      // cover
            BackgroundImage bgImg = new BackgroundImage(tile, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, bgSz);
            this.setBackground(new Background(bgImg));
            this.TILE = null;

            this.BLACK_HOVER = getCellImageView(stones[0]);
            getChildren().add(this.BLACK_HOVER);

            this.WHITE_HOVER = getCellImageView(stones[1]);
            getChildren().add(this.WHITE_HOVER);

            this.BLACK_STONE = getCellImageView(stones[0]);
            this.BLACK_STONE.setVisible(true);
            getChildren().add(this.BLACK_STONE);

            this.WHITE_STONE = getCellImageView(stones[1]);
            getChildren().add(this.WHITE_STONE);

            /*this.LABEL = new Label("0");
            this.LABEL.setVisible(false);
            setMargin(this.LABEL, new Insets(0, 0, 0, 0));
            this.LABEL.setMinSize(0, 0);

            final DoubleProperty FONT_SIZE = new SimpleDoubleProperty(0);
            FONT_SIZE.bind(BLACK_STONE.fitWidthProperty().divide(2).subtract(Bindings.length(this.LABEL.textProperty())));
            this.LABEL.styleProperty().bind(Bindings.concat("-fx-font-size: ", FONT_SIZE));

            getChildren().add(this.LABEL);*/
        }

        private ImageView getCellImageView(Image i) {
            if(i == null) {
                throw new NullPointerException();
            }

            ImageView iv = new ImageView(i) {
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
            };
            iv.setPreserveRatio(true);
            iv.setMouseTransparent(true);
            iv.setSmooth(false);
            iv.setVisible(false);

            return iv;
        }

        /*public void hoverWhite() {
            hover(WHITE_HOVER);
        }

        public void hoverBlack() {
            hover(BLACK_HOVER);
        }

        public void unhover() {
            if(!isSelected) {
                BLACK_HOVER.setVisible(false);
                WHITE_HOVER.setVisible(false);
            }
        }

        private void hover(ImageView iv) {
            unhover();              // might be unnecessary
            if(!isSet && !isSelected) {
                iv.setOpacity(0.5);
                iv.setVisible(true);
            }
        }

        public void selectWhite() {
            select(WHITE_HOVER);
        }

        public void selectBlack() {
            select(BLACK_HOVER);
        }

        public void deselect() {
            isSelected = false;
            unhover();
        }

        private void select(ImageView iv) {
            deselect();             // might be unnecessary
            iv.setOpacity(0.75);
            iv.setVisible(true);
            isSelected = true;
        }

        private void setWhite() {
            set(WHITE_STONE);
        }

        private void setBlack() {
            set(BLACK_STONE);
        }

        public void unset() {
            deselect();
            BLACK_STONE.setVisible(false);
            WHITE_STONE.setVisible(false);
            LABEL.setVisible(false);
            isSet = false;
        }

        private void set(ImageView iv) {
            deselect();
            iv.setVisible(true);
            if(showsMoveNumbers) {
                PixelReader p = iv.getImage().getPixelReader();
                if(p == null) {
                    throw new NullPointerException("Can't get stone color");
                }
                this.LABEL.setTextFill(p.getColor((int)(iv.getImage().getWidth() / 2), (int)(iv.getImage().getHeight() / 2)).invert());
                this.LABEL.setVisible(true);
            }
            isSet = true;
        }*/

        // Getters

        public ImageView getTile() {
            return TILE;
        }

        public ImageView getBlackHover() {
            return BLACK_HOVER;
        }

        public ImageView getWhiteHover() {
            return WHITE_HOVER;
        }

        public ImageView getBlackStone() {
            return BLACK_STONE;
        }

        public ImageView getWhiteStone() {
            return WHITE_STONE;
        }

        /*public Label getLabel() {
            return LABEL;
        }*/
    } // private class BoardCell
}
