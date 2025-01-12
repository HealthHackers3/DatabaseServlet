package api.post.delete;

import api.interfaces.apiCommandHandler;
import util.centralisedLogger;
import util.userAuthenticator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.PreparedStatement;
import java.sql.Statement;

public class deletePost implements apiCommandHandler {
    private final String[] commands;

    public deletePost(String[] commands) {
        this.commands = commands;
    }

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp, Statement s) throws Exception {
        if (!userAuthenticator.checkSession(req, resp, s.getConnection())) {
            return;
        }

        String postId = commands[2];

        try {
            // Prepare SQL query to delete the post
            String deletePostSQL = "DELETE FROM Lposts WHERE post_id = ?;";
            try (PreparedStatement deleteStmt = s.getConnection().prepareStatement(deletePostSQL)) {
                deleteStmt.setInt(1, Integer.parseInt(postId));
                int rowsAffected = deleteStmt.executeUpdate();

                if (rowsAffected > 0) {
                    // Respond with success if the post was deleted
                    resp.setContentType("application/json");
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.getWriter().write("{\"message\": \"Post deleted successfully.\"}");
                } else {
                    // Respond with error if the post was not found
                    resp.setContentType("application/json");
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write("{\"error\": \"Post not found.\"}");
                }
            }
        } catch (Exception e) {
            centralisedLogger.log("Error deleting post: " + e.getMessage());

            // Respond with server error if an exception occurs
            resp.setContentType("application/json");
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"An error occurred while deleting the post.\"}");
        }
    }
}
