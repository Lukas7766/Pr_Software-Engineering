package pr_se.gogame.model.ruleset;

import pr_se.gogame.model.Game;
import pr_se.gogame.model.helper.StoneColor;
import pr_se.gogame.model.helper.UndoableCommand;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Container for Game score data
 */
public class GameResult {

    /**
     * Description of what happened for each player
     */
    private final Map<StoneColor, String> description;

    /**
     * The winner of the game
     */
    private StoneColor winner;

    /**
     * Caller-definable components of the score
     */
    private final Map<StoneColor, Map<PointType, Number>> scoreComponents;

    /**
     * Instantiates a new, empty GameResult.
     */
    public GameResult() {
        this.scoreComponents = new EnumMap<>(StoneColor.class);
        this.description = new EnumMap<>(StoneColor.class);
        for(StoneColor c : StoneColor.values()) {
            scoreComponents.put(c, new LinkedHashMap<>());
            description.put(c, "");
        }
    }

    /**
     * Adds a caller-definable component to the GameResult
     * @param c The StoneColor to whom this component applies
     * @param type The PointType of this score component
     * @param value The numeric score value of this score component
     * @return An UndoableCommand to undo this method's effects
     */
    public UndoableCommand addScoreComponent(final StoneColor c, final PointType type, final Number value) {
        if(c == null || type == null || value == null) {
            throw new NullPointerException();
        }

        if(type == PointType.HANDICAP && (value.intValue() < Game.MIN_HANDICAP_AMOUNT || value.intValue() > Game.MAX_HANDICAP_AMOUNT)) {
            throw new IllegalStateException("Handicap value out of bounds!");
        }

        final Number oldValue = scoreComponents.get(c).get(type);

        UndoableCommand ret = new UndoableCommand() {
            @Override
            public void execute() {
                scoreComponents.get(c).put(type, value);
            }

            @Override
            public void undo() {
                if(oldValue != null) {
                    scoreComponents.get(c).put(type, oldValue);
                } else {
                    scoreComponents.get(c).remove(type);
                }
            }
        };
        ret.execute();
        return ret;
    }

    /**
     * Returns the total score of the supplied StoneColor
     * @param color the player color whose score is to be returned
     * @return the total score of the supplied StoneColor
     */
    public double getScore(StoneColor color) {
        if(color == null) {
            throw new NullPointerException();
        }

        return scoreComponents.get(color).values().stream().mapToDouble(Number::doubleValue).sum();
    }

    /**
     * Returns a list of the score components for the supplied StoneColor
     * @param c The StoneColor whose score components are queried
     * @return a list of the score components for the supplied StoneColor
     */
    public String getDescription(StoneColor c) {
        if(c == null) {
            throw new NullPointerException();
        }

        StringBuilder sb = new StringBuilder(description.get(c));
        Iterator<Map.Entry<PointType, Number>> iter = scoreComponents.get(c).entrySet().iterator();
        Map.Entry<PointType, Number> cur;
        if(iter.hasNext()) {
            cur = iter.next();
            sb.append("\n\n").append(cur.getKey()).append(": ").append(cur.getValue());
        }
        while(iter.hasNext()) {
            cur = iter.next();
            sb.append("\n+ ").append(cur.getKey()).append(": ").append(cur.getValue());
        }
        sb.append("\n\n= ").append(getScore(c)).append(" points");
        return sb.toString();
    }

    /**
     * Sets the description of what happened for the supplied StoneColor
     * @param c The StoneColor whose description is to be set
     * @param description The description of what happened to the supplied StoneColor
     * @return an UndoableCommand to revert to the old description
     */
    public UndoableCommand setDescription(final StoneColor c, final String description) {
        final String oldDescription = this.description.get(c);
        UndoableCommand ret = new UndoableCommand() {
            @Override
            public void execute() {
                GameResult.this.description.put(c, description);
            }

            @Override
            public void undo() {
                GameResult.this.description.put(c, oldDescription);
            }
        };
        ret.execute();

        return ret;
    }

    /**
     * Returns the StoneColor of the winner
     * @return the StoneColor of the winner
     */
    public StoneColor getWinner() {
        return winner;
    }

    /**
     * Sets which StoneColor is the winner
     * @param newWinner the winner's StoneColor
     * @return an UndoableCommand to undo this method
     */
    public UndoableCommand setWinner(final StoneColor newWinner) {
        final StoneColor oldWinner = this.winner;
        UndoableCommand ret = UndoableCommand.updateValue(c -> winner = c, oldWinner, newWinner);
        ret.execute();

        return ret;
    }

    /**
     * Returns all the score components of this GameResult for the supplied StoneColor
     * @param color The StoneColor whose score components are being queried
     * @return A map of the score components for each PointType
     */
    public Map<PointType, Number> getScoreComponents(StoneColor color) {
        return Map.copyOf(scoreComponents.get(color));
    }

    /**
     * Contains various kinds of point types, including their displayed names
     */
    public enum PointType {
        HANDICAP("Handicap"),
        KOMI("Komi"),
        CAPTURED_STONES("Captured stones"),
        TERRITORY("Territory points"),
        STONES_ON_BOARD("Stones on board");

        /**
         * The displayed description of this PointType
         */
        private final String description;

        /**
         * Constructs a PointType
         * @param description the description of this PointType
         */
        PointType(String description) {
            this.description = description;
        }

        /**
         * Returns a string representation of this PointType
         * @return the displayed description of this PointType
         */
        @Override
        public String toString() {
            return description;
        }
    }
}
