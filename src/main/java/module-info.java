module com.example.aoopproject {
    requires de.jensd.fx.glyphs.fontawesome;
    requires java.sql;
    requires MaterialFX;


    opens com.example.aoopproject to javafx.fxml;
    opens com.example.aoopproject.controllers to javafx.fxml;
    opens com.example.aoopproject.controllers.admin to javafx.fxml;
    opens com.example.aoopproject.controllers.student to javafx.fxml;
    exports com.example.aoopproject;
    exports com.example.aoopproject.controllers;
    exports com.example.aoopproject.controllers.admin;
    exports com.example.aoopproject.controllers.student;
    exports com.example.aoopproject.models;
    exports com.example.aoopproject.views;
}