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

    private static int nextSerialNo = 0;
    public final int serialNo;

    /**
     * Creates a StoneGroupPointer pointing to the supplied StoneGroup
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
    public StoneGroup getStoneGroup() {
        return stoneGroup;
    }

    public void setStoneGroup(StoneGroup stoneGroup) {
        if(stoneGroup == null) {
            throw new NullPointerException();
        }
        this.stoneGroup = stoneGroup;
    }

    public static void resetDebug() {
        nextSerialNo = 0;
    }
}
