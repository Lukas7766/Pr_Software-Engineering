package pr_se.gogame.model;

public class StoneGroupPointer {
    private StoneGroup stoneGroup;

    public StoneGroupPointer(StoneGroup stoneGroup) {
        if(stoneGroup == null) { // Checking here as well to make stack-traces shorter.
            throw new NullPointerException();
        }
        setStoneGroup(stoneGroup);
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
