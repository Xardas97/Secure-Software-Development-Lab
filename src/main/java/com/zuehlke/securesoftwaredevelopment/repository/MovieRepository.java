package com.zuehlke.securesoftwaredevelopment.repository;

import com.zuehlke.securesoftwaredevelopment.config.AuditLogger;
import com.zuehlke.securesoftwaredevelopment.domain.Genre;
import com.zuehlke.securesoftwaredevelopment.domain.Movie;
import com.zuehlke.securesoftwaredevelopment.domain.NewMovie;
import com.zuehlke.securesoftwaredevelopment.exception.InternalServerError;
import com.zuehlke.securesoftwaredevelopment.exception.InvalidArgumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class MovieRepository {

    private static final Logger LOG = LoggerFactory.getLogger(MovieRepository.class);
    private static final AuditLogger auditLogger = AuditLogger.getAuditLogger(MovieRepository.class);

    private DataSource dataSource;

    public MovieRepository(DataSource dataSource) {

        this.dataSource = dataSource;
    }

    public List<Movie> getAll() {
        LOG.debug("Getting all movies");

        List<Movie> movieList = new ArrayList<>();
        String query = "SELECT id, title, description FROM movies";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(query)) {
            while (rs.next()) {
                Movie movie = createMovieFromResultSet(rs);
                movieList.add(movie);
            }
        } catch (SQLException e) {
            LOG.error("Failed to get the movies" + e.getMessage());
            throw new InternalServerError();
        }
        return movieList;
    }

    public List<Movie> search(String searchTerm) {
        LOG.debug("Searching for movies with: " + searchTerm);

        List<Movie> movieList = new ArrayList<>();
        String query = "SELECT DISTINCT m.id, m.title, m.description FROM movies m, movies_to_genres mg, genres g" +
                " WHERE m.id = mg.movieId" +
                " AND mg.genreId = g.id" +
                " AND (UPPER(m.title) like UPPER('%" + searchTerm + "%')" +
                " OR UPPER(g.name) like UPPER('%" + searchTerm + "%'))";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(query)) {
            while (rs.next()) {
                movieList.add(createMovieFromResultSet(rs));
            }
        }
        catch (SQLException e) {
            LOG.error("Failed to search for movies: " + e.getMessage());
            throw new InternalServerError();
        }
        return movieList;
    }

    public Movie get(int movieId, List<Genre> genreList) {
        LOG.debug("Getting the movie with id: " + movieId);

        String query = "SELECT id, title, description FROM movies WHERE id = " + movieId;
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(query)) {
            while (rs.next()) {
                Movie movie = createMovieFromResultSet(rs);
                List<Genre> movieGenres = new ArrayList<>();
                String query2 = "SELECT movieId, genreId FROM movies_to_genres WHERE movieId = " + movieId;
                ResultSet rs2 = statement.executeQuery(query2);
                while (rs2.next()) {
                    Genre genre = genreList.stream().filter(g -> {
                        try {
                            return g.getId() == rs2.getInt(2);
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }).findFirst().get();
                    movieGenres.add(genre);
                }
                movie.setGenres(movieGenres);
                return movie;
            }
        } catch (SQLException e) {
            LOG.warn("Failed to get the movie: " + e.getMessage());
        }

        LOG.warn("Movie with id " + movieId + " not found");
        return null;
    }

    public long create(NewMovie movie, List<Genre> genresToInsert) {
        LOG.info("Creating a movie: " + movie + " with genres: " + String.join(",", genresToInsert.stream().map(Genre::getName).collect(Collectors.toList())));

        String query = "INSERT INTO movies(title, description) VALUES(?, ?)";
        long id = 0;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
        ) {
            statement.setString(1, movie.getTitle());
            statement.setString(2, movie.getDescription());
            statement.executeUpdate();
            LOG.debug("Successfully created a movie, adding genres");

            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                id = generatedKeys.getLong(1);
                long finalId = id;
                genresToInsert.stream().forEach(genre -> {
                    String query2 = "INSERT INTO movies_to_genres(movieId, genreId) VALUES (?, ?)";
                    try (PreparedStatement statement2 = connection.prepareStatement(query2);
                    ) {
                        statement2.setInt(1, (int) finalId);
                        statement2.setInt(2, genre.getId());
                        statement2.executeUpdate();
                    } catch (SQLException e) {
                        LOG.error("Failed to add genre to a movie: " + e.getMessage());
                        throw new InternalServerError();
                    }
                });

                LOG.debug("Finished adding all genres to the new movie");
            }
        } catch (SQLException e) {
            LOG.warn("Failed to create a new movie: " + e.getMessage());
            throw new InvalidArgumentException();
        }

        return id;
    }

    public void delete(int movieId) {
        LOG.info("Deleting movie " + movieId);

        String query = "DELETE FROM movies WHERE id = " + movieId;
        String query2 = "DELETE FROM ratings WHERE movieId = " + movieId;
        String query3 = "DELETE FROM comments WHERE movieId = " + movieId;
        String query4 = "DELETE FROM movies_to_genres WHERE movieId = " + movieId;
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
        ) {
            statement.executeUpdate(query);
            statement.executeUpdate(query2);
            statement.executeUpdate(query3);
            statement.executeUpdate(query4);

            LOG.debug("Successfully deleted the movie " + movieId);
        } catch (SQLException e) {
            LOG.warn("Failed to delete the movie: " + e.getMessage());
            throw new InvalidArgumentException();
        }
    }

    private Movie createMovieFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt(1);
        String title = rs.getString(2);
        String description = rs.getString(3);
        return new Movie(id, title, description, new ArrayList<>());
    }
}
