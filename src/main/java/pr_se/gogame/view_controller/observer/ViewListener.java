package pr_se.gogame.view_controller.observer;

/**
 * This listener is used for communication between the GlobalSettings and the view components. It is separate because
 *  some updates do not pertain to the model at all, only changing aspects of the view. View components can then check
 *  all GlobalSettings and update themselves accordingly.
 */
public interface ViewListener {
    /**
     * Called when any setting is updated.
     */
    void onSettingsUpdated();

    /**
     * Called when a move is confirmed.
     */
    void onMoveConfirmed();
}
