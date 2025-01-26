module com.example.aoopproject {
    requires de.jensd.fx.glyphs.fontawesome;
    requires MaterialFX;
    requires org.json;
    requires javafx.controls;
    requires javafx.fxml;
    requires java.compiler;
    requires org.jsoup;
    requires Java.WebSocket;
    requires java.sql;
    requires mysql.connector.java;

    opens com.example.aoopproject to javafx.fxml;
    opens com.example.aoopproject.controllers to javafx.fxml;
    opens com.example.aoopproject.controllers.admin to javafx.fxml;
    opens com.example.aoopproject.controllers.student to javafx.fxml;
    opens com.example.aoopproject.Instructor to javafx.graphics, javafx.fxml;
    opens com.example.aoopproject.views to javafx.fxml;
    opens com.example.aoopproject.models to javafx.base, javafx.fxml, javafx.graphics;

    exports com.example.aoopproject;
    exports com.example.aoopproject.controllers;
    exports com.example.aoopproject.controllers.admin;
    exports com.example.aoopproject.controllers.student;
    exports com.example.aoopproject.models;
    exports com.example.aoopproject.views;
    exports com.example.aoopproject.Instructor;
    exports com.example.aoopproject.database;
}