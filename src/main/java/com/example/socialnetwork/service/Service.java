package com.example.socialnetwork.service;

import com.example.socialnetwork.domain.*;
import com.example.socialnetwork.domain.validators.UserValidator;
import com.example.socialnetwork.domain.validators.ValidationException;
import com.example.socialnetwork.domain.validators.ValidatorFriendship;
import com.example.socialnetwork.repository.*;
import com.example.socialnetwork.utils.ChangeEventType;
import com.example.socialnetwork.utils.Observable;
import com.example.socialnetwork.utils.Observer;
import com.example.socialnetwork.utils.UserChangeEvent;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Service implements ServiceInterface, Observable<UserChangeEvent> {
    private PagingRepository<Long, User> repoUsers;
    private UserValidator userValidator;
    private PagingRepository<Long, Friendship> repoFriendships;
    private ValidatorFriendship validatorFriendship;
    private PagingRepository<Long, FriendRequest> repoRequests;
    private PagingRepository<Long, Message> repoMessages;
    private List<Observer<UserChangeEvent>> observers = new ArrayList<>();

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
    public void addObserver(Observer<UserChangeEvent> o) {
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

    public Service(PagingRepository<Long, User> repoUsers, UserValidator userValidator, PagingRepository<Long, Friendship> repoFriendships, ValidatorFriendship validatorFriendship, PagingRepository<Long, FriendRequest> repoRequests, PagingRepository<Long, Message> repoMessages) {
        this.repoUsers = repoUsers;
        this.userValidator = userValidator;
        this.repoFriendships = repoFriendships;
        this.validatorFriendship = validatorFriendship;
        this.repoRequests = repoRequests;
        this.repoMessages = repoMessages;
    }

    private static <T> int size(Iterable<T> it) {
        AtomicInteger count = new AtomicInteger();
        it.forEach(i -> {
            count.getAndIncrement();
        });
        return count.get();
    }

    @Override
    public Optional<User> addUser(String firstName, String lastName) {
        User u = new User(firstName, lastName);
        try {
            userValidator.validate(u);
            repoUsers.save(u);
            notify(new UserChangeEvent(ChangeEventType.ADD, u));
            return Optional.of(u);
        } catch (Exception e) {
            throw e;
        }

    }

    private Optional<FriendRequest> findFriendRequest(Long id1, Long id2) {
        return StreamSupport.stream(getAllFriendRequests().spliterator(), false)
                .filter(friendRequest -> (Objects.equals(id1, friendRequest.getUser1().getId()) && Objects.equals(id2, friendRequest.getUser2().getId())) ||
                        (Objects.equals(id2, friendRequest.getUser1().getId()) && Objects.equals(id1, friendRequest.getUser2().getId())))
                .findFirst();
    }

    public void addFriendRequest(Long id1, Long id2) {

        if (findFriendship(id1, id2).isPresent()) {
            throw new ValidationException("Exista deja relatie de prietenie intre acesti utilizatori");
        }
        if (findFriendRequest(id1, id2).isPresent()) {
            throw new ValidationException("Cererea de prietenie exista deja!");
        }

        Optional<User> user1 = repoUsers.findOne(id1);
        Optional<User> user2 = repoUsers.findOne(id2);
        FriendRequest friendRequest = new FriendRequest(user1.get(), user2.get());
        repoRequests.save(friendRequest);

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
            throw new ValidationException("Nu exista acesti utilizatori in baza de date!");
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
    public Optional<User> updateUser(Long id, String first_name, String last_name) {
        Optional<User> oldUser = repoUsers.findOne(id);
        User newUser = new User(id, first_name, last_name);
        if (oldUser.isPresent()) {
            Optional<User> result = repoUsers.update(newUser);
            userValidator.validate(newUser);
            notify(new UserChangeEvent(ChangeEventType.UPDATE, newUser, oldUser.get()));
            return result;
        } else {
            throw new ValidationException("Nu exista acest utilizator!");
        }
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

    @Override
    public Optional<Friendship> createFriendship(Long id1, Long id2) {
        Optional<User> user1 = repoUsers.findOne(id1);
        Optional<User> user2 = repoUsers.findOne(id2);
        if (user1.isPresent() && user2.isPresent()) {
            try {
                LocalDateTime friendsFrom = LocalDateTime.now();
                Friendship friendship = new Friendship(user1.get(), user2.get(), friendsFrom);
                validatorFriendship.validate(friendship);
                repoFriendships.save(friendship);
                return Optional.of(friendship);
            } catch (ValidationException e) {
                throw e;
            }
        } else {
            throw new ValidationException("Acesti utilizatori nu exista in baza de date!");
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
            throw new ValidationException("Aceasta relatie de prietenie nu exista!");
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

    @Override
    public Iterable<Friendship> getAllFriendshipsByMonth(String luna) {
        Map<String, Integer> luni = initMonthsMap();
        int month = luni.get(luna);
        List<Friendship> friendships = StreamSupport.stream(getAllFriendships().spliterator(), false)
                .filter(friendship -> friendship.getFriendsFrom().getMonthValue() == month)
                .collect(Collectors.toList());
        if (size(friendships) != 0) {
            return friendships;
        } else {
            throw new ValidationException("Nu exista prietenii create in aceasta luna!");
        }
    }

    @Override
    public Iterable<User> getAllMatchingUsers(String sir) {
        List<User> users = StreamSupport.stream(getAllUsers().spliterator(), false)
                .filter(user -> user.getFirstName().contains(sir) || user.getLastName().contains(sir))
                .collect(Collectors.toList());
        if (size(users) != 0) {
            return users;
        } else {
            throw new ValidationException("Nu exista nume sau prenume care sa contina acest subsir de caractere!");
        }
    }

    public void removeFriendRequest(Long id) {
        repoRequests.delete(id);
    }
}