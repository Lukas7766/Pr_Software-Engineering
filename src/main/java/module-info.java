module pr_se.gogame {
    requires javafx.controls;
    exports pr_se.gogame.view_controller;
    opens pr_se.gogame.view_controller;
    exports pr_se.gogame.model;
    opens pr_se.gogame.model;
    exports pr_se.gogame.model.file;
    opens pr_se.gogame.model.file;
    exports pr_se.gogame.model.ruleset;
    opens pr_se.gogame.model.ruleset;
    exports pr_se.gogame.view_controller.dialog;
    opens pr_se.gogame.view_controller.dialog;
    exports pr_se.gogame.view_controller.observer;
    opens pr_se.gogame.view_controller.observer;
}