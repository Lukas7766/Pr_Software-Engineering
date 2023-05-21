package pr_se.gogame.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class newFileSaver {
    private Path path;

    public newFileSaver(Path path) {
        this.path = path;
    }

    public newFileSaver() {
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

}
