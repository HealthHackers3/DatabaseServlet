package api.post.get;

import api.interfaces.apiCommandHandler;
import util.centralisedLogger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.ResultSet;
import java.sql.Statement;

public class getPostImages implements apiCommandHandler {
    private String[] commands;

    public getPostImages(String[] commands) {
        this.commands = commands;
    }

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp, Statement s) throws Exception {
        try {
            // Extract post_id from commands
            int postId = Integer.parseInt(commands[2]);

            // SQL query to fetch image IDs for the given post_id ordered by image_index
            String sql = "SELECT image_id FROM Lpost_images WHERE post_id = " + postId + " ORDER BY image_index";

            centralisedLogger.log("Executing SQL: " + sql);

            try (ResultSet rs = s.executeQuery(sql)) {
                StringBuilder resultJson = new StringBuilder();
                resultJson.append("[");

                boolean hasResults = false;

                // Iterate over the result set and build the JSON array
                while (rs.next()) {
                    hasResults = true;
                    int imageId = rs.getInt("image_id");
                    resultJson.append(imageId).append(",");
                }

                if (hasResults) {
                    // Remove trailing comma
                    resultJson.setLength(resultJson.length() - 1);
                }

                resultJson.append("]");

                // Respond with the JSON array
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.setContentType("application/json");
                resp.getWriter().write(resultJson.toString());
            }
        } catch (Exception e) {
            handleError(resp, "failed image fetch", e);
        }
    }
}
