package pr_se.gogame.model.file;

/**
 * Exception to be thrown if unsupported SGF features are parsed
 */
public class LoadingGameException extends Exception {
    /**
     * The ScannedToken that caused this exception
     */
    private final ScannedToken token;

    /**
     * Creates a new LoadingGameException
     * @param message Message for the exception
     * @param token The ScannedToken that caused this exception
     */
    public LoadingGameException(String message, ScannedToken token) {
        super(message);
        this.token = token;
    }

    /**
     * Returns the ScannedToken that caused this exception
     * @return the ScannedToken that caused this exception
     */
    public ScannedToken getToken() {
        return token;
    }
}
