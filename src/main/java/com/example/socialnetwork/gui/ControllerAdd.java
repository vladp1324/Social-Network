package com.example.socialnetwork.gui;

import com.example.socialnetwork.service.Service;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ControllerAdd {
    @FXML
    Label textAddUser;
    @FXML
    TextField firstNameTextField;
    @FXML
    TextField lastNameTextField;
    Service service;
    public void init(Service service) {
        this.service = service;
        textAddUser.setText("Introduceti datele necesare pentru adaugarea unui utilizator");
    }
    public void onAddButton(){
        String first_name = firstNameTextField.getText();
        String last_name = lastNameTextField.getText();
        try {
            service.addUser(first_name, last_name);
            MessageAlert.showMessage(null, Alert.AlertType.CONFIRMATION, null, "Utilizatorul a fost adaugat in baza de date!");

            Stage stage = (Stage) textAddUser.getScene().getWindow();
            stage.close();
        }
        catch(Exception e){
            MessageAlert.showErrorMessage(null, e.getMessage());
        }
    }
}
