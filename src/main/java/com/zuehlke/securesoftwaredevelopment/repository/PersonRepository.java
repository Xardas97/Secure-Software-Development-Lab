package com.zuehlke.securesoftwaredevelopment.repository;

import com.zuehlke.securesoftwaredevelopment.config.AuditLogger;
import com.zuehlke.securesoftwaredevelopment.config.Entity;
import com.zuehlke.securesoftwaredevelopment.domain.Person;
import com.zuehlke.securesoftwaredevelopment.exception.InternalServerError;
import com.zuehlke.securesoftwaredevelopment.exception.InvalidArgumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class PersonRepository {

    private static final Logger LOG = LoggerFactory.getLogger(PersonRepository.class);
    private static final AuditLogger auditLogger = AuditLogger.getAuditLogger(PersonRepository.class);

    private DataSource dataSource;

    public PersonRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Person> getAll() {
        LOG.debug("Getting all people");

        List<Person> personList = new ArrayList<>();
        String query = "SELECT id, firstName, lastName, email FROM persons";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(query)) {
            while (rs.next()) {
                personList.add(createPersonFromResultSet(rs));
            }
        } catch (SQLException e) {
            LOG.error("Failed to get all people: " + e.getMessage());
            throw new InternalServerError();
        }

        return personList;
    }

    public List<Person> search(String searchTerm) throws SQLException {
        LOG.debug("Searching for people with: " + searchTerm);

        List<Person> personList = new ArrayList<>();
        String query = "SELECT id, firstName, lastName, email FROM persons WHERE UPPER(firstName) like UPPER('%" + searchTerm + "%')" +
                " OR UPPER(lastName) like UPPER('%" + searchTerm + "%')";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(query)) {
            while (rs.next()) {
                personList.add(createPersonFromResultSet(rs));
            }
        }
        catch (SQLException e) {
            LOG.error("Failed to search for people: " + e.getMessage());
            throw new InternalServerError();
        }

        return personList;
    }

    public Person get(String personId) {
        LOG.debug("Getting person: " + personId);

        String query = "SELECT id, firstName, lastName, email FROM persons WHERE id = " + personId;
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(query)) {
            while (rs.next()) {
                return createPersonFromResultSet(rs);
            }
        } catch (SQLException e) {
            LOG.warn("Failed to get the person: " + e.getMessage());
        }

        LOG.warn("Person with id " + personId + " not found");
        return null;
    }

    public void delete(int personId) {
        LOG.info("Deleting person " + personId);

        Person personFromDb = get("" + personId);
        if (personFromDb == null) throw new InvalidArgumentException();

        String query = "DELETE FROM persons WHERE id = " + personId;
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
        ) {
            statement.executeUpdate(query);
            auditLogger.audit("Successfully deleted person " + personFromDb);
        } catch (SQLException e) {
            LOG.warn("Failed to delete person: " + e.getMessage());
            throw new InvalidArgumentException();
        }
    }

    private Person createPersonFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt(1);
        String firstName = rs.getString(2);
        String lastName = rs.getString(3);
        String email = rs.getString(4);
        return new Person("" + id, firstName, lastName, email);
    }

    public void update(Person personUpdate) {
        LOG.info("Updating person " + personUpdate.getId());

        Person personFromDb = get(personUpdate.getId());
        if (personFromDb == null) throw new InvalidArgumentException();

        String query = "UPDATE persons SET firstName = ?, lastName = '" + personUpdate.getLastName() + "', email = ? where id = " + personUpdate.getId();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
        ) {
            String firstName = personUpdate.getFirstName() != null ? personUpdate.getFirstName() : personFromDb.getFirstName();
            String email = personUpdate.getEmail() != null ? personUpdate.getEmail() : personFromDb.getEmail();
            statement.setString(1, firstName);
            statement.setString(2, email);
            statement.executeUpdate();
            auditLogger.audit(new Entity("Person", personUpdate.getId(), personFromDb.toString(), personUpdate.toString()));
        } catch (SQLException e) {
            LOG.warn("Failed to update person: " + e.getMessage());
            throw new InvalidArgumentException();
        }
    }
}
