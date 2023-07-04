package pr_se.gogame.model.ruleset;

import pr_se.gogame.model.Game;
import pr_se.gogame.model.helper.StoneColor;
import pr_se.gogame.model.helper.UndoableCommand;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class GameResult {

    private final Map<StoneColor, String> description;

    private StoneColor winner;

    private final Map<StoneColor, Map<PointType, Number>> scoreComponents;

    public GameResult() {
        this.scoreComponents = new EnumMap<>(StoneColor.class);
        this.description = new EnumMap<>(StoneColor.class);
        for(StoneColor c : StoneColor.values()) {
            scoreComponents.put(c, new LinkedHashMap<>());
            description.put(c, "");
        }
    }

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
            public void execute(boolean saveEffects) {
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
        ret.execute(true);
        return ret;
    }

    public double getScore(StoneColor color) {
        if(color == null) {
            throw new NullPointerException();
        }

        return scoreComponents.get(color).values().stream().mapToDouble(Number::doubleValue).sum();
    }

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

    public UndoableCommand setDescription(final StoneColor c, final String description) {
        final String oldDescription = this.description.get(c);
        UndoableCommand ret = new UndoableCommand() {
            @Override
            public void execute(boolean saveEffects) {
                GameResult.this.description.put(c, description);
            }

            @Override
            public void undo() {
                GameResult.this.description.put(c, oldDescription);
            }
        };
        ret.execute(true);

        return ret;
    }

    public StoneColor getWinner() {
        return winner;
    }

    public UndoableCommand setWinner(final StoneColor newWinner) {
        final StoneColor oldWinner = this.winner;
        UndoableCommand ret = UndoableCommand.updateValue(c -> winner = c, oldWinner, newWinner);
        ret.execute(true);

        return ret;
    }

    public Map<PointType, Number> getScoreComponents(StoneColor color) {
        return Map.copyOf(scoreComponents.get(color));
    }

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
}
