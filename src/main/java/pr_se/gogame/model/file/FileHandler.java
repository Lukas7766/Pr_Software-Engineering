package pr_se.gogame.model.file;

import java.io.File;
import java.nio.file.NoSuchFileException;

public interface FileHandler {
    boolean saveFile(File file);

    boolean loadFile(File file) throws NoSuchFileException, LoadingGameException;

    File getCurrentFile();
}
