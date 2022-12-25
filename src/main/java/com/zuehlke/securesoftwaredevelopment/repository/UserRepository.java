package com.zuehlke.securesoftwaredevelopment.repository;

import com.zuehlke.securesoftwaredevelopment.config.AuditLogger;
import com.zuehlke.securesoftwaredevelopment.domain.Person;
import com.zuehlke.securesoftwaredevelopment.domain.User;
import com.zuehlke.securesoftwaredevelopment.exception.InternalServerError;
import com.zuehlke.securesoftwaredevelopment.exception.InvalidArgumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@Repository
public class UserRepository {

    private static final Logger LOG = LoggerFactory.getLogger(UserRepository.class);
    private static final AuditLogger auditLogger = AuditLogger.getAuditLogger(UserRepository.class);

    private DataSource dataSource;

    public UserRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public User findUser(String username) {
        LOG.debug("Getting user with username" + username);
        String query = "SELECT id, username, password FROM users WHERE username='" + username + "'";
        return findUserWithQuery(query);
    }

    private User findUserById(int userId) {
        LOG.debug("Getting user with id" + userId);
        String query = "SELECT id, username, password FROM users WHERE id='" + userId + "'";
        return findUserWithQuery(query);
    }

    private User findUserWithQuery(String query) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(query)) {
            if (rs.next()) {
                int id = rs.getInt(1);
                String username1 = rs.getString(2);
                String password = rs.getString(3);
                return new User(id, username1, password);
            }
        } catch (SQLException e) {
            LOG.error("Failed to get user: " + e.getMessage());
            throw new InternalServerError();
        }

        return null;
    }

    public boolean validCredentials(String username, String password) {
        String query = "SELECT username FROM users WHERE username='" + username + "' AND password='" + password + "'";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(query)) {
            return rs.next();
        } catch (SQLException e) {
            LOG.error("Failed to get user: " + e.getMessage());
            throw new InternalServerError();
        }
    }

    public void delete(int userId) {
        LOG.info("Deleting user " + userId);

        User userFromDb = findUserById(userId);
        if (userFromDb == null) throw new InvalidArgumentException();

        String query = "DELETE FROM users WHERE id = " + userId;
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
        ) {
            statement.executeUpdate(query);
            auditLogger.audit("Successfully deleted user " + userFromDb);
        } catch (SQLException e) {
            LOG.warn("Failed to delete user: " + e.getMessage());
            throw new InvalidArgumentException();
        }
    }
}
