package pr_se.gogame.view_controller;

import pr_se.gogame.view_controller.observer.ViewListener;

import java.util.LinkedList;
import java.util.List;

/**
 * Stores display setting that exclusively pertain to the view
 */
public final class GlobalSettings {
    /**
     * If true, the view will show additional debug information.
     */
    public static final boolean DEBUG = false;

    /**
     * If true, moves have to be confirmed separately.
     */
    private static boolean confirmationNeeded = false;

    /**
     * If true, move numbers are shown on stones.
     */
    private static boolean showMoveNumbers = false;

    /**
     * If true, coordinate axes are shown at the sides of the board
     */
    private static boolean showCoordinates = true;

    /**
     * Contains the path of the folder containing the graphics packs
     */
    public static final String GRAPHICS_PACK_FOLDER = "./Graphics Packs";

    /**
     * Contains the file name (NOT THE PATH) of the default graphics pack
     */
    private static String graphicsPackFileName = "default.zip";

    /**
     * Contains all listeners listening to the GlobalSettings
     */
    private static final List<ViewListener> listeners = new LinkedList<>();

    private GlobalSettings() {
        // This private constructor solely exists to prevent instantiation.
    }

    /**
     * Adds the supplied listener
     * @param l the listener to be added
     */
    public static void addListener(ViewListener l) {
        if(l == null) {
            throw new NullPointerException();
        }
        GlobalSettings.listeners.add(l);
    }

    /**
     * Removes the supplied listener
     * @param l the listener to be removed
     */
    public static void removeListener(ViewListener l) {
        GlobalSettings.listeners.remove(l);
    }

    /**
     * Notifies all listeners that a move has been confirmed
     */
    public static void confirmMove() {
        for(ViewListener l : listeners) {
            l.onMoveConfirmed();
        }
    }

    /**
     * Notifies all listeners that a setting has been updated
     */
    public static void update() {
        for(ViewListener l : listeners) {
            l.onSettingsUpdated();
        }
    }

    // Getters and Setters

    /**
     * @return whether move confirmation is necessary
     */
    public static boolean isConfirmationNeeded() {
        return confirmationNeeded;
    }

    /**
     * Sets whether moves have to be confirmed separately
     * @param confirmationNeeded whether moves have to be confirmed separately
     */
    public static void setConfirmationNeeded(boolean confirmationNeeded) {
        GlobalSettings.confirmationNeeded = confirmationNeeded;
        update();
    }

    /**
     * @return whether move numbers are shown on the stones
     */
    public static boolean isShowMoveNumbers() {
        return showMoveNumbers;
    }

    /**
     * Sets whether move numbers should be shown on the stones
     * @param showMoveNumbers whether move numbers should be shown on the stones
     */
    public static void setShowMoveNumbers(boolean showMoveNumbers) {
        GlobalSettings.showMoveNumbers = showMoveNumbers;
        update();
    }

    /**
     * @return whether coordinate axes should be shown on the sides of the board
     */
    public static boolean isShowCoordinates() {
        return showCoordinates;
    }

    /**
     * Sets whether coordinate axes should be shown on the sides of the board
     * @param showCoordinates whether coordinate axes should be shown on the sides of the board
     */
    public static void setShowCoordinates(boolean showCoordinates) {
        GlobalSettings.showCoordinates = showCoordinates;
        update();
    }

    /**
     * @return the full path of the currently selected graphics pack
     */
    public static String getGraphicsPath() {
        return GRAPHICS_PACK_FOLDER + "/" + graphicsPackFileName;
    }

    /**
     * @return the file (NOT THE PATH) name of the currently selected graphics pack
     */
    public static String getGraphicsPackFileName() {
        return graphicsPackFileName;
    }

    /**
     * Sets the file name (NOT THE PATH) of the currently selected graphics pack
     * @param graphicsPackFileName the file name (NOT THE PATH) of the currently selected graphics pack
     */
    public static void setGraphicsPackFileName(String graphicsPackFileName) {
        if(graphicsPackFileName == null) {
            throw new NullPointerException();
        }
        GlobalSettings.graphicsPackFileName = graphicsPackFileName;
        update();
    }
}
