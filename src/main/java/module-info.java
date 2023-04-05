module pr_se.gogame {
    requires javafx.controls;
    opens  pr_se.gogame;
    // exports pr_se.gogame;
    exports pr_se.gogame.view_controller;
    opens pr_se.gogame.view_controller;
    exports pr_se.gogame.model;
    opens pr_se.gogame.model;
}