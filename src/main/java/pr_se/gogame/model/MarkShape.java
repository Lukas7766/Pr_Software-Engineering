package pr_se.gogame.model;

import pr_se.gogame.model.file.SGFToken;

public enum MarkShape {
    CIRCLE(SGFToken.CR),
    SQUARE(SGFToken.SQ),
    TRIANGLE(SGFToken.TR);

    private final SGFToken sgfToken;

    MarkShape(SGFToken sgfToken) {
        this.sgfToken = sgfToken;
    }

    public SGFToken getSgfToken() {
        return sgfToken;
    }
}
