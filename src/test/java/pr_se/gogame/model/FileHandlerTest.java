package pr_se.gogame.model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pr_se.gogame.model.file.FileHandler;
import pr_se.gogame.model.ruleset.JapaneseRuleset;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

import static pr_se.gogame.model.StoneColor.*;

public class FileHandlerTest {
    Game game;

    @BeforeEach
    void setUp() {
        game = new Game();
        game.newGame(BLACK, 19, 0, new JapaneseRuleset());
    }

    @Test
    void onlyHandicap() {
        game.newGame(BLACK, 19, 9, new JapaneseRuleset());
        game.saveGame(new File("onlyHandicap.sgf"));

        game.loadGame(new File("onlyHandicap.sgf"));

    }

}
