package com.example.socialnetwork.gui;

import com.example.socialnetwork.Main;
import com.example.socialnetwork.domain.User;
import com.example.socialnetwork.service.Service;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class ControllerLogin {
    @FXML
    TextField usernameTextField;
    @FXML
    PasswordField passwordTextField;
    Service service;

    void closeCurrentWindow() {
        Stage stage = (Stage) usernameTextField.getScene().getWindow();
        stage.close();
    }

    public void init(Service service) {
        this.service = service;
    }

    public void onUserSignIn() throws IOException {
        String username = usernameTextField.getText();
        String password = passwordTextField.getText();

        Optional<User> connectedUser = service.login(username, password);

        if (connectedUser.isEmpty()) {
            MessageAlert.showErrorMessage(null, "Incorrect username or password!");
            return;
        }

        MessageAlert.showMessage(null, Alert.AlertType.CONFIRMATION, null, "Successful login!");

        Stage stage = (Stage) usernameTextField.getScene().getWindow();
        closeCurrentWindow();

        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("user-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        ControllerUser controllerUser = fxmlLoader.getController();
        controllerUser.init(service, connectedUser.get());
        stage.setScene(scene);
        stage.show();
    }

    public void onUserSignUp() throws IOException {
        closeCurrentWindow();

        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("addUser-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        ControllerAdd controllerAdd = fxmlLoader.getController();
        controllerAdd.init(service);
        Stage addUserStage = new Stage();
        addUserStage.setScene(scene);
        addUserStage.show();
    }
}
