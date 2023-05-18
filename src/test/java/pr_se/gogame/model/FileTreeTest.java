package pr_se.gogame.model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class FileTreeTest {

    private FileTree tree;

    @BeforeEach
    void setUp() {
        tree = new FileTree(9, "Black", "White");

        tree.addName(StoneColor.WHITE, "White");
        tree.addName(StoneColor.BLACK, "Black");
        tree.addMove(SgfToken.B, 0, 0);
        tree.addMove(SgfToken.W, 0, 1);
        tree.addMove(SgfToken.B, 1, 0);
        tree.addMove(SgfToken.W, 1, 1);
    }

    @AfterEach
    void tearDown() {
        tree = null;
    }

    @Test
    void testPrint() {
        tree.printGameTree();
    }

    @Test
    void getStart() {
        assertEquals("(;FF[4]GM[1]SZ[9]", tree.getStart().toString());
    }

    @Test
    void addNode() {
        tree.addMove(SgfToken.AE, 1, 1);
        assertEquals(tree.getCurrent().getToken(), "AE[bh]");
    }

    @Test
    void viewing() {
        Node testView = tree.viewCurrent();
        assertEquals(";W[bh]", testView.getToken());
        testView = tree.viewPrev();
        testView = tree.viewPrev();
        assertEquals(";W[ah]", testView.getToken());
        testView = tree.viewNext();
        assertEquals(";B[bi]", testView.getToken());
    }

    @Test
    void handicapStoneSet() {
        ArrayList<String> white = new ArrayList<>();
        white.add(FileTree.calculateCoordinate(9, 0, 0));
        white.add(FileTree.calculateCoordinate(9, 0, 1));
        ArrayList<String> black = new ArrayList<>();
        black.add(FileTree.calculateCoordinate(9, 1, 0));
        black.add(FileTree.calculateCoordinate(9, 1, 1));

        tree.addStonesBeforeGame(white,black);
        assertEquals(";AW[ai][ah]",tree.getCurrent().getPrevious().getToken());
        assertEquals("AB[bi][bh]",tree.getCurrent().getToken());
    }

    @Test
    void fileTreeToString(){
        tree.addNode(SgfToken.B,FileTree.calculateCoordinate(9,2,3));
        System.out.println(tree.toString());
    }

}