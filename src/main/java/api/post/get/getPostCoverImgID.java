package api.post.get;

import api.interfaces.apiCommandHandler;
import util.userAuthenticator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class getPostCoverImgID implements apiCommandHandler {
    private final String[] commands;
    public getPostCoverImgID(String[] commands){
        this.commands = commands;
    }
    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp, Statement s) throws Exception {
        if(!userAuthenticator.checkSession(req, resp, s.getConnection())){return;}

        try {
            String postId = commands[2];
            // Query to find the image ID with the lowest order_index for the given post ID
            String query = "SELECT image_id " +
                    "FROM Lpost_images " +
                    "WHERE post_id = ? " +
                    "ORDER BY order_index ASC " +
                    "LIMIT 1";

            // Use PreparedStatement to prevent SQL injection
            try (PreparedStatement ps = s.getConnection().prepareStatement(query)) {
                ps.setInt(1, Integer.parseInt(postId));

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int imageId = rs.getInt("image_id");

                        // Return the image ID as JSON response
                        resp.setContentType("application/json");
                        resp.getWriter().write("{\"image_id\": " + imageId + "}");
                    } else {
                        // No images found for the given post ID
                        handleError(resp, "No images found for the given post ID", new Exception("No images found for the given post ID"));
                    }
                }
            }
        } catch (Exception e) {
            handleError(resp, "An error occurred while obtaining the id", e);
        }
    }
}