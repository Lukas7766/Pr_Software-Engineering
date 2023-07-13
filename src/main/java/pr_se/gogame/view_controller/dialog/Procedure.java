package pr_se.gogame.view_controller.dialog;

/**
 * Custom functional interface that takes no parameters and returns nothing. Such interfaces exist (e.g., Runnable),
 *  but these could imply that multi-threading is used, which is not the case.
 */
@FunctionalInterface
public interface Procedure {
    /**
     * Executes this Procedure's action
     */
    void use();
}
