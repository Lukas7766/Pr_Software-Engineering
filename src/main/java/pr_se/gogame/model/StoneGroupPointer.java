package pr_se.gogame.model;

public class StoneGroupPointer {
    private StoneGroup stoneGroup;

    // TODO: Remove these debug variables
    private static int nextSerialNo = 0;
    public final int serialNo;

    public StoneGroupPointer(StoneGroup stoneGroup) {
        if(stoneGroup == null) { // Checking here as well to make stack-traces shorter.
            throw new NullPointerException();
        }
        setStoneGroup(stoneGroup);
        this.stoneGroup.addPointer(this);
        serialNo = nextSerialNo;
        nextSerialNo++;
    }

    public StoneGroup getStoneGroup() {
        return stoneGroup;
    }

    public void setStoneGroup(StoneGroup stoneGroup) {
        if(stoneGroup == null) {
            throw new NullPointerException();
        }
        this.stoneGroup = stoneGroup;
    }
}
