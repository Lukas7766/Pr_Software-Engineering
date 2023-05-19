package pr_se.gogame.model;

public enum StoneColor {
    BLACK, WHITE;

    public static StoneColor getOpposite(StoneColor color) {
        if(color == null) {
            throw new NullPointerException();
        }

        if(color == BLACK) {
            return WHITE;
        }

        return BLACK;
    }
}

