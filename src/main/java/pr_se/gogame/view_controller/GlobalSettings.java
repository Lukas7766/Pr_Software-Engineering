package pr_se.gogame.view_controller;

import java.util.LinkedList;
import java.util.List;

public final class GlobalSettings {
    public static final boolean DEBUG = false; // TODO: Remove in final product (or maybe not)

    private static boolean confirmationNeeded = false;

    private static boolean showMoveNumbers = false;

    private static boolean showCoordinates = true;

    public static final String GRAPHICS_PACK_FOLDER = "./Grafiksets";

    private static String graphicsPack = "/default.zip";

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
        return GRAPHICS_PACK_FOLDER + graphicsPack;
    }

    public static String getGraphicsPack() {
        return graphicsPack;
    }

    public static void setGraphicsPack(String graphicsPack) {
        if(graphicsPack == null) {
            throw new NullPointerException();
        }
        GlobalSettings.graphicsPack = graphicsPack;
        update();
    }
}
