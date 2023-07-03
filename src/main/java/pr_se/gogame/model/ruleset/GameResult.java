package pr_se.gogame.model.ruleset;

import pr_se.gogame.model.helper.StoneColor;

import java.util.*;

public class GameResult {

    private final Map<StoneColor, String> description;

    private StoneColor winner;

    private final Map<StoneColor, Map<String, Number>> scoreComponents;

    public GameResult() {
        this.scoreComponents = new EnumMap<>(StoneColor.class);
        for(StoneColor c : StoneColor.values()) {
            scoreComponents.put(c, new LinkedHashMap<>());
        }
        this.description = new EnumMap<>(StoneColor.class);
    }

    public void addScoreComponent(StoneColor c, String name, Number value) {
        if(c == null || name == null || value == null) {
            throw new NullPointerException();
        }

        scoreComponents.get(c).put(name, value);
    }

    public double getScore(StoneColor color) {
        if(color == null) {
            throw new NullPointerException();
        }

        return scoreComponents.get(color).values().stream().mapToDouble(Number::doubleValue).sum();
    }

    public String getDescription(StoneColor c) {
        StringBuilder sb = new StringBuilder(description.get(c));
        Iterator<Map.Entry<String, Number>> iter = scoreComponents.get(c).entrySet().iterator();
        Map.Entry<String, Number> cur;
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

    public void setDescription(StoneColor c, String description) {
        this.description.put(c, description);
    }

    public StoneColor getWinner() {
        return winner;
    }

    public void setWinner(StoneColor winner) {
        this.winner = winner;
    }
}
