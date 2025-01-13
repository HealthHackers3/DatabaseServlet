package api.post.post;

import api.interfaces.apiCommandHandler;
import util.userAuthenticator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.PreparedStatement;
import java.sql.Statement;

public class postUnlike implements apiCommandHandler {
    private final String[] commands;

    public postUnlike(String[] commands) {
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
            // Remove the like from lpost_likes
            String deleteLikeSQL = "DELETE FROM lpost_likes WHERE post_id = ? AND user_id = ?;";
            try (PreparedStatement deleteLikeStmt = s.getConnection().prepareStatement(deleteLikeSQL)) {
                deleteLikeStmt.setInt(1, postId);
                deleteLikeStmt.setInt(2, userId);
                int rowsDeleted = deleteLikeStmt.executeUpdate();

                if (rowsDeleted > 0) {
                    // Decrement the likes column on the post
                    String updateLikesSQL = "UPDATE lposts SET likes = likes - 1 WHERE post_id = ?;";
                    try (PreparedStatement updateLikesStmt = s.getConnection().prepareStatement(updateLikesSQL)) {
                        updateLikesStmt.setInt(1, postId);
                        updateLikesStmt.executeUpdate();
                    }
                    resp.setContentType("application/json");
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.getWriter().write("{\"message\": \"Like removed successfully.\"}");

                } else {
                    // Like does not exist
                    handleError(resp, "User has not liked this post.", new Exception("User has not liked this post."));
                }
            }
        } catch (Exception e) {
            handleError(resp, e.getMessage(), e);
            throw e;
        }
    }
}
