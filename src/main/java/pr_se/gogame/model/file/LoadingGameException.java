package pr_se.gogame.model.file;

/**
 * Exception to be thrown if unsupported SGF features are parsed
 */
public class LoadingGameException extends Exception {
    /**
     * The ScannedToken that caused this exception
     */
    private final ScannedToken t;

    /**
     * Creates a new LoadingGameException
     * @param message Message for the exception
     * @param t The ScannedToken that caused this exception
     */
    public LoadingGameException(String message, ScannedToken t) {
        super(message);
        this.t = t;
    }

    /**
     * Returns the ScannedToken that caused this exception
     * @return
     */
    public ScannedToken getT() {
        return t;
    }
}
