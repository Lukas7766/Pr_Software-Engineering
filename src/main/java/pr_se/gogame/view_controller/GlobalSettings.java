package pr_se.gogame.view_controller;

import java.util.LinkedList;
import java.util.List;

public class GlobalSettings {

    private static boolean confirmationNeeded = false;

    private static boolean showMoveNumbers = false;

    private static boolean showCoordinates = true;

    private static final List<ViewListener> listeners = new LinkedList<>();

    public static void addListener(ViewListener l) {
        listeners.add(l);
    }

    public static void removeListener(ViewListener l) {
        listeners.remove(l);
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
}
