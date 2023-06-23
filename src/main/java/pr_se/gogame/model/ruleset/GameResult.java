package pr_se.gogame.model.ruleset;

import pr_se.gogame.model.StoneColor;

public class GameResult {
    private final double scoreBlack;
    private final double scoreWhite;
    private final String description;

    private final StoneColor winner;
    public GameResult(double scoreBlack, double scoreWhite, StoneColor winner, String description) {
        this.scoreBlack = scoreBlack;
        this.scoreWhite = scoreWhite;
        this.description = description;
        this.winner = winner;
    }
    public double getScoreBlack() {
        return scoreBlack;
    }
    public double getScoreWhite() {
        return scoreWhite;
    }
    public String getDescription() {
        return description;
    }
    public StoneColor getWinner() {
        return winner;
    }
}
