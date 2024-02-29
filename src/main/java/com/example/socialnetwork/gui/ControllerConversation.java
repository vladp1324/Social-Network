package com.example.socialnetwork.gui;

import com.example.socialnetwork.domain.Message;
import com.example.socialnetwork.domain.User;
import com.example.socialnetwork.service.Service;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ControllerConversation {
    Service service;
    private User from;
    private User to;
    @FXML
    ListView<Message> MessagesListView;
    ObservableList<Message> MessagesModel = FXCollections.observableArrayList();
    @FXML
    TextField messageTextField;

    public void init(Service service, User from, User to) {
        this.service = service;
        this.from = from;
        this.to = to;

        MessagesListView.setItems(MessagesModel);

        updateMessages();
    }

    private void updateMessages() {
        MessagesModel.setAll(StreamSupport.stream(service.showConversation(from.getId(), to.getId()).spliterator(), false).collect(Collectors.toList()));
    }

    public void onSendMessage() {
        if (messageTextField.getText().isBlank()) {
            MessageAlert.showErrorMessage(null, "No message to send!");
            return;
        }

        String text = messageTextField.getText();
        messageTextField.clear();

        Message selectedMessage = MessagesListView.getSelectionModel().getSelectedItem();
        LocalDateTime localDateTime = LocalDateTime.now();

        if (selectedMessage == null) {
            service.sendMessage(from.getId(), to.getId(), text, localDateTime);

            updateMessages();

            return;
        }

        service.replyMessage(from.getId(), to.getId(), text, selectedMessage.getId());
        updateMessages();
    }
}