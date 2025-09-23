module db.dropalltables {
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires java.sql;

    exports com.dropalltables;
    exports com.dropalltables.controllers;

    opens com.dropalltables.controllers to javafx.fxml;
    opens com.dropalltables.models to javafx.base;
}
