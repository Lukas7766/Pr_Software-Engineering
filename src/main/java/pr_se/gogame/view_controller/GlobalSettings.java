package pr_se.gogame.view_controller;

import java.util.LinkedList;
import java.util.List;

public class GlobalSettings {

    private static boolean confirmationNeeded = false;

    private static boolean showMoveNumbers = false;

    private static boolean showCoordinates = true;

    private static String graphicsPath = "./Grafiksets/default.zip";

    private static boolean demoMode = false;

    private static final List<ViewListener> listeners = new LinkedList<>();

    public static void addListener(ViewListener l) {
        if(l == null) {
            throw new NullPointerException();
        }
        GlobalSettings.listeners.add(l);
    }

    public static void removeListener(ViewListener l) {
        GlobalSettings.listeners.remove(l);
    }

    private static void update() {
        for(ViewListener l : listeners) {
            l.fire();
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
        return graphicsPath;
    }

    public static void setGraphicsPath(String graphicsPath) {
        if(graphicsPath == null) {
            throw new NullPointerException();
        }
        GlobalSettings.graphicsPath = graphicsPath;
        update();
    }

    public static boolean isDemoMode() {
        return demoMode;
    }

    public static void setDemoMode(boolean demoMode) {
        GlobalSettings.demoMode = demoMode;
        update();
    }
}
