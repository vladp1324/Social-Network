package com.example.socialnetwork;

import com.example.socialnetwork.domain.validators.UserValidator;
import com.example.socialnetwork.domain.validators.ValidatorFriendship;
import com.example.socialnetwork.gui.ControllerLogin;
import com.example.socialnetwork.repository.FriendRequestDBRepository;
import com.example.socialnetwork.repository.FriendshipDBRepository;
import com.example.socialnetwork.repository.MessageDBRepository;
import com.example.socialnetwork.repository.UserDBRepository;
import com.example.socialnetwork.service.Service;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        String url = "jdbc:postgresql://localhost:5432/socialnetwork";
        String username = "postgres";
        String password;
        {
            password = "a";
        }

        UserDBRepository userRepository = new UserDBRepository(url, username, password);
        UserValidator userValidator = new UserValidator();

        FriendshipDBRepository friendshipRepository = new FriendshipDBRepository(url, username, password, userRepository);
        ValidatorFriendship friendshipValidator = new ValidatorFriendship();

        FriendRequestDBRepository friendRequestDBRepository = new FriendRequestDBRepository(url, username, password, userRepository);
        MessageDBRepository messageDBRepository = new MessageDBRepository(url, username, password, userRepository);

        Service service = new Service(userRepository, userValidator, friendshipRepository, friendshipValidator, friendRequestDBRepository, messageDBRepository);

        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        ControllerLogin controllerLogin = fxmlLoader.getController();
        controllerLogin.init(service);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}