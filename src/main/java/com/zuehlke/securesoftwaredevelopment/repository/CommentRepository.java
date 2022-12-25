package com.zuehlke.securesoftwaredevelopment.repository;

import com.zuehlke.securesoftwaredevelopment.domain.Comment;
import com.zuehlke.securesoftwaredevelopment.exception.InvalidArgumentException;
import com.zuehlke.securesoftwaredevelopment.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class CommentRepository {

    private static final Logger LOG = LoggerFactory.getLogger(CommentRepository.class);


    private DataSource dataSource;

    public CommentRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void create(Comment comment) {
        LOG.info("Adding a comment to movie " + comment.getMovieId() + ": " + comment.getComment());

        String query = "insert into comments(movieId, userId, comment) values (" + comment.getMovieId() + ", " + comment.getUserId() + ", ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, comment.getComment());
            statement.execute();
            LOG.debug("Successfully added a comment");
        } catch (SQLException e) {
            LOG.warn("Failed to add the comment: " + e.getMessage());
            throw new InvalidArgumentException();
        }
    }

    public List<Comment> getAll(String movieId) {
        LOG.debug("Getting all comments for movie: " + movieId);

        List<Comment> commentList = new ArrayList<>();
        String query = "SELECT movieId, userId, comment FROM comments WHERE movieId = " + movieId;
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(query)) {
            while (rs.next()) {
                commentList.add(new Comment(rs.getInt(1), rs.getInt(2), rs.getString(3)));
            }
        } catch (SQLException e) {
            LOG.warn("Failed to get the comments: " + e.getMessage());
            throw new NotFoundException();
        }

        return commentList;
    }
}
