package pr_se.gogame.model;

import org.junit.jupiter.api.BeforeEach;
import pr_se.gogame.model.file.SGFScanner;
import pr_se.gogame.model.ruleset.JapaneseRuleset;

import java.io.File;
import java.io.FileReader;

import static pr_se.gogame.model.helper.StoneColor.BLACK;

public class SGFScannerTest {

    Game game;

    File file;

    History oldHistory;

    int size = 19;

    int handicap = 0;

    SGFScanner scanner;

    @BeforeEach
    void setUp() {
        game = new Game();
        game.newGame(BLACK, size, handicap, new JapaneseRuleset());
        oldHistory = game.getHistory();
        file = new File("tmp.sgf");
        new FileReader(file);
        scanner = new SGFScanner();
    }

    // Argument-Checking

}
