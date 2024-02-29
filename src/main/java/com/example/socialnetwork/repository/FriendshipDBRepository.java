package com.example.socialnetwork.repository;

import com.example.socialnetwork.domain.Friendship;
import com.example.socialnetwork.domain.User;
import com.example.socialnetwork.domain.validators.ValidationException;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class FriendshipDBRepository implements PagingRepository<Long, Friendship> {
    private String url;
    private String username;
    private String password;
    private PagingRepository<Long, User> userRepo;

    public FriendshipDBRepository(String url, String username, String password, PagingRepository<Long, User> userRepo) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.userRepo = userRepo;
    }

    @Override
    public Optional<Friendship> findOne(Long longID) {
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("select * from friendships " +
                     "where id = ?");

        ) {
            statement.setInt(1, Math.toIntExact(longID));
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                Long user1Id = resultSet.getLong("user1_id");
                Long user2Id = resultSet.getLong("user2_id");
                LocalDateTime friendsFrom = resultSet.getTimestamp("friends_from").toLocalDateTime();
                Optional<User> user1 = userRepo.findOne(user1Id);
                Optional<User> user2 = userRepo.findOne(user2Id);
                Friendship friendship = new Friendship(user1.get(), user2.get(), friendsFrom);
                friendship.setId(longID);
                return Optional.of(friendship);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return Optional.empty();
    }


    @Override
    public Page<Friendship> findAll(Pageable pageable) {
        List<Friendship> friendships = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(this.url, this.username, this.password);
             PreparedStatement pagePreparedStatement = connection.prepareStatement("SELECT * FROM friendships " +
                     "LIMIT ? OFFSET ?");

             PreparedStatement countPreparedStatement = connection.prepareStatement
                     ("SELECT COUNT(*) AS count FROM friendships ");

        ) {
            pagePreparedStatement.setInt(1, pageable.getPageSize());
            pagePreparedStatement.setInt(2, pageable.getPageSize() * pageable.getPageNumber());
            try (ResultSet pageResultSet = pagePreparedStatement.executeQuery();
                 ResultSet countResultSet = countPreparedStatement.executeQuery();) {
                while (pageResultSet.next()) {
                    Long id = pageResultSet.getLong("id");
                    Long user1Id = pageResultSet.getLong("user1_id");
                    Long user2Id = pageResultSet.getLong("user2_id");
                    LocalDateTime friendsFrom = pageResultSet.getTimestamp("friends_from").toLocalDateTime();

                    Optional<User> user1 = userRepo.findOne(user1Id);
                    Optional<User> user2 = userRepo.findOne(user2Id);
                    Friendship friendship = new Friendship(user1.get(), user2.get(), friendsFrom);
                    friendship.setId(id);
                    friendships.add(friendship);
                }

                int totalCount = 0;
                if (countResultSet.next()) {
                    totalCount = countResultSet.getInt("count");
                }

                return new Page<>(friendships, totalCount);

            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Iterable<User> findAllFriends(Long idConnectedUser) {
        List<User> friends = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("select * from friendships where user1_id = ? or user2_id = ?");
        ) {
            statement.setLong(1, idConnectedUser);
            statement.setLong(2, idConnectedUser);

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Long user1Id = resultSet.getLong("user1_id");
                Long user2Id = resultSet.getLong("user2_id");

                Optional<User> user1 = userRepo.findOne(user1Id);
                Optional<User> user2 = userRepo.findOne(user2Id);
                if (user1.isPresent() && user2.isPresent()) {
                    User friend = user1.get();
                    if (Objects.equals(friend.getId(), idConnectedUser))
                        friend = user2.get();
                    friends.add(friend);
                }
            }

            return friends;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Iterable<Friendship> findAll() {
        Set<Friendship> friendships = new HashSet<>();

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("select * from friendships");
             ResultSet resultSet = statement.executeQuery()
        ) {

            while (resultSet.next()) {
                Long id = resultSet.getLong("id");
                Long user1Id = resultSet.getLong("user1_id");
                Long user2Id = resultSet.getLong("user2_id");
                Timestamp timestamp = resultSet.getTimestamp("friends_from");
                LocalDateTime friendsFrom = (timestamp != null) ? timestamp.toLocalDateTime() : null;

                Optional<User> user1 = userRepo.findOne(user1Id);
                Optional<User> user2 = userRepo.findOne(user2Id);
                Friendship friendship = new Friendship(user1.get(), user2.get(), friendsFrom);
                friendship.setId(id);
                friendships.add(friendship);

            }
            return friendships;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public Optional<Friendship> save(Friendship entity) {
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("insert into friendships(user1_id, user2_id, friends_from)" +
                     "values(?, ?, ?)");
        ) {
            statement.setLong(1, entity.getUser1().getId());
            statement.setLong(2, entity.getUser2().getId());
            statement.setTimestamp(3, Timestamp.valueOf(entity.getFriendsFrom()));

            int affectedRows = statement.executeUpdate();

            return affectedRows != 0 ? Optional.empty() : Optional.of(entity);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Friendship> delete(Long id) {
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("delete from friendships " +
                     "where id = ?");
        ) {
            statement.setLong(1, id);
            Optional<Friendship> friendship = findOne(id);
            if (friendship.isPresent()) {
                int affectedRows = statement.executeUpdate();

                return affectedRows == 0 ? Optional.empty() : friendship;
            } else {
                throw new ValidationException("Nu exista aceasta prietenie in baza de date!");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Friendship> update(Friendship entity) {
        return Optional.empty();
    }
}