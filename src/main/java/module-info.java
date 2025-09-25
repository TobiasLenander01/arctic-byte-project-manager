module db.dropalltables {
    requires javafx.base;
    requires transitive javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires java.sql;
    requires java.desktop;

    exports com.dropalltables;
    exports com.dropalltables.controllers;
    exports com.dropalltables.models;

    opens com.dropalltables.controllers to javafx.fxml;
    opens com.dropalltables.models to javafx.base;
}
