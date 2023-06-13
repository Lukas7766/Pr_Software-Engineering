package pr_se.gogame.model;

public class GeraldsHistory {

    private final GeraldsNode head;

    private GeraldsNode current;

    private int counter = 0;

    public GeraldsHistory() {
        head = new GeraldsNode(null, null);
        current = head;
    }

    public void rewind() {
        while(stepBack());
    }

    public void skipToEnd() {
        while(stepForward());
    }

    public boolean stepBack() {
        if(current.getPrev() != null) {
            System.out.println("Undoing " + current.getComment());
            current.getCommand().undo();
            current = current.getPrev();

            return true;
        }

        return false;
    }

    public boolean stepForward() {
        if(current.getNext() != null) {
            current = current.getNext();
            current.getCommand().execute(false);
            System.out.println("Re-Doing " + current.getComment());

            return true;
        }

        return false;
    }

    public void addNode(GeraldsNode addedNode) {
        current.setNext(addedNode);
        current = current.getNext();
        System.out.println("Added node no. " + counter);
        counter++;
    }
}
