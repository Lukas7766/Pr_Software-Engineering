package pr_se.gogame.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class newFileSaver {
    //TODO: closing bracket when saving
    private Path path;
    String buffer;

    //private boolean in_A_Branch;

    public newFileSaver(Path path) {
        this.path = path;
    }

    public newFileSaver() {
    }

    public void setFilepath(Path filepath) {
        this.path = filepath;
    }

    //TODO: File Speichern (static evtl)
    public boolean saveFile(Path filepath) {
        try {
            Files.write(filepath, buffer.getBytes());
            return true;
        } catch (IOException e) {
            System.out.println("File write Error");
            return false;
        }
    }

}
