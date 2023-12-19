package com.example.socialnetwork.gui;

import com.example.socialnetwork.HelloApplication;
import com.example.socialnetwork.domain.*;
import com.example.socialnetwork.domain.validators.UserValidator;
import com.example.socialnetwork.domain.validators.ValidationException;
import com.example.socialnetwork.domain.validators.ValidatorFriendship;
import com.example.socialnetwork.repository.*;
import com.example.socialnetwork.service.Service;
import com.example.socialnetwork.utils.Observer;
import com.example.socialnetwork.utils.UserChangeEvent;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ControllerMain implements Initializable, Observer<UserChangeEvent> {
    Service service;
    @FXML
    TableView<User> userTableView;
    @FXML
    TableColumn<User, Long> idUser;
    @FXML
    TableColumn<User, String> firstNameUser;
    @FXML
    TableColumn<User, String> lastNameUser;
    ObservableList<User> usersModel = FXCollections.observableArrayList();
    ObservableList<Friendship> friendshipsModel = FXCollections.observableArrayList();
    ObservableList<FriendRequest> friendrequestsModel = FXCollections.observableArrayList();
    @FXML
    TableView<Friendship> friendshipTableView;
    @FXML
    TableColumn<Friendship, User> user1Column;
    @FXML
    TableColumn<Friendship, User> user2Column;
    @FXML
    TableColumn<Friendship, LocalDateTime> friendsFromColumn;
    @FXML
    TableView<FriendRequest> friendRequestTableView;
    @FXML
    TableColumn<FriendRequest, User> frUser1Column;
    @FXML
    TableColumn<FriendRequest, User> frUser2Column;
    @FXML
    TableColumn<FriendRequest, FriendRequestStatus> statusColumn;

    private int currentPageUsers = 0;
    private int currentPageFriendships = 0;
    private int currentPageFriendRequests = 0;
    private int currentPageMessages = 0;
    private int pageSize = 0;
    private int totalNumberOfElements = 0;

    @Override
    public void update(UserChangeEvent t) {
        updateUsersTable();
    }

    private void initModelUsers() {
        Page<User> pageUsers = service.findAllUsers(new Pageable(currentPageUsers, pageSize));
        int maxPageUsers = (int) Math.ceil((double) pageUsers.getTotalElementCount() / pageSize) - 1;
        if (currentPageUsers > maxPageUsers) {
            currentPageUsers = maxPageUsers;
            pageUsers = service.findAllUsers(new Pageable(currentPageUsers, pageSize));
        }
        usersModel.setAll(StreamSupport.stream(pageUsers.getElementsOnPage().spliterator(),
                false).collect(Collectors.toList()));
    }
    private void initModelFriendships(){
        Page<Friendship> pageFriendships = service.findAllFriendships(new Pageable(currentPageFriendships, pageSize));
        int maxPageFriendships = (int) Math.ceil((double) pageFriendships.getTotalElementCount() / pageSize) - 1;
        if (currentPageFriendships > maxPageFriendships) {
            currentPageFriendships = maxPageFriendships;
            pageFriendships = service.findAllFriendships(new Pageable(currentPageFriendships, pageSize));
        }
        friendshipsModel.setAll(StreamSupport.stream(pageFriendships.getElementsOnPage().spliterator(),
                false).collect(Collectors.toList()));
    }

    private void initModelFriendRequests(){
        Page<FriendRequest> pageFriendRequests = service.findAllFriendRequests(new Pageable(currentPageFriendRequests, pageSize));
        int maxPageFriendRequest = (int) Math.ceil((double) pageFriendRequests.getTotalElementCount() / pageSize) - 1;
        if (currentPageFriendRequests > maxPageFriendRequest) {
            currentPageFriendRequests = maxPageFriendRequest;
            pageFriendRequests = service.findAllFriendRequests(new Pageable(currentPageFriendRequests, pageSize));
        }
        friendrequestsModel.setAll(StreamSupport.stream(pageFriendRequests.getElementsOnPage().spliterator(),
                false).collect(Collectors.toList()));
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userTableView.setItems(usersModel);
        friendshipTableView.setItems(friendshipsModel);
        friendRequestTableView.setItems(friendrequestsModel);

        String url = "jdbc:postgresql://localhost:5432/socialnetwork";
        String username = "postgres";
        String password;
        {
            password = "a";
        }

        //UserDBRepository userRepository = new UserDBRepository(url, username, password);
        PagingRepository<Long, User> userRepository = new UserDBRepository(url, username, password);
        UserValidator userValidator = new UserValidator();

        PagingRepository<Long, Friendship> friendshipRepository = new FriendshipDBRepository(url, username, password, userRepository);
        ValidatorFriendship friendshipValidator = new ValidatorFriendship();

        PagingRepository<Long, FriendRequest> friendRequestDBRepository = new FriendRequestDBRepository(url, username, password, userRepository);
        PagingRepository<Long, Message> messageDBRepository = new MessageDBRepository(url, username, password, userRepository);

        this.service = new Service(userRepository, userValidator, friendshipRepository, friendshipValidator, friendRequestDBRepository, messageDBRepository);

        //init table users
        idUser.setCellValueFactory(new PropertyValueFactory<>("id"));
        firstNameUser.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameUser.setCellValueFactory(new PropertyValueFactory<>("lastName"));

        userTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        //init table friendships
        user1Column.setCellValueFactory(new PropertyValueFactory<>("user1"));
        user2Column.setCellValueFactory(new PropertyValueFactory<>("user2"));
        friendsFromColumn.setCellValueFactory(new PropertyValueFactory<>("friendsFrom"));
        this.service.addObserver(this);

        //init table friendrequests
        frUser1Column.setCellValueFactory(new PropertyValueFactory<>("user1"));
        frUser2Column.setCellValueFactory(new PropertyValueFactory<>("user2"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        //paging repo
        Scanner scanner = new Scanner(System.in);
        System.out.println("Introduceti numarul maxim de elemente de pe o pagina:");
        pageSize = scanner.nextInt();
        //initModel();
        initModelUsers();
        initModelFriendships();
        initModelFriendRequests();
        //updateUsersTable();
        //updateFriendshipsTable();
        ///updateFriendRequestsTable();
    }

    private void updateFriendshipsTable() {
        friendshipsModel.setAll(StreamSupport.stream(service.getAllFriendships().spliterator(), false).collect(Collectors.toList()));
        initModelFriendships();
    }

    private void updateUsersTable() {
        usersModel.setAll(StreamSupport.stream(service.getAllUsers().spliterator(), false).collect(Collectors.toList()));
        initModelUsers();
    }

    private void updateFriendRequestsTable() {
        friendrequestsModel.setAll(StreamSupport.stream(service.getAllFriendRequests().spliterator(), false).collect(Collectors.toList()));
        initModelFriendRequests();
    }


    public void onDeleteUserButton() {
        //User selectedUser = userTableView.getSelectionModel().getSelectedItem();
        ObservableList<User> selectedUsers = userTableView.getSelectionModel().getSelectedItems();
        if (selectedUsers.isEmpty()) {
            MessageAlert.showErrorMessage(null, "No user selected!");
            return;
        }
        List<User> users = new ArrayList<>(selectedUsers);
        users.forEach(user -> service.removeUser(user.getId()));
        //service.removeUser(selectedUser.getId());
        //updateUsersTable();
        updateFriendshipsTable();
    }

    public void onDeleteFriendshipButton() {
        Friendship selectedFriendship = friendshipTableView.getSelectionModel().getSelectedItem();

        if (selectedFriendship == null) {
            MessageAlert.showErrorMessage(null, "No friendship selected!");
            return;
        }
        service.removeFriendship(selectedFriendship.getIdUser1(), selectedFriendship.getIdUser2());
        updateFriendshipsTable();

    }

    public void onAddUserButton() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("addUser-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 560, 270);
        ControllerAdd controllerAdd = fxmlLoader.getController();
        controllerAdd.init(service);
        Stage addUserStage = new Stage();
        addUserStage.setTitle("ADD User");
        addUserStage.setScene(scene);
        addUserStage.show();
    }

    public void onModifyUserButton() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("modifyUser-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 560, 270);
        ControllerModify controllerModify = fxmlLoader.getController();
        controllerModify.init(service);
        Stage stage = new Stage();
        stage.setTitle("Modify User");
        stage.setScene(scene);
        stage.show();
    }

    public void onSendMessage() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader((HelloApplication.class.getResource("sendMessage-view.fxml")));
        Scene scene = new Scene(fxmlLoader.load(), 1220, 370);
        ControllerMessage controllerMessage = fxmlLoader.getController();
        controllerMessage.init(service, pageSize);
        Stage stage = new Stage();
        stage.setTitle("Message");
        stage.setScene(scene);
        stage.show();
    }

    public void onSendFriendRequestButton() {
        ObservableList<User> selectedUsers = userTableView.getSelectionModel().getSelectedItems();
        if (selectedUsers.size() != 2) {
            MessageAlert.showErrorMessage(null, "Select 2 users!");
            return;
        }
        User user1 = selectedUsers.get(0);
        User user2 = selectedUsers.get(1);
        try {
            service.addFriendRequest(user1.getId(), user2.getId());
        } catch (ValidationException e) {
            MessageAlert.showErrorMessage(null, e.getMessage());
        }
        updateFriendRequestsTable();

    }

    public void onAcceptFriendRequestButton() {
        FriendRequest friendRequest = friendRequestTableView.getSelectionModel().getSelectedItem();
        if (friendRequest == null) {
            MessageAlert.showErrorMessage(null, "No friendrequest selected!");
            return;
        }
        service.acceptFriendRequest(friendRequest.getId());
        updateFriendRequestsTable();

        PauseTransition pause = new PauseTransition(Duration.millis(50));
        pause.setOnFinished(event -> {
            service.removeFriendRequest(friendRequest.getId());
            updateFriendRequestsTable();
            updateFriendshipsTable();
        });
        pause.play();

    }

    public void onDeclineFriendRequestButton() {
        FriendRequest friendRequest = friendRequestTableView.getSelectionModel().getSelectedItem();
        if (friendRequest == null) {
            MessageAlert.showErrorMessage(null, "No friendrequest selected!");
            return;
        }
        service.rejectFriendRequest(friendRequest.getId());
        updateFriendRequestsTable();

        PauseTransition pause = new PauseTransition(Duration.millis(50));
        pause.setOnFinished(event -> {
            service.removeFriendRequest(friendRequest.getId());
            updateFriendRequestsTable();
            updateFriendshipsTable();
        });
        pause.play();

    }

    public void onPreviousUser(ActionEvent actionEvent) {
        //if(currentPageUsers > 0)
            currentPageUsers--;

        initModelUsers();
    }

    public void onNextUser(ActionEvent actionEvent) {
        //if(currentPageUsers <= pageSize)
            currentPageUsers++;

        initModelUsers();
    }

    public void onPreviousFriendRequest(ActionEvent actionEvent) {
        //if(currentPageFriendRequests > 0)
            currentPageFriendRequests--;
        initModelFriendRequests();
    }

    public void onNextFriendRequest(ActionEvent actionEvent) {
        //if(currentPageFriendRequests <= pageSize)
            currentPageFriendRequests++;
        initModelFriendRequests();
    }

    public void onPreviousFriendships(ActionEvent actionEvent){
        //if(currentPageFriendships > 0)
            currentPageFriendships--;
        initModelFriendships();
    }

    public void onNextFriendships(ActionEvent actionEvent){
        //if(currentPageFriendships <= pageSize)
            currentPageFriendships++;
        initModelFriendships();
    }
}