package com.example.socialnetwork.gui;

import com.example.socialnetwork.domain.User;
import com.example.socialnetwork.service.Service;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ControllerFriendRequests {
    Service service;
    private User connectedUser;
    @FXML
    ObservableList<User> friendshipsModel;

    @FXML
    ListView<User> friendRequestsListView;
    ObservableList<User> friendRequestsModel = FXCollections.observableArrayList();

    public void init(Service service, User connectedUser, ObservableList<User> friendshipsModel) {
        this.service = service;
        this.connectedUser = connectedUser;
        this.friendshipsModel = friendshipsModel;

        friendRequestsListView.setItems(friendRequestsModel);

        updateFriendRequests();
    }

    public void updateFriendRequests() {
        friendRequestsModel.setAll(StreamSupport.stream(service.getAllFriendRequests(connectedUser.getId()).spliterator(),
                false).collect(Collectors.toList()));
    }

    private void updateFriends() {
        friendshipsModel.setAll(StreamSupport.stream(service.getAllFriends(connectedUser.getId()).spliterator(),
                false).collect(Collectors.toList()));
    }

    public void onAcceptFriendRequest() {
        User user = friendRequestsListView.getSelectionModel().getSelectedItem();
        if (user == null) {
            MessageAlert.showErrorMessage(null, "No user selected!");
            return;
        }

        try {
            service.acceptFriendRequest(connectedUser.getId(), user.getId());
            updateFriendRequests();
            updateFriends();
        } catch (Exception e) {
            MessageAlert.showErrorMessage(null, e.getMessage());
        }
    }

    public void onDeclineFriendRequest() {
        User user = friendRequestsListView.getSelectionModel().getSelectedItem();
        if (user == null) {
            MessageAlert.showErrorMessage(null, "No user selected!");
            return;
        }

        try {
            service.rejectFriendRequest(connectedUser.getId(), user.getId());
            updateFriendRequests();
        } catch (Exception e) {
            MessageAlert.showErrorMessage(null, e.getMessage());
        }
    }
}