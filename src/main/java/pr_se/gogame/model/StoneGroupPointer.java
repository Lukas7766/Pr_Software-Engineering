package pr_se.gogame.model;

/**
 * Model
 * Indirection layer for StoneGroups in Board; allows for merging Stone Groups with lower time complexity (as only
 * the pointers have to be redirected)
 */
public class StoneGroupPointer {
    /**
     * the StoneGroup that this points to
     */
    private StoneGroup stoneGroup;

    /**
     * Serial No. of the next instance of this class (used for debugging)
     */
    private static int nextSerialNo = 0;

    /**
     * Serial No. of this instance of the StoneGroupPointer class (used for debugging)
     */
    public final int serialNo;

    /**
     * Creates a StoneGroupPointer pointing to the supplied StoneGroup. Adds itself to said StoneGroups list of
     *  StoneGroupPointers
     * @param stoneGroup the StoneGroup this points to
     */
    public StoneGroupPointer(StoneGroup stoneGroup) {
        if(stoneGroup == null) {
            throw new NullPointerException();
        }
        setStoneGroup(stoneGroup);
        this.stoneGroup.addPointer(this);

        serialNo = nextSerialNo;
        nextSerialNo++;
    }

    // Getters and Setters

    /**
     * @return The StoneGroup that this StoneGroupPointer points to
     */
    public StoneGroup getStoneGroup() {
        return stoneGroup;
    }

    /**
     * Sets the StoneGroup that this StoneGroupPointer points to
     * @param stoneGroup the new StoneGroup that this StoneGroupPointer should point to
     */
    public void setStoneGroup(StoneGroup stoneGroup) {
        if(stoneGroup == null) {
            throw new NullPointerException();
        }
        this.stoneGroup = stoneGroup;
    }

    /**
     * Used for resetting debug variables
     */
    public static void resetDebug() {
        nextSerialNo = 0;
    }
}
