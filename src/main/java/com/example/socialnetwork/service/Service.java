package com.example.socialnetwork.service;

import com.example.socialnetwork.domain.*;
import com.example.socialnetwork.domain.validators.UserValidator;
import com.example.socialnetwork.domain.validators.ValidationException;
import com.example.socialnetwork.domain.validators.ValidatorFriendship;
import com.example.socialnetwork.repository.*;
import com.example.socialnetwork.utils.ChangeEventType;
import com.example.socialnetwork.utils.Observer;
import com.example.socialnetwork.utils.UserChangeEvent;
import com.example.socialnetwork.utils.Observable;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Service implements ServiceInterface, Observable<UserChangeEvent> {
    private UserDBRepository repoUsers;
    private UserValidator userValidator;
    private FriendshipDBRepository repoFriendships;
    private ValidatorFriendship validatorFriendship;
    private FriendRequestDBRepository repoRequests;
    private MessageDBRepository repoMessages;
    private List<com.example.socialnetwork.utils.Observer<UserChangeEvent>> observers = new ArrayList<>();

    public Page<User> findAllUsers(Pageable pageable) {
        return repoUsers.findAll(pageable);
    }

    public Page<Friendship> findAllFriendships(Pageable pageable) {
        return repoFriendships.findAll(pageable);
    }

    public Page<FriendRequest> findAllFriendRequests(Pageable pageable) {
        return repoRequests.findAll(pageable);
    }

    public Page<Message> findAllMessages(Pageable pageable) {
        return repoMessages.findAll(pageable);
    }

    @Override
    public void addObserver(com.example.socialnetwork.utils.Observer<UserChangeEvent> o) {
        observers.add(o);
    }

    @Override
    public void removeObserver(Observer<UserChangeEvent> o) {
        observers.remove(o);
    }

    @Override
    public void notify(UserChangeEvent t) {
        observers.forEach(o -> o.update(t));
    }

    public Service(UserDBRepository repoUsers, UserValidator userValidator, FriendshipDBRepository repoFriendships, ValidatorFriendship validatorFriendship, FriendRequestDBRepository repoRequests, MessageDBRepository repoMessages) {
        this.repoUsers = repoUsers;
        this.userValidator = userValidator;
        this.repoFriendships = repoFriendships;
        this.validatorFriendship = validatorFriendship;
        this.repoRequests = repoRequests;
        this.repoMessages = repoMessages;
    }

    public Iterable<Message> showConversation(Long id1, Long id2) {
        return repoMessages.getConversation(id1, id2);
    }

    public Iterable<User> getAllFriendRequests(Long id) {
        return repoRequests.findAll(id);
    }

    public Iterable<User> getAllFriends(Long id) {
        return repoFriendships.findAllFriends(id);

    }

    public Optional<User> login(String username, String password) {
        return repoUsers.findUserByUsernamePassword(username, password);
    }

    private static <T> int size(Iterable<T> it) {
        AtomicInteger count = new AtomicInteger();
        it.forEach(i -> {
            count.getAndIncrement();
        });
        return count.get();
    }

    @Override
    public Optional<User> addUser(String firstName, String lastName, String username, String password) {
        User u = new User(firstName, lastName, username, password);
        userValidator.validate(u);
        repoUsers.save(u);
        notify(new UserChangeEvent(ChangeEventType.ADD, u));
        return Optional.of(u);
    }

    private Optional<FriendRequest> findFriendRequest(Long id1, Long id2) {
        return StreamSupport.stream(getAllFriendRequests().spliterator(), false)
                .filter(friendRequest -> (Objects.equals(id1, friendRequest.getUser1().getId()) && Objects.equals(id2, friendRequest.getUser2().getId())) ||
                        (Objects.equals(id2, friendRequest.getUser1().getId()) && Objects.equals(id1, friendRequest.getUser2().getId())))
                .findFirst();
    }

    public void addFriendRequest(Long id1, Long id2) {
        Optional<User> user1 = repoUsers.findOne(id1);
        Optional<User> user2 = repoUsers.findOne(id2);

        if (user1.isEmpty() || user2.isEmpty())
            throw new ValidationException("Invalid users!");

        if (Objects.equals(id1, id2))
            throw new ValidationException("You can't send friend request to yourself!");

        if (findFriendship(id1, id2).isPresent()) {
            throw new ValidationException("This is already your friend!");
        }

        if (repoRequests.findFriendRequest(id1, id2).isPresent()) {
            throw new ValidationException("Wait for this user to see your friend request!");
        }

        if (repoRequests.findFriendRequest(id2, id1).isPresent()) {
            acceptFriendRequest(id1, id2);
            return;
        }

        FriendRequest friendRequest = new FriendRequest(user1.get(), user2.get());
        repoRequests.save(friendRequest);
    }

    public void acceptFriendRequest(Long id1, Long id2) {
        repoRequests.deleteFriendRequest(id1, id2);
        LocalDateTime friendsFrom = LocalDateTime.now();

        Optional<User> user1 = repoUsers.findOne(id1);
        Optional<User> user2 = repoUsers.findOne(id2);
        if (user1.isEmpty() || user2.isEmpty())
            throw new ValidationException("This users doesn't exist!");

        Friendship friendship = new Friendship(user1.get(), user2.get(), friendsFrom);
        repoFriendships.save(friendship);
    }

    public void rejectFriendRequest(long id1, long id2) {
        repoRequests.deleteFriendRequest(id1, id2);
    }

    public void acceptFriendRequest(Long id) {
        Optional<FriendRequest> friendRequest = repoRequests.findOne(id);
        friendRequest.get().setStatus(FriendRequestStatus.APPROVED);
        repoRequests.update(friendRequest.get());

        LocalDateTime friendsFrom = LocalDateTime.now();
        Friendship friendship = new Friendship(friendRequest.get().getUser1(), friendRequest.get().getUser2(), friendsFrom);

        repoFriendships.save(friendship);
    }

    public void rejectFriendRequest(Long id) {
        Optional<FriendRequest> friendRequest = repoRequests.findOne(id);
        friendRequest.get().setStatus(FriendRequestStatus.REJECTED);
        repoRequests.update(friendRequest.get());
    }


    public void sendMessage(Long idFrom, Long idTo, String text, LocalDateTime localDateTime) {
        Optional<User> fromUser = repoUsers.findOne(idFrom);
        Optional<User> toUser = repoUsers.findOne(idTo);
        if (fromUser.isPresent() && toUser.isPresent()) {
            Message message = new Message(fromUser.get(), toUser.get(), text, localDateTime);
            repoMessages.save(message);
        } else {
            throw new ValidationException("This users doesn't exist!");
        }
    }

    public void replyMessage(Long idFrom, Long idTo, String text, Long idReply) {
        Optional<User> fromUser = repoUsers.findOne(idFrom);
        Optional<User> toUser = repoUsers.findOne(idTo);
        if (fromUser.isPresent() && toUser.isPresent()) {
            Message message = new Message(fromUser.get(), toUser.get(), text, idReply);
            try {
                repoMessages.save(message);
            } catch (ValidationException e) {
                throw e;
            }
        } else {
            throw new ValidationException("Nu exista acesti utilizatori in baza de date!");
        }
    }

    @Override
    public Iterable<Message> getAllMessages() {
        return repoMessages.findAll();
    }

    public Iterable<Message> getConversation(Long id1, Long id2) {
        return StreamSupport.stream(getAllMessages().spliterator(), false)
                .filter(message -> (Objects.equals(message.getFrom().getId(), id1) && Objects.equals(message.getTo().getId(), id2)) ||
                        (Objects.equals(message.getFrom().getId(), id2) && Objects.equals(message.getTo().getId(), id1)))
                .sorted(Comparator.comparing(Message::getDateTime))
                .collect(Collectors.toList());

    }

    @Override
    public Optional<User> updateUser(Long id, String first_name, String last_name, String username, String password) {
        Optional<User> oldUser = repoUsers.findOne(id);
        if (oldUser.isEmpty()) {
            throw new ValidationException("This user doesn't exist!");
        }

        if (repoUsers.findUserByUsername(username).isPresent()) {
            throw new ValidationException("This username is already used!");
        }

        if (first_name.isBlank())
            first_name = oldUser.get().getFirstName();
        if (last_name.isBlank())
            last_name = oldUser.get().getLastName();
        if (username.isBlank())
            username = oldUser.get().getUsername();
        if (password.isBlank())
            password = oldUser.get().getPassword();

        User newUser = new User(id, first_name, last_name, username, password);

        Optional<User> result = repoUsers.update(newUser);
        notify(new UserChangeEvent(ChangeEventType.UPDATE, newUser, oldUser.get()));
        return result;

    }

    @Override
    public Optional<User> removeUser(Long id) {
        try {
            Optional<User> user = repoUsers.delete(id);
            user.ifPresent(u -> notify(new UserChangeEvent(ChangeEventType.REMOVE, null, u)));
            return user;
        } catch (ValidationException e) {
            throw e;
        }
    }


    Optional<Friendship> findFriendship(Long id1, Long id2) {
        return StreamSupport.stream(getAllFriendships().spliterator(), false)
                .filter(findFriendship -> (findFriendship.getUser1().getId() == id1 && findFriendship.getUser2().getId() == id2)
                        || (findFriendship.getUser1().getId() == id2 && findFriendship.getUser2().getId() == id1)).findFirst();

    }

    @Override
    public Optional<Friendship> removeFriendship(Long id1, Long id2) {
        Optional<Friendship> friendship = findFriendship(id1, id2);
        if (friendship.isPresent()) {
            return repoFriendships.delete(friendship.get().getId());
        } else {
            throw new ValidationException("This friendships doesn't exist!");
        }
    }

    @Override
    public Iterable<User> getAllUsers() {
        return repoUsers.findAll();
    }

    @Override
    public Iterable<FriendRequest> getAllFriendRequests() {
        return repoRequests.findAll();
    }

    @Override
    public Iterable<Friendship> getAllFriendships() {
        return repoFriendships.findAll();
    }

    private Map<String, Integer> initMonthsMap() {
        Map<String, Integer> luni = new HashMap<>();
        luni.put("ianuarie", 1);
        luni.put("februarie", 2);
        luni.put("martie", 3);
        luni.put("aprilie", 4);
        luni.put("mai", 5);
        luni.put("iunie", 6);
        luni.put("iulie", 7);
        luni.put("august", 8);
        luni.put("septembrie", 9);
        luni.put("octombrie", 10);
        luni.put("noiembrie", 11);
        luni.put("decembrie", 12);
        return luni;
    }

    public void removeFriendRequest(Long id) {
        repoRequests.delete(id);
    }

}