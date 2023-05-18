package pr_se.gogame.model;

import org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class FileTreeTest {

    private final FileTree tree = new FileTree(9,"Black","White");
    @BeforeEach
    void setUp() {

        /*TreeNode first = new TreeNode(SgfToken.B,"bh");
        TreeNode second = new TreeNode(SgfToken.B,"cg");
        TreeNode third = new TreeNode(SgfToken.B,"dh");
        TreeNode fourth = new TreeNode(SgfToken.B,"sa");*/
        tree.addMove(SgfToken.B,0,0);
        tree.addMove(SgfToken.W,0,1);
        tree.addMove(SgfToken.B,1,0);
        tree.addMove(SgfToken.W,1,1);
    }

    @Test
    void testPrint(){
        tree.printGameTree();
    }

    @Test
    void getStart() {
        assertEquals("(;FF[4]GM[1]SZ[9]PB[Black]PW[White]",tree.getStart().toString());
    }

    @Test
    void addNode() {
        tree.addMove(SgfToken.AE,1,1);
        assertEquals(tree.getCurrent().getToken(),"AE[bh]");
    }
    @Test
    void viewing() {
        Node testView = tree.viewCurrent();
        assertEquals(";W[bh]",testView.getToken());
        testView = tree.viewPrev();
        testView = tree.viewPrev();
        assertEquals(";W[ah]",testView.getToken());
        testView = tree.viewNext();
        assertEquals(";B[bi]",testView.getToken());


    }

}