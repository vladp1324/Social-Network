package com.example.socialnetwork.repository;

import com.example.socialnetwork.domain.FriendRequest;
import com.example.socialnetwork.domain.FriendRequestStatus;
import com.example.socialnetwork.domain.User;
import com.example.socialnetwork.domain.validators.ValidationException;

import java.sql.*;
import java.util.*;

public class FriendRequestDBRepository implements PagingRepository<Long, FriendRequest> {
    private String url;
    private String username;
    private String password;
    private PagingRepository<Long, User> userRepo;

    public FriendRequestDBRepository(String url, String username, String password, PagingRepository<Long, User> userRepo) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.userRepo = userRepo;
    }

    @Override
    public Optional<FriendRequest> findOne(Long longID) {
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("select * from friendrequests " +
                     "where id = ?");
        ) {
            statement.setInt(1, Math.toIntExact(longID));
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                Long user1Id = resultSet.getLong("user1_id");
                Long user2Id = resultSet.getLong("user2_id");
                FriendRequestStatus status = FriendRequestStatus.valueOf(resultSet.getString("status"));

                Optional<User> user1 = userRepo.findOne(user1Id);
                Optional<User> user2 = userRepo.findOne(user2Id);
                FriendRequest friendRequest = new FriendRequest(user1.get(), user2.get(), status);
                friendRequest.setId(longID);

                return Optional.of(friendRequest);

            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return Optional.empty();
    }

    @Override
    public Page<FriendRequest> findAll(Pageable pageable) {
        List<FriendRequest> friendRequests = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(this.url, this.username, this.password);
             PreparedStatement pagePreparedStatement = connection.prepareStatement("SELECT * FROM friendrequests " +
                     "LIMIT ? OFFSET ?");

             PreparedStatement countPreparedStatement = connection.prepareStatement
                     ("SELECT COUNT(*) AS count FROM friendrequests ");

        ) {
            pagePreparedStatement.setInt(1, pageable.getPageSize());
            pagePreparedStatement.setInt(2, pageable.getPageSize() * pageable.getPageNumber());
            try (ResultSet pageResultSet = pagePreparedStatement.executeQuery();
                 ResultSet countResultSet = countPreparedStatement.executeQuery();) {
                while (pageResultSet.next()) {
                    Long id = pageResultSet.getLong("id");
                    Long user1Id = pageResultSet.getLong("user1_id");
                    Long user2Id = pageResultSet.getLong("user2_id");
                    FriendRequestStatus status = FriendRequestStatus.valueOf(pageResultSet.getString("status"));

                    Optional<User> user1 = userRepo.findOne(user1Id);
                    Optional<User> user2 = userRepo.findOne(user2Id);
                    FriendRequest friendRequest = new FriendRequest(user1.get(), user2.get(), status);
                    friendRequest.setId(id);
                    friendRequests.add(friendRequest);
                }

                int totalCount = 0;
                if (countResultSet.next()) {
                    totalCount = countResultSet.getInt("count");
                }

                return new Page<>(friendRequests, totalCount);

            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Iterable<FriendRequest> findAll() {
        Set<FriendRequest> friendRequests = new HashSet<>();

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("select * from friendrequests");
             ResultSet resultSet = statement.executeQuery()
        ) {

            while (resultSet.next()) {
                Long id = resultSet.getLong("id");
                Long user1Id = resultSet.getLong("user1_id");
                Long user2Id = resultSet.getLong("user2_id");
                FriendRequestStatus status = FriendRequestStatus.valueOf(resultSet.getString("status"));

                Optional<User> user1 = userRepo.findOne(user1Id);
                Optional<User> user2 = userRepo.findOne(user2Id);
                FriendRequest friendRequest = new FriendRequest(user1.get(), user2.get(), status);
                friendRequest.setId(id);
                friendRequests.add(friendRequest);

            }

            return friendRequests;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public Optional<FriendRequest> save(FriendRequest entity) {
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("insert into friendrequests(user1_id, user2_id, status)" +
                     "values(?, ?, ?)");
        ) {
            statement.setLong(1, entity.getUser1().getId());
            statement.setLong(2, entity.getUser2().getId());
            statement.setString(3, entity.getStatus().toString());
            int affectedRows = statement.executeUpdate();

            return affectedRows != 0 ? Optional.empty() : Optional.of(entity);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<FriendRequest> delete(Long id) {
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("delete from friendrequests " +
                     "where id = ?");
        ) {
            statement.setLong(1, id);
            Optional<FriendRequest> friendRequest = findOne(id);
            if (friendRequest.isPresent()) {
                int affectedRows = statement.executeUpdate();

                return affectedRows == 0 ? Optional.empty() : friendRequest;
            } else {
                throw new ValidationException("Nu exista aceasta prietenie in baza de date!");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<FriendRequest> update(FriendRequest entity) {
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(
                     "update friendrequests set user1_id=?,user2_id=?,status=? where id=?");
        ) {
            statement.setLong(1, entity.getUser1().getId());
            statement.setLong(2, entity.getUser2().getId());
            statement.setString(3, entity.getStatus().toString());
            statement.setLong(4, entity.getId());

            int affectedRows = statement.executeUpdate();
            return affectedRows == 0 ? Optional.of(entity) : Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<FriendRequest> findFriendRequest(Long from, Long to) {
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("select * from friendrequests where user1_id = ? and user2_id = ?");
        ) {
            statement.setLong(1, from);
            statement.setLong(2, to);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                Long ID = resultSet.getLong("id");
                Long user1Id = resultSet.getLong("user1_id");
                Long user2Id = resultSet.getLong("user2_id");
                FriendRequestStatus status = FriendRequestStatus.valueOf(resultSet.getString("status"));

                Optional<User> user1 = userRepo.findOne(user1Id);
                Optional<User> user2 = userRepo.findOne(user2Id);
                if (user1.isEmpty() || user2.isEmpty())
                    throw new ValidationException("This friend request is invalid!");

                FriendRequest friendRequest = new FriendRequest(user1.get(), user2.get(), status);
                friendRequest.setId(ID);

                return Optional.of(friendRequest);

            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return Optional.empty();
    }

    public Iterable<User> findAll(Long id) {
        List<User> friendRequests = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("select * from friendrequests where user2_id = ?");
        ) {
            statement.setLong(1, id);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Long user1Id = resultSet.getLong("user1_id");
                Optional<User> user1 = userRepo.findOne(user1Id);

                if (user1.isEmpty())
                    throw new ValidationException("Invalid user!");

                friendRequests.add(user1.get());

            }

            return friendRequests;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteFriendRequest(Long id1, Long id2) {
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("delete from friendrequests " +
                     "where (user1_id = ? and user2_id = ?) or (user1_id = ? and user2_id = ?)");
        ) {
            statement.setLong(1, id1);
            statement.setLong(2, id2);
            statement.setLong(3, id2);
            statement.setLong(4, id1);

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0)
                throw new ValidationException("This friend request is invalid");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}