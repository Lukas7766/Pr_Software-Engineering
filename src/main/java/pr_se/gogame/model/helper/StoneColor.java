package pr_se.gogame.model.helper;

public enum StoneColor {
    BLACK("Black"), WHITE("White");

    private final String name;

    StoneColor(String name) {
        this.name = name;
    }

    public static StoneColor getOpposite(StoneColor color) {
        if(color == null) {
            throw new NullPointerException();
        }

        if(color == BLACK) {
            return WHITE;
        }

        return BLACK;
    }

    @Override
    public String toString() {
        return name;
    }
}

