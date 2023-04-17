package pr_se.gogame.view_controller;

/**
 * This interface represents the listener of the goGame. <br>
 *
 */
public interface GameListener {

    /** The listener is used to update the VIEW based on the passed Game Event.
     *
     * @param e Game Event
     */
    void gameCommand(GameEvent e);
}
