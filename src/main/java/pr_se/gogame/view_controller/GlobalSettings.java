package pr_se.gogame.view_controller;

import pr_se.gogame.view_controller.observer.ViewListener;

import java.util.LinkedList;
import java.util.List;

public final class GlobalSettings {
    public static final boolean DEBUG = false;

    private static boolean confirmationNeeded = false;

    private static boolean showMoveNumbers = false;

    private static boolean showCoordinates = true;

    public static final String GRAPHICS_PACK_FOLDER = "./Graphics Packs";

    private static String graphicsPackFileName = "default.zip";

    private static final List<ViewListener> listeners = new LinkedList<>();

    private GlobalSettings() {
        // This private constructor solely exists to prevent instantiation.
    }

    public static void addListener(ViewListener l) {
        if(l == null) {
            throw new NullPointerException();
        }
        GlobalSettings.listeners.add(l);
    }

    public static void removeListener(ViewListener l) {
        GlobalSettings.listeners.remove(l);
    }

    public static void confirmMove() {
        for(ViewListener l : listeners) {
            l.onMoveConfirmed();
        }
    }

    private static void update() {
        for(ViewListener l : listeners) {
            l.onSettingsUpdated();
        }
    }

    // Getters and Setters

    public static boolean isConfirmationNeeded() {
        return confirmationNeeded;
    }

    public static void setConfirmationNeeded(boolean confirmationNeeded) {
        GlobalSettings.confirmationNeeded = confirmationNeeded;
        update();
    }

    public static boolean isShowMoveNumbers() {
        return showMoveNumbers;
    }

    public static void setShowMoveNumbers(boolean showMoveNumbers) {
        GlobalSettings.showMoveNumbers = showMoveNumbers;
        update();
    }

    public static boolean isShowCoordinates() {
        return showCoordinates;
    }

    public static void setShowCoordinates(boolean showCoordinates) {
        GlobalSettings.showCoordinates = showCoordinates;
        update();
    }

    public static String getGraphicsPath() {
        return GRAPHICS_PACK_FOLDER + "/" + graphicsPackFileName;
    }

    public static String getGraphicsPackFileName() {
        return graphicsPackFileName;
    }

    public static void setGraphicsPackFileName(String graphicsPackFileName) {
        if(graphicsPackFileName == null) {
            throw new NullPointerException();
        }
        GlobalSettings.graphicsPackFileName = graphicsPackFileName;
        update();
    }
}
