package pr_se.gogame.model;

import javafx.scene.paint.Color;

public class GameResult {
    private final double scoreBlack;
    private final double scoreWhite;
    private final String GameResult;

    private final StoneColor winner;
    public GameResult(double scoreBlack, double scoreWhite, StoneColor winner, String GameResult) {
        this.scoreBlack = scoreBlack;
        this.scoreWhite = scoreWhite;
        this.GameResult = GameResult;
        this.winner = winner;
    }
    public double getScoreBlack() {
        return scoreBlack;
    }
    public double getScoreWhite() {
        return scoreWhite;
    }
    public String getGameResult() {
        return GameResult;
    }
    public StoneColor getWinner() {
        return winner;
    }
}
