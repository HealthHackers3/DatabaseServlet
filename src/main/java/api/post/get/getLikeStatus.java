package api.post.get;

import api.interfaces.apiCommandHandler;
import util.userAuthenticator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class getLikeStatus implements apiCommandHandler {
    private final String[] commands;

    public getLikeStatus(String[] commands) {
        this.commands = commands;
    }

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp, Statement s) throws Exception {
        if (!userAuthenticator.checkSession(req, resp, s.getConnection())) {
            return;
        }

        int postId = Integer.parseInt(commands[2]); // Post ID from commands
        int userId = Integer.parseInt(commands[3]); // User ID from commands

        String query = "SELECT COUNT(*) AS like_count FROM lpost_likes WHERE post_id = ? AND user_id = ?;";

        try (PreparedStatement ps = s.getConnection().prepareStatement(query)) {
            ps.setInt(1, postId);
            ps.setInt(2, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    boolean hasLiked = rs.getInt("like_count") > 0;

                    // Respond with the like status
                    resp.setContentType("application/json");
                    resp.setCharacterEncoding("UTF-8");
                    resp.getWriter().write("{" +
                            "\"post_id\": " + postId + "," +
                            "\"user_id\": " + userId + "," +
                            "\"has_liked\": " + hasLiked +
                            "}");
                } else {
                    handleError(resp, "Unable to determine like status", new Exception("Unable to determine like status"));
                }
            }
        } catch (Exception e) {
            handleError(resp, "An error occurred while checking like status.", e);
        }
    }
}
