package pr_se.gogame.model.file;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pr_se.gogame.model.Game;
import pr_se.gogame.model.History;
import pr_se.gogame.model.ruleset.JapaneseRuleset;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.MissingFormatArgumentException;

import static org.junit.jupiter.api.Assertions.*;
import static pr_se.gogame.model.helper.StoneColor.BLACK;

import static pr_se.gogame.model.file.SGFToken.*;

class SGFScannerTest {
    SGFScanner scanner;

    // Argument-Checking
    @Test
    void sgfScanner() {
        assertThrows(NullPointerException.class, () -> new SGFScanner(null));
    }

    @Test
    void next() {
        scanner = new SGFScanner(new StringReader("(;FF[4]"));
        try {
            assertEquals(LPAR, scanner.next().getToken());
            assertEquals(SEMICOLON, scanner.next().getToken());
            ScannedToken t = scanner.next();
            assertEquals(FF, t.getToken());
            assertEquals("4", t.getAttributeValue());
        } catch(IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    void scanAllSGFTokens() {
        StringBuilder sb = new StringBuilder();
        Arrays.stream(values()).forEach(t -> {
            String output = t.getValue();
            if(output.endsWith("]")) {
                try {
                    output = String.format(output, "foo42bar");
                } catch (MissingFormatArgumentException e) {
                    System.err.println("Error outputting token " + t.getValue());
                }
            }
            sb.append(output);
        });

        System.out.println("Output: " + sb);

        scanner = new SGFScanner(new StringReader(sb.toString()));

        try {
            for (SGFToken t : SGFToken.values()) {
                ScannedToken st = scanner.next();
                assertEquals(t, st.getToken());
                assertEquals("foo42bar", st.getAttributeValue());
            }
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }
}