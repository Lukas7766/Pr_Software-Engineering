package pr_se.gogame.model;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileHandler {

    FileTree tree;

    String namePlayerBlack;

    String getNamePlayerWhite;

    int boardSize = 0;

    private final Map<Pattern, BiConsumer<Pattern, String>> patternToMethod = createPatternMap();

    int handicap ;
    private Path path;

    public FileHandler(Path path) {
        this.path = path;
    }

    public FileHandler() {
    }

    public void setFilepath(Path filepath) {
        this.path = filepath;
    }

    public boolean saveFile(Path filepath, String data) {
        try {
            Files.write(filepath, data.getBytes());
            return true;
        } catch (IOException e) {
            System.out.println("File write Error");
            return false;
        }
    }

    public void loadFile(Path filepath) {
        String content;
        try {
            content = Files.readString(filepath);
            content = content.replaceAll("\\R", " ");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String[] test = content.split(";");
        for (String s : test) {
            processString(s);
            if (this.boardSize != 0){
                this.tree = new FileTree(this.boardSize);
            }
        }
    }


    public Map<Pattern, BiConsumer<Pattern, String>> createPatternMap() {
        Map<Pattern, BiConsumer<Pattern, String>> map = new HashMap<>();
        map.put(Pattern.compile("B\\[\\w+\\]"), this::addStoneBlack);
        map.put(Pattern.compile("FF\\[4\\]GM\\[1\\]SZ\\[(.+?)\\].*PB\\[(.+?)\\].*PW\\[(.+?)\\]"), this::processStartOfFile);
        map.put(Pattern.compile("W\\[(\\w+)\\]"), this::addNameWhite);
        map.put(Pattern.compile("AE\\[\\w+\\]"), this::addEmpty);
        //map.put(Pattern.compile("AW(\\w+)"), FileHandler::method1);
        //map.put(Pattern.compile("AB(\\w+)"), FileHandler::method1);
        map.put(Pattern.compile("PB\\[(\\w+)\\]"), this::addNameBlack);
        map.put(Pattern.compile("PW\\[(\\w+)\\]"), this::addNameWhite);
        //map.put(Pattern.compile("KM\\[(\\w+)\\]"), FileHandler::method1);
        //map.put(Pattern.compile("C\\[(\\w+)\\]"), FileHandler::method1);
        map.put(Pattern.compile("HA\\[(\\w+)\\]"), this::addHandicap);
        //map.put(Pattern.compile("MA\\[(\\w+)\\]"), FileHandler::method1);
        //map.put(Pattern.compile("LB\\[(\\w+):(\\w+)\\]"), FileHandler::method1);

        return Collections.unmodifiableMap(map);
    }

    public void processString(String s) {
        for (Map.Entry<Pattern, BiConsumer<Pattern, String>> entry : patternToMethod.entrySet()) {
            Matcher m = entry.getKey().matcher(s);
            if (m.matches()) {
                entry.getValue().accept(entry.getKey(), s);//TODO doesnt detect start file ? why
                return;
            }
        }
    }

    private void addStoneBlack(Pattern p, String s) {
        tree.addStone(StoneColor.BLACK, calculateGridCoordinates(s)[0], calculateGridCoordinates(s)[1]);
    }

    private void addStoneWhite(Pattern p, String s) {
        tree.addStone(StoneColor.WHITE, calculateGridCoordinates(s)[0], calculateGridCoordinates(s)[1]);
    }

    private void processStartOfFile(Pattern p, String s) {
        Matcher m = p.matcher(s);
        if (m.matches()) {
            this.tree = new FileTree(Integer.parseInt(m.group(0)));
            tree.addName(StoneColor.BLACK, m.group(1));
            tree.addName(StoneColor.WHITE, m.group(2));
        }
    }

    private void addNameBlack(Pattern p, String s) {
        // do something with s
    }

    private void addNameWhite(Pattern p, String s) {
        // do something with s
    }

    private void addHandicap(Pattern p, String s) {
        // do something with s
    }


    private void addEmpty(Pattern p, String s) {
        // do something with s
    }


    public int[] calculateGridCoordinates(String s) {
        return new int[]{s.charAt(0) - 97, this.boardSize - ((int) s.charAt(1) - 96)};
    }

    public void setMoves(Game game){
        Node start = tree.getStart();
        while (start != null){
            if (start.getToken().startsWith(";W")){
                int[] coords = tree.getGridCoords(start);
                game.playMove(coords[0],coords[1]);
            }
            if (start.getToken().startsWith(";B")){
                int[] coords = tree.getGridCoords(start);
                game.playMove(coords[0],coords[1]);
            }
            start = start.getNext();
        }
    }

}
