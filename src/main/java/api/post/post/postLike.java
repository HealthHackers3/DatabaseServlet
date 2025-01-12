package api.post.post;

import api.interfaces.apiCommandHandler;
import util.centralisedLogger;
import util.userAuthenticator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.PreparedStatement;
import java.sql.Statement;

public class postLike implements apiCommandHandler {
    private final String[] commands;

    public postLike(String[] commands) {
        this.commands = commands;
    }

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp, Statement s) throws Exception {
        if (!userAuthenticator.checkSession(req, resp, s.getConnection())) {
            return;
        }

        int postId = Integer.parseInt(commands[2]);
        int userId = Integer.parseInt(commands[3]);

        try {
            // Add a like to lpost_likes

            String insertLikeSQL = "INSERT INTO lpost_likes (post_id, user_id) VALUES (?, ?) ON CONFLICT DO NOTHING;";
            try (PreparedStatement insertLikeStmt = s.getConnection().prepareStatement(insertLikeSQL)) {
                insertLikeStmt.setInt(1, postId);
                insertLikeStmt.setInt(2, userId);
                int rowsInserted = insertLikeStmt.executeUpdate();

                if (rowsInserted > 0) {
                    // Increment the likes column on the post
                    String updateLikesSQL = "UPDATE lposts SET likes = likes + 1 WHERE post_id = ?;";
                    try (PreparedStatement updateLikesStmt = s.getConnection().prepareStatement(updateLikesSQL)) {
                        updateLikesStmt.setInt(1, postId);
                        updateLikesStmt.executeUpdate();
                    }
                    resp.setContentType("application/json");
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.getWriter().write("{\"message\": \"Like added successfully.\"}");

                } else {
                    // Like already exists
                    resp.setContentType("application/json");
                   // resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write("{\"error\": \"User has already liked this post.\"}");
                }
            }
        } catch (Exception e) {
            centralisedLogger.log("Error adding like: " + e.getMessage());
            handleError(resp, e.getMessage(), e);
            throw e;
        }
    }
}
