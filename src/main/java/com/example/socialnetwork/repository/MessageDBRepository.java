package com.example.socialnetwork.repository;

import com.example.socialnetwork.domain.Message;
import com.example.socialnetwork.domain.User;
import com.example.socialnetwork.domain.validators.ValidationException;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class MessageDBRepository implements PagingRepository<Long, Message> {
    private String url;
    private String username;
    private String password;
    private PagingRepository<Long, User> userRepo;

    public MessageDBRepository(String url, String username, String password, PagingRepository<Long, User> userRepo) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.userRepo = userRepo;
    }

    @Override
    public Optional<Message> findOne(Long aLong) {
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("select * from messages " +
                     "where id = ?");

        ) {
            statement.setInt(1, Math.toIntExact(aLong));
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {

                Long fromUserid = resultSet.getLong("from_userid");
                Long toUserid = resultSet.getLong("to_userid");
                String text = resultSet.getString("messagetext");
                LocalDateTime dateTime = resultSet.getTimestamp("date_time").toLocalDateTime();
                Long idreply = resultSet.getLong("id_reply");

                Optional<User> fromUser = userRepo.findOne(fromUserid);
                Optional<User> toUser = userRepo.findOne(toUserid);
                Message message = new Message(fromUser.get(), toUser.get(), text, dateTime, idreply);
                message.setId(aLong);
                return Optional.of(message);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return Optional.empty();
    }

    @Override
    public Page<Message> findAll(Pageable pageable) {
        List<Message> messages = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(this.url, this.username, this.password);
             PreparedStatement pagePreparedStatement = connection.prepareStatement("SELECT * FROM messages " +
                     "LIMIT ? OFFSET ?");

             PreparedStatement countPreparedStatement = connection.prepareStatement
                     ("SELECT COUNT(*) AS count FROM messages ");

        ) {
            pagePreparedStatement.setInt(1, pageable.getPageSize());
            pagePreparedStatement.setInt(2, pageable.getPageSize() * pageable.getPageNumber());
            try (ResultSet pageResultSet = pagePreparedStatement.executeQuery();
                 ResultSet countResultSet = countPreparedStatement.executeQuery();) {
                while (pageResultSet.next()) {
                    Long id = pageResultSet.getLong("id");
                    Long fromUserid = pageResultSet.getLong("from_userid");
                    Long toUserid = pageResultSet.getLong("to_userid");
                    String text = pageResultSet.getString("messagetext");
                    LocalDateTime dateTime = pageResultSet.getTimestamp("date_time").toLocalDateTime();
                    Long idreply = pageResultSet.getLong("id_reply");

                    Optional<User> fromUser = userRepo.findOne(fromUserid);
                    Optional<User> toUser = userRepo.findOne(toUserid);
                    Message message = new Message(fromUser.get(), toUser.get(), text, dateTime, idreply);
                    message.setId(id);

                    messages.add(message);
                }

                int totalCount = 0;
                if (countResultSet.next()) {
                    totalCount = countResultSet.getInt("count");
                }

                return new Page<>(messages, totalCount);

            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Iterable<Message> findAll() {
        Set<Message> messages = new HashSet<>();

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("select * from messages");
             ResultSet resultSet = statement.executeQuery()
        ) {

            while (resultSet.next()) {
                Long id = resultSet.getLong("id");
                Long fromUserid = resultSet.getLong("from_userid");
                Long toUserid = resultSet.getLong("to_userid");
                String text = resultSet.getString("messagetext");
                LocalDateTime dateTime = resultSet.getTimestamp("date_time").toLocalDateTime();
                Long idreply = resultSet.getLong("id_reply");

                Optional<User> fromUser = userRepo.findOne(fromUserid);
                Optional<User> toUser = userRepo.findOne(toUserid);
                Message message = new Message(fromUser.get(), toUser.get(), text, dateTime, idreply);
                message.setId(id);

                messages.add(message);

            }
            return messages;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public Optional<Message> save(Message entity) {
        if (entity.getIdreply() != null && findOne(entity.getIdreply()).isEmpty()) {
            throw new ValidationException("Nu poti da reply la un mesaj inexistent!");
        }
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("insert into messages(from_userid, to_userid, messagetext, date_time, id_reply)" +
                     "values(?, ?, ?, ?, ?)");
        ) {
            statement.setLong(1, entity.getFrom().getId());
            statement.setLong(2, entity.getTo().getId());
            statement.setString(3, entity.getText());
            statement.setTimestamp(4, Timestamp.valueOf(entity.getDateTime()));
            statement.setObject(5, entity.getIdreply(), Types.BIGINT);
            int affectedRows = statement.executeUpdate();
            return affectedRows != 0 ? Optional.empty() : Optional.of(entity);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Message> delete(Long aLong) {
        return Optional.empty();
    }

    @Override
    public Optional<Message> update(Message entity) {
        return Optional.empty();
    }

    public Iterable<Message> getConversation(Long id1, Long id2) {
        List<Message> messages = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("select * from messages " +
                     "where (from_userid = ? and to_userid = ?) or (from_userid = ? and to_userid = ?) " +
                     "order by date_time");
        ) {
            statement.setLong(1, id1);
            statement.setLong(2, id2);
            statement.setLong(3, id2);
            statement.setLong(4, id1);

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Long id = resultSet.getLong("id");
                Long fromUserid = resultSet.getLong("from_userid");
                Long toUserid = resultSet.getLong("to_userid");
                String text = resultSet.getString("messagetext");
                LocalDateTime dateTime = resultSet.getTimestamp("date_time").toLocalDateTime();
                Long idreply = resultSet.getLong("id_reply");

                Optional<User> fromUser = userRepo.findOne(fromUserid);
                Optional<User> toUser = userRepo.findOne(toUserid);
                Message message = new Message(fromUser.get(), toUser.get(), text, dateTime, idreply);
                message.setId(id);

                messages.add(message);

            }
            return messages;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}