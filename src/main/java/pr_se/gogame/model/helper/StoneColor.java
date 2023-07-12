package pr_se.gogame.model.helper;

/**
 * Contains the possible colors for both players
 */
public enum StoneColor {
    BLACK("Black"), WHITE("White");

    /**
     * The default name that is to be displayed for each StoneColor.
     */
    private final String name;

    /**
     * Constructs a new StoneColor
     * @param name The default name that is to be displayed for this StoneColor.
     */
    StoneColor(String name) {
        this.name = name;
    }

    /**
     * Determines the opposite of the supplied StoneColor (eliminates many conditionals)
     * @param color The color whose opposite is to be determined
     * @return The opposite of the supplied StoneColor
     */
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

