package api.auth;

import api.interfaces.apiCommandHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import util.centralisedLogger;
import util.passwordHasher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Map;

public class postNewUser implements apiCommandHandler {
    String[] commands;
    public postNewUser(String[] commands){
        this.commands = commands;
    }
    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp, Statement s) throws IOException, SQLException {
        centralisedLogger.log("Command: " + Arrays.toString(commands));
        resp.setContentType("application/json");

        try {
            // Log content type
            centralisedLogger.log("Content-Type: " + req.getContentType());

            // Parse input fields
            String username = null, password = null, email = null;

            // Handle JSON input
            if ("application/json".equals(req.getContentType())) {
                BufferedReader reader = req.getReader();
                StringBuilder jsonBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonBuilder.append(line);
                }
                String json = jsonBuilder.toString();
                centralisedLogger.log("Raw JSON Body: " + json);

                // Parse JSON
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, String> jsonMap = objectMapper.readValue(json, Map.class);
                username = jsonMap.get("username");
                password = jsonMap.get("password");
                email = jsonMap.get("email");
            }
            // Handle form data
            else if ("application/x-www-form-urlencoded".equals(req.getContentType())) {
                username = req.getParameter("username");
                password = req.getParameter("password");
                email = req.getParameter("email");
            }

            // Log parsed fields
            centralisedLogger.log("Parsed username: " + username);
            centralisedLogger.log("Parsed password: " + password);
            centralisedLogger.log("Parsed email: " + email);

            // Validate input
            if (username == null || password == null || email == null) {
                handleError(resp, "{\"error\": \"Missing required fields\"}", null);
                return;
            }

            // Insert the new user into the database
            String sql = "INSERT INTO lusers (username, password, email) VALUES (?, ?, ?)";
            try (PreparedStatement ps = s.getConnection().prepareStatement(sql)) {
                ps.setString(1, username);
                ps.setString(2, passwordHasher.hashPassword(password));
                ps.setString(3, email);

                int rowsAffected = ps.executeUpdate();
                if (rowsAffected > 0) {
                    centralisedLogger.log("{\"success\": \"User added successfully\"}");
                    resp.getWriter().write("{\"message\": \"User added successfully\"}");
                } else {
                    handleError(resp, "{\"error\": \"Failed to add user\"}", null);
                }
            }
        } catch (SQLException e) {
            handleError(resp, "{\"error\": \"Database error: " + e.getMessage() + "\"}", e);
        } catch (Exception e) {
            handleError(resp, "{\"error\": \"Unexpected error: " + e.getMessage() + "\"}", e);
        }
    }
}