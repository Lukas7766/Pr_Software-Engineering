package pr_se.gogame.model.ruleset;

public enum PointType {
    HANDICAP("Handicap"),
    KOMI("Komi"),
    CAPTURED_STONES("Captured stones"),
    TERRITORY("Territory points"),
    STONES_ON_BOARD("Stones on board");

    private final String description;

    PointType(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return description;
    }
}
