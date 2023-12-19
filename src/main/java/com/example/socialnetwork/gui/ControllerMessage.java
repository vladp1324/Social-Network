package com.example.socialnetwork.gui;

import com.example.socialnetwork.domain.Message;
import com.example.socialnetwork.domain.User;
import com.example.socialnetwork.repository.Page;
import com.example.socialnetwork.repository.Pageable;
import com.example.socialnetwork.service.Service;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


public class ControllerMessage {
    private Service service;
    @FXML
    TableView<User> userTableView;
    @FXML
    TableColumn<User, Long> idUser;
    @FXML
    TableColumn<User, String> firstNameUser;
    @FXML
    TableColumn<User, String> lastNameUser;
    @FXML
    TextField message;

    ObservableList<User> usersModel = FXCollections.observableArrayList();

    @FXML
    TableView<Message> messageTableView;
    ObservableList<Message> messagesModel = FXCollections.observableArrayList();

    @FXML
    TableColumn<Message, Long> idMessage;
    @FXML
    TableColumn<Message, User> userfrom;
    @FXML
    TableColumn<Message, User> userto;
    @FXML
    TableColumn<Message, String> text;
    @FXML
    TableColumn<Message, LocalDateTime> dateTime;
    @FXML
    TableColumn<Message, Long> idreply;
    @FXML
    ComboBox<User> userComboBox;
    User oldUserComboBox;
    User currentUserComboBox;

    private int currentPageUsers = 0;
    private int currentPageMessage = 0;
    private int pageSize;

    private void initModelUsers(){
        Page<User> pageUsers = service.findAllUsers(new Pageable(currentPageUsers, pageSize));
        int maxPageUsers = (int) Math.ceil((double) pageUsers.getTotalElementCount() / pageSize) - 1;
        if (currentPageUsers > maxPageUsers) {
            currentPageUsers = maxPageUsers;
            pageUsers = service.findAllUsers(new Pageable(currentPageUsers, pageSize));
        }
        usersModel.setAll(StreamSupport.stream(pageUsers.getElementsOnPage().spliterator(),
                false).collect(Collectors.toList()));
    }
    private void initModelMessages(){
        Page<Message> pageMessages = service.findAllMessages(new Pageable(currentPageMessage, pageSize));
        int maxPageMessages = (int) Math.ceil((double) pageMessages.getTotalElementCount() / pageSize) - 1;
        if (currentPageMessage > maxPageMessages) {
            currentPageMessage = maxPageMessages;
            pageMessages = service.findAllMessages(new Pageable(currentPageMessage, pageSize));
        }
        messagesModel.setAll(StreamSupport.stream(pageMessages.getElementsOnPage().spliterator(),
                false).collect(Collectors.toList()));


    }

    public void onPreviousUser(ActionEvent actionEvent) {
        if(currentPageUsers > 0)
            currentPageUsers--;

        initModelUsers();
    }

    public void onNextUser(ActionEvent actionEvent) {
        if(currentPageUsers <= pageSize)
            currentPageUsers++;

        initModelUsers();
    }

    public void onPreviousMessage(ActionEvent actionEvent) {
        if(currentPageMessage > 0)
            currentPageMessage--;

        initModelMessages();
    }

    public void onNextMessage(ActionEvent actionEvent) {
        if(currentPageMessage <= pageSize)
            currentPageMessage++;

        initModelMessages();
    }

    public void init(Service service, int pgsize) {
        this.pageSize = pgsize;
        this.service = service;
        userTableView.setItems(usersModel);
        messageTableView.setItems(messagesModel);

        //initializez tabela users
        idUser.setCellValueFactory(new PropertyValueFactory<>("id"));
        firstNameUser.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameUser.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        userTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        updateUsersTable();

        //intializez tabela messages
        idMessage.setCellValueFactory(new PropertyValueFactory<>("id"));
        userfrom.setCellValueFactory(new PropertyValueFactory<>("from"));
        userto.setCellValueFactory(new PropertyValueFactory<>("to"));
        text.setCellValueFactory(new PropertyValueFactory<>("text"));
        dateTime.setCellValueFactory(new PropertyValueFactory<>("dateTime"));
        idreply.setCellValueFactory(new PropertyValueFactory<>("idreply"));
        updateMessagesTable();

        //initializez combox-ul de users
        Iterable<User> users = service.getAllUsers();
        List<User> userList = StreamSupport.stream(users.spliterator(), false)
                .collect(Collectors.toList());
        ObservableList<User> userOptions = FXCollections.observableArrayList(userList);
        userComboBox.setItems(userOptions);

        userComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                currentUserComboBox = newValue;
                oldUserComboBox = oldValue;
            }
        });
    }

    private void updateUsersTable() {
        usersModel.setAll(StreamSupport.stream(service.getAllUsers().spliterator(), false).collect(Collectors.toList()));
        initModelUsers();
    }

    private void updateMessagesTable() {
        messagesModel.setAll(StreamSupport.stream(service.getAllMessages().spliterator(), false).collect(Collectors.toList()));
        initModelMessages();
    }

    public void onRefreshButton(){
        updateMessagesTable();
    }

    public void onSendMessageButton() {
        if (message.getText().isEmpty()) {
            MessageAlert.showErrorMessage(null, "No message to send!");
            return;
        }
        ObservableList<User> selectedUsers = userTableView.getSelectionModel().getSelectedItems();

        Message message1 = messageTableView.getSelectionModel().getSelectedItem();
        Long iduserfrom = currentUserComboBox.getId();
        if (message1 != null) {
            //Long iduserfrom = selectedUsers.get(0).getId();
            Long iduserto = selectedUsers.get(0).getId();
//            if(iduserfrom == iduserto){
//                MessageAlert.showErrorMessage(null, "You can't reply to yourself!");
//                return;
//            }
            if (iduserfrom != message1.getTo().getId() || iduserto != message1.getFrom().getId()) {
                MessageAlert.showErrorMessage(null, "You can't reply to this message!");
                return;
            }
            service.replyMessage(iduserfrom, iduserto, message.getText(), message1.getId());
            updateMessagesTable();
        } else {
            if (selectedUsers.isEmpty()) {
                MessageAlert.showErrorMessage(null, "No user selected!");
                return;
            }
            List<User> users = new ArrayList<>(selectedUsers);
            if (StreamSupport.stream(users.spliterator(), false).anyMatch(u -> u.getId() == iduserfrom)) {
                MessageAlert.showErrorMessage(null, "You can't send a message to yourself!");
                return;
            }
            LocalDateTime localDateTime = LocalDateTime.now();
            for (int i = 0; i < users.size(); i++) {
                service.sendMessage(iduserfrom, users.get(i).getId(), message.getText(), localDateTime);
                updateMessagesTable();
            }
        }
    }

    public void onMessageOrderedByDate() {
        ObservableList<User> selectedUsers = userTableView.getSelectionModel().getSelectedItems();
        if (selectedUsers.size() != 2) {
            MessageAlert.showErrorMessage(null, "Select 2 users!");
            return;
        }
        Iterable<Message> conversation = service.getConversation(selectedUsers.get(0).getId(), selectedUsers.get(1).getId());
        conversation.forEach(System.out::println);
        messagesModel.setAll(StreamSupport.stream(conversation.spliterator(), false).collect(Collectors.toList()));
    }

}
