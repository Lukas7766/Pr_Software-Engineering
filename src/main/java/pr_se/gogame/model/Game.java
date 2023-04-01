package pr_se.gogame.model;

import java.nio.file.Path;

public class Game implements  GameInterface {
    @Override
    public boolean saveGame() {
        return true;
    }

    @Override
    public void newGame() {
        System.out.println("newGame");
    }

    @Override
    public boolean importGame(Path path) {
        System.out.println(path);
        return true;
    }

    @Override
    public boolean exportGame(Path path) {
        System.out.println(path);
        return true;
    }

    @Override
    public void pass() {
        System.out.println("pass");
    }

    @Override
    public void resign() {
        System.out.println("resign");

    }

    @Override
    public void scoreGame() {
        System.out.println("scoreGame");
    }
}
