package com.example.socialnetwork.gui;

import com.example.socialnetwork.Main;
import com.example.socialnetwork.domain.User;
import com.example.socialnetwork.service.Service;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ControllerUser {
    Service service;
    private User connectedUser;
    @FXML
    ListView<User> usersListView;
    ObservableList<User> usersModel = FXCollections.observableArrayList();

    @FXML
    ListView<User> friendsListView;
    ObservableList<User> friendshipsModel = FXCollections.observableArrayList();

    @FXML
    TextField messageTextField;

    void closeCurrentWindow() {
        Stage stage = (Stage) usersListView.getScene().getWindow();
        stage.close();
    }

    public void init(Service service, User connectedUser) {
        this.service = service;
        this.connectedUser = connectedUser;
        usersListView.setItems(usersModel);
        friendsListView.setItems(friendshipsModel);

        friendsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        updateUsers();
        updateFriends();
    }

    private void updateUsers() {
        usersModel.setAll(StreamSupport.stream(service.getAllUsers().spliterator(),
                false).collect(Collectors.toList()));
    }

    private void updateFriends() {
        friendshipsModel.setAll(StreamSupport.stream(service.getAllFriends(connectedUser.getId()).spliterator(),
                false).collect(Collectors.toList()));
    }

    public void onSendFriendRequest() {
        User selectedUser = usersListView.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            MessageAlert.showErrorMessage(null, "No user selected!");
            return;
        }

        try {
            service.addFriendRequest(connectedUser.getId(), selectedUser.getId());
            updateFriends();
        } catch (Exception e) {
            MessageAlert.showErrorMessage(null, e.getMessage());
        }
    }

    public void onShowFriendRequests() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("friend-requests-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        ControllerFriendRequests controllerFriendRequests = fxmlLoader.getController();
        controllerFriendRequests.init(service, connectedUser, friendshipsModel);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.show();
    }

    public void onDeleteFriend() {
        ObservableList<User> selectedFriends = friendsListView.getSelectionModel().getSelectedItems();
        if (selectedFriends.size() != 1) {
            MessageAlert.showErrorMessage(null, "Select 1 friend!");
            return;
        }

        User friend = selectedFriends.get(0);

        try {
            service.removeFriendship(connectedUser.getId(), friend.getId());
            updateFriends();
        } catch (Exception e) {
            MessageAlert.showErrorMessage(null, e.getMessage());
        }
    }

    public void onSendMessage() {
        if (messageTextField.getText().isEmpty()) {
            MessageAlert.showErrorMessage(null, "No message to send!");
            return;
        }

        ObservableList<User> selectedFriends = friendsListView.getSelectionModel().getSelectedItems();
        if (selectedFriends.isEmpty()) {
            MessageAlert.showErrorMessage(null, "Select at least 1 friend!");
            return;
        }

        List<User> friends = new ArrayList<>(selectedFriends);
        LocalDateTime localDateTime = LocalDateTime.now();


        friends.forEach(friend ->
                service.sendMessage(connectedUser.getId(), friend.getId(), messageTextField.getText(), localDateTime));
        messageTextField.clear();
    }

    public void onOpenConversation() throws IOException {
        ObservableList<User> selectedFriends = friendsListView.getSelectionModel().getSelectedItems();
        if (selectedFriends.size() != 1) {
            MessageAlert.showErrorMessage(null, "Select 1 friend!");
            return;
        }

        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("conversation-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        ControllerConversation controllerConversation = fxmlLoader.getController();
        controllerConversation.init(service, connectedUser, selectedFriends.get(0));
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.show();
    }


    public void onManageAccount() throws IOException {
        closeCurrentWindow();

        Stage stage = (Stage) usersListView.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("manage_account-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        ControllerManageAccount controllerManageAccount = fxmlLoader.getController();
        controllerManageAccount.init(service, this.connectedUser);
        stage.setScene(scene);
        stage.show();
    }

    public void onLogout() throws IOException {
        closeCurrentWindow();

        Stage stage = (Stage) usersListView.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        ControllerLogin controllerLogin = fxmlLoader.getController();
        controllerLogin.init(service);
        stage.setScene(scene);
        stage.show();
    }
}