package pr_se.gogame.model.file;

public class LoadingGameException extends Exception {
    private final ScannedToken t;

    public LoadingGameException(String message, ScannedToken t) {
        super(message);
        this.t = t;
    }

    public ScannedToken getT() {
        return t;
    }
}
