package com.example.socialnetwork.gui;

import com.example.socialnetwork.Main;
import com.example.socialnetwork.domain.User;
import com.example.socialnetwork.service.Service;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class ControllerManageAccount {
    Service service;
    private User connectedUser;
    @FXML
    TextField newUsername;
    @FXML
    TextField newFirstName;
    @FXML
    TextField newLastName;
    @FXML
    TextField newPassword;

    void closeCurrentWindow() {
        Stage stage = (Stage) newFirstName.getScene().getWindow();
        stage.close();
    }

    public void init(Service service, User connectedUser) {
        this.service = service;
        this.connectedUser = connectedUser;
    }

    public void onBackButton() throws IOException {
        closeCurrentWindow();

        Stage stage = (Stage) newUsername.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("user-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        ControllerUser controllerUser = fxmlLoader.getController();
        controllerUser.init(service, this.connectedUser);
        stage.setScene(scene);
        stage.show();
    }

    public void onDeleteAccount() {
        try {
            Optional<User> deletedUser = service.removeUser(connectedUser.getId());
            if (deletedUser.isEmpty()) {
                MessageAlert.showErrorMessage(null, "This account doesn't exist!");
                return;
            }

            MessageAlert.showMessage(null, Alert.AlertType.CONFIRMATION, null, "Account deleted successfully!");

            closeCurrentWindow();

            Stage stage = (Stage) newUsername.getScene().getWindow();
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("login-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            ControllerLogin controllerLogin = fxmlLoader.getController();
            controllerLogin.init(service);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            MessageAlert.showErrorMessage(null, e.getMessage());
        }
    }

    public void onModifyAccount() {
        String first_name = newFirstName.getText();
        String last_name = newLastName.getText();
        String username = newUsername.getText();
        String password = newPassword.getText();
        try {
            service.updateUser(connectedUser.getId(), first_name, last_name, username, password);
            MessageAlert.showMessage(null, Alert.AlertType.CONFIRMATION, null, "Changes applied successfully!");

            closeCurrentWindow();
            Stage stage = (Stage) newUsername.getScene().getWindow();

            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("user-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            ControllerUser controllerUser = fxmlLoader.getController();
            controllerUser.init(service, connectedUser);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            MessageAlert.showErrorMessage(null, e.getMessage());
        }
    }
}