package pr_se.gogame.model.file;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.MissingFormatArgumentException;
import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.*;
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
        Arrays.stream(values()).filter(t -> t != EOF).forEach(t -> {
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
                if(t.getValue().endsWith("]")) {
                    assertEquals("foo42bar", st.getAttributeValue());
                } else {
                    assertEquals("", st.getAttributeValue());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    void skipWhiteSpace() {
        scanner = new SGFScanner(new StringReader("      (     ;    FF[4]\n \n    GM[1] \r\rSZ[19] \r\nHA[0]"));
        try {
            assertEquals(LPAR, scanner.next().getToken());
            assertEquals(SEMICOLON, scanner.next().getToken());
            assertEquals(FF, scanner.next().getToken());
            assertEquals(GM, scanner.next().getToken());
            assertEquals(SZ, scanner.next().getToken());
            assertEquals(HA, scanner.next().getToken());
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    void readTilEOF() {
        scanner = new SGFScanner(new StringReader("SZ[9]"));
        try {
            assertEquals(SZ, scanner.next().getToken());
            assertEquals(EOF, scanner.next().getToken());
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    void reformatAttribute() {
        scanner = new SGFScanner(new StringReader("C[\tThis\\\n is \\\\a \\]test \\:o\\f the\n reformatting\r.]"));
        try {
            assertEquals(" This is \\a ]test :of the\n reformatting.", scanner.next().getAttributeValue());
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    void invalidFirstCharacter() {
        // I'm doing it like this to future-proof this test in case any new tokens are ever added.
        String upperCaseChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        List<Character> firstTokenChars = Arrays.stream(SGFToken.values()).map(t -> t.getValue().charAt(0)).toList();
        OptionalInt invalidChar = upperCaseChars.chars().filter(c -> !firstTokenChars.contains((char)c)).findAny();
        if(invalidChar.isEmpty()) {
            fail("Test needs to be reworked, as all uppercase characters are used to start SGF tokens");
        } else {
            scanner = new SGFScanner(new StringReader("" + invalidChar.getAsInt()));
            assertThrows(IOException.class, () -> scanner.next());
        }
    }

    @Test
    void invalidSecondCharacter() {
        invalidInput("FZ");
        invalidInput("AZ");
        invalidInput("PZ");
        invalidInput("SY");
    }

    @Test
    void fileEndInMiddleOfInput() {
        invalidInput("C[I just fell asleep on the keyboa  ");
        invalidInput("C[Me too");
    }

    void invalidInput(String input) {
        scanner = new SGFScanner(new StringReader(input));
        assertThrows(IOException.class, () -> scanner.next());
    }
}