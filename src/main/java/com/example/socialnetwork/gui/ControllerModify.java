package com.example.socialnetwork.gui;

import com.example.socialnetwork.service.Service;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ControllerModify {
    @FXML
    Label textModifyUser;
    @FXML
    TextField idTextField;
    @FXML
    TextField firstNameTextField;
    @FXML
    TextField lastNameTextField;
    Service service;

    public void init(Service service) {
        this.service = service;
        textModifyUser.setText("Introduceti datele necesare pentru modificarea unui utilizator");
    }

    public void onModifyButton() {
        Long id = Long.parseLong(idTextField.getText());
        String first_name = firstNameTextField.getText();
        String last_name = lastNameTextField.getText();
        try {
            service.updateUser(id, first_name, last_name);
            MessageAlert.showMessage(null, Alert.AlertType.CONFIRMATION, null, "Utilizatorul a fost modificat cu succes!");

            Stage stage = (Stage) textModifyUser.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            MessageAlert.showErrorMessage(null, e.getMessage());
        }

    }
}
