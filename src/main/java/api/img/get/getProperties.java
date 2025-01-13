package api.img.get;

import api.interfaces.apiCommandHandler;
import com.google.gson.JsonObject;
import util.centralisedLogger;
import util.userAuthenticator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

class getProperties implements apiCommandHandler {
    private final String[] commands;

    public getProperties(String[] commands) {
        this.commands = commands;
    }

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp, Statement s) throws Exception {
        centralisedLogger.log("Command: " + Arrays.toString(commands));
        if (!userAuthenticator.checkSession(req, resp, s.getConnection())) {
            return;
        }

        int image_id = Integer.parseInt(commands[2]);
        try {
            // Query image properties from the database
            ResultSet rs = s.executeQuery("SELECT cell_count, cell_density, cell_dimensions_y, cell_dimensions_x FROM lpost_images WHERE image_id = " + image_id);

            if (rs.next()) {
                JsonObject json = new JsonObject();
                json.addProperty("cell_count", rs.getInt("cell_count"));
                json.addProperty("cell_density", rs.getFloat("cell_density"));
                json.addProperty("cell_dimensions_y", rs.getFloat("cell_dimensions_y"));
                json.addProperty("cell_dimensions_x", rs.getFloat("cell_dimensions_x"));

                // Send JSON response with properties
                resp.setContentType("application/json");
                resp.setCharacterEncoding("UTF-8");
                resp.getWriter().write(json.toString());
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "No data found for the given image ID");
            }
        } catch (SQLException e) {
            handleError(resp, "{\"error\": \"Database error while fetching image properties\"}", e);
        } catch (Exception e) {
            handleError(resp, "{\"error\": \"Unexpected error\"}", e);
        }
    }
}