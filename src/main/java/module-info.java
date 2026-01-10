module com.shajid.app.inventro {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.base;
    requires javafx.graphics;
    requires java.sql;
    requires org.apache.pdfbox;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;

    opens com.shajid.app.inventro.controller to javafx.fxml, javafx.base;
    opens com.shajid.app.inventro.model to javafx.base;

    exports com.shajid.app.inventro;
    exports com.shajid.app.inventro.model;
}
