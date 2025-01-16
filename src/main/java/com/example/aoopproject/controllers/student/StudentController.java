package com.example.aoopproject.controllers.student;

import com.example.aoopproject.views.ViewFactory;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class StudentController {

    @FXML
    public VBox adminVBox;

    @FXML
    public Button notificationsButton;

    @FXML
    public Button notesButton;

    @FXML
    public Button timerButton;

    @FXML
    public Button doubtsolvingButton;

    @FXML
    private Button logoutButton;

    @FXML
    private void handleLogoutButtonAction(ActionEvent ignoredEvent) {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        ViewFactory.getInstance().showLoginScreen(stage);
    }
}