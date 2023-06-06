module pr_se.gogame {
    requires javafx.controls;
    // opens  pr_se.gogame; // No longer necessary as resources are now stored in root or ./Grafiksets
    exports pr_se.gogame.view_controller;
    opens pr_se.gogame.view_controller;
    exports pr_se.gogame.model;
    opens pr_se.gogame.model;
}