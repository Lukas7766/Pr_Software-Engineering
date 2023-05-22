package pr_se.gogame.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileHandler {
    private Path path;

    public FileHandler(Path path) {
        this.path = path;
    }

    public FileHandler() {
    }

    public void setFilepath(Path filepath) {
        this.path = filepath;
    }

    public void saveFile(Path filepath,String data) {
        try {
            Files.write(filepath, data.getBytes());
        } catch (IOException e) {
            System.out.println("File write Error");
        }
    }

    public void loadFile(Path filepath){

    }

}
