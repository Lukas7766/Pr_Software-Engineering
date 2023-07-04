package pr_se.gogame.model.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.NoSuchFileException;

public interface FileHandler {
    /**
     * Saves a game to the supplied file
     * @param file The file to be saved
     * @return True if saving succeeded, false otherwise
     */
    boolean saveFile(File file) throws IOException, IllegalStateException;

    /**
     * Loads a game from the supplied file
     * @param file The file to be loaded
     * @return True if loading succeeded, false otherwise
     * @throws NoSuchFileException If the file to be loaded does not exist
     * @throws LoadingGameException If a content-related error occurred while loading the game, mostly due to
     *  unsupported features
     */
    boolean loadFile(File file) throws NoSuchFileException, LoadingGameException;

    /**
     * Returns the currently set file.
     * @return The file that this FileHandler currently saves to, null if no file has been set (i.e., loaded or saved)
     *  yet.
     */
    File getCurrentFile();
}
