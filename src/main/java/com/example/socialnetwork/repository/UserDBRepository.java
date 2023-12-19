package com.example.socialnetwork.repository;

import com.example.socialnetwork.domain.User;
import com.example.socialnetwork.domain.validators.ValidationException;

import java.sql.*;
import java.util.*;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class UserDBRepository implements PagingRepository<Long, User>{
    private String url;
    private String username;
    private String password;

    public UserDBRepository(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    @Override
    public Optional<User> findOne(Long longID) {
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("select * from users " +
                     "where id = ?");

        ) {
            statement.setInt(1, Math.toIntExact(longID));
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                User u = new User(firstName, lastName);
                u.setId(longID);
                return Optional.ofNullable(u);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return Optional.empty();
    }
    @Override
    public Page<User> findAll(Pageable pageable) {
        List<User> users = new ArrayList<>();
        try(Connection connection= DriverManager.getConnection(this.url,this.username,this.password);
            PreparedStatement pagePreparedStatement=connection.prepareStatement("SELECT * FROM users " +
                    "LIMIT ? OFFSET ?");

            PreparedStatement countPreparedStatement = connection.prepareStatement
                    ("SELECT COUNT(*) AS count FROM users ");

        ) {
            pagePreparedStatement.setInt(1, pageable.getPageSize());
            pagePreparedStatement.setInt(2, pageable.getPageSize() * pageable.getPageNumber());
            try (ResultSet pageResultSet = pagePreparedStatement.executeQuery();
                 ResultSet countResultSet = countPreparedStatement.executeQuery(); ) {
                while (pageResultSet.next()) {
                    Long id = pageResultSet.getLong("id");
                    String firstName = pageResultSet.getString("first_name");
                    String lastName = pageResultSet.getString("last_name");
                    User user = new User(firstName, lastName);
                    user.setId(id);
                    users.add(user);
                }
                int totalCount = 0;
                if(countResultSet.next()) {
                    totalCount = countResultSet.getInt("count");
                }

                return new Page<>(users, totalCount);

            }
        }
        catch (SQLException e){
            throw new RuntimeException(e);
        }
    }
    @Override
    public Iterable<User> findAll() {
        Set<User> users = new HashSet<>();

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("select * from users");
             ResultSet resultSet = statement.executeQuery()
        ) {

            while (resultSet.next()) {
                Long id = resultSet.getLong("id");
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                User user = new User(firstName, lastName);
                user.setId(id);
                users.add(user);

            }
            return users;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public Optional<User> save(User entity) {
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("insert into users(first_name, last_name)" +
                     "values(?, ?)");
        ) {
            statement.setString(1, entity.getFirstName());
            statement.setString(2, entity.getLastName());
            int affectedRows = statement.executeUpdate();
            return affectedRows != 0 ? Optional.empty() : Optional.of(entity);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<User> delete(Long id) {
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("delete from users " +
                     "where id = ?");
        ) {
            statement.setLong(1, id);
            Optional<User> user = findOne(id);
            if(user.isPresent()) {
                int affectedRows = statement.executeUpdate();
                return affectedRows == 0 ? Optional.empty() : user;
            }
            else{
                throw new ValidationException("Nu exista acest utilizator in baza de date!");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<User> update(User entity) {
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("update users set first_name=?,last_name=? where id=?");
        ) {
            statement.setString(1, entity.getFirstName());
            statement.setString(2, entity.getLastName());
            statement.setLong(3, entity.getId());
            int affectedRows = statement.executeUpdate();
            return affectedRows == 0?Optional.of(entity):Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}

/*public class UserDBRepository implements Repository<Long, User> {

    private String url;
    private String username;
    private String password;

//    private Validator<User> validator;


    public UserDBRepository(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    @Override
    public Optional<User> findOne(Long longID) {
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("select * from users " +
                     "where id = ?");

        ) {
            statement.setInt(1, Math.toIntExact(longID));
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                User u = new User(firstName, lastName);
                u.setId(longID);
                return Optional.ofNullable(u);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return Optional.empty();
    }

    @Override
    public Iterable<User> findAll() {
        Set<User> users = new HashSet<>();

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("select * from users");
             ResultSet resultSet = statement.executeQuery()
        ) {

            while (resultSet.next()) {
                Long id = resultSet.getLong("id");
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                User user = new User(firstName, lastName);
                user.setId(id);
                users.add(user);

            }
            return users;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public Optional<User> save(User entity) {
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("insert into users(first_name, last_name)" +
                     "values(?, ?)");
        ) {
            statement.setString(1, entity.getFirstName());
            statement.setString(2, entity.getLastName());
            int affectedRows = statement.executeUpdate();
            return affectedRows != 0 ? Optional.empty() : Optional.of(entity);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<User> delete(Long id) {
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("delete from users " +
                     "where id = ?");
        ) {
            statement.setLong(1, id);
            Optional<User> user = findOne(id);
            if(user.isPresent()) {
                int affectedRows = statement.executeUpdate();
                return affectedRows == 0 ? Optional.empty() : user;
            }
            else{
                throw new ValidationException("Nu exista acest utilizator in baza de date!");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<User> update(User entity) {
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("update users set first_name=?,last_name=? where id=?");
        ) {
            statement.setString(1, entity.getFirstName());
            statement.setString(2, entity.getLastName());
            statement.setLong(3, entity.getId());
            int affectedRows = statement.executeUpdate();
            return affectedRows == 0?Optional.of(entity):Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
*/