package pr_se.gogame.view_controller.observer;

/**
 * This listener can be registered with the Game in the model.
 */
public interface GameListener {

    /** The listener is used to update the view based on the passed GameEvent.
     *
     * @param e GameEvent containing info for the view's update
     */
    void gameCommand(GameEvent e);
}
