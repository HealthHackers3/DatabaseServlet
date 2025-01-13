package api.img.post;

import api.interfaces.apiCommandHandler;
import util.centralisedLogger;
import util.userAuthenticator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

public class postSetToPost implements apiCommandHandler {
    String[] commands;

    public postSetToPost(String[] commands) {
        this.commands = commands;
    }

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp, Statement s) throws Exception {
        // Validate the user's session
        if (!userAuthenticator.checkSession(req, resp, s.getConnection())) {
            return;
        }
        try {
            // Log the incoming commands
            centralisedLogger.log("Commands: " + Arrays.toString(commands));

            // Extract image ID and post ID from the commands
            String imgId = commands[2];
            String postId = commands[3];

            // Log the operation details
            centralisedLogger.log("Setting image ID " + imgId + " to post ID " + postId);

            // Use PreparedStatement for secure database interaction
            String updateSQL = "UPDATE lpost_images SET post_id = ? WHERE image_id = ?";
            try (PreparedStatement ps = s.getConnection().prepareStatement(updateSQL)) {
                ps.setString(1, postId);
                ps.setString(2, imgId);
                ps.executeUpdate(); // Execute the update query
            }

            // Send success response
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("{\"message\":\"Successfully added image to post " + postId + "\"}");

        } catch (SQLException e) {
            // Handle SQL exceptions and send error response
            handleError(resp, "Invalid request", e);
        }
    }
}
