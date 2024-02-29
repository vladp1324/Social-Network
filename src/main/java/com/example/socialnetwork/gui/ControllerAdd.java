package com.example.socialnetwork.gui;

import com.example.socialnetwork.Main;
import com.example.socialnetwork.service.Service;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class ControllerAdd {
    @FXML
    TextField firstNameTextField;
    @FXML
    TextField lastNameTextField;
    @FXML
    TextField usernameTextField;
    @FXML
    TextField passwordTextField;
    Service service;

    public void init(Service service) {
        this.service = service;
    }

    void closeCurrentWindow() {
        Stage stage = (Stage) firstNameTextField.getScene().getWindow();
        stage.close();
    }

    void openLoginWindow() throws IOException {
        Stage stage = (Stage) firstNameTextField.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        ControllerLogin controllerLogin = fxmlLoader.getController();
        controllerLogin.init(service);
        stage.setScene(scene);
        stage.show();
    }

    public void onAddButton() {
        String first_name = firstNameTextField.getText();
        String last_name = lastNameTextField.getText();
        String username = usernameTextField.getText();
        String password = passwordTextField.getText();
        try {
            service.addUser(first_name, last_name, username, password);
            MessageAlert.showMessage(null, Alert.AlertType.CONFIRMATION, null, "Account successfully created!");

            closeCurrentWindow();

            openLoginWindow();
        } catch (Exception e) {
            MessageAlert.showErrorMessage(null, e.getMessage());
        }
    }

    public void onBackButton() throws IOException {
        closeCurrentWindow();

        openLoginWindow();
    }
}