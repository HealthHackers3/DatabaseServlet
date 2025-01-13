package api.auth;

import api.interfaces.apiCommandHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import util.centralisedLogger;
import util.passwordHasher;
import util.userAuthenticator;

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

    public postNewUser(String[] commands) {
        this.commands = commands;
    }

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp, Statement s) throws Exception {
        centralisedLogger.log("Command: " + Arrays.toString(commands));
        resp.setContentType("application/json");

        // Check if the user session is valid
        if (!userAuthenticator.checkSession(req, resp, s.getConnection())) {
            return;
        }
        try {
            // Determine request content type and parse input fields
            String username = null, password = null, email = null;

            if ("application/json".equals(req.getContentType())) {
                // Handle JSON input
                BufferedReader reader = req.getReader();
                StringBuilder jsonBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonBuilder.append(line);
                }
                String json = jsonBuilder.toString();
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, String> jsonMap = objectMapper.readValue(json, Map.class);
                username = jsonMap.get("username");
                password = jsonMap.get("password");
                email = jsonMap.get("email");
            } else if ("application/x-www-form-urlencoded".equals(req.getContentType())) {
                // Handle form data input
                username = req.getParameter("username");
                password = req.getParameter("password");
                email = req.getParameter("email");
            }

            // Validate parsed input
            if (username == null || password == null || email == null) {
                handleError(resp, "{\"error\": \"Missing required fields\"}", null);
                return;
            }

            // Insert new user into the database
            String sql = "INSERT INTO lusers (username, password, email) VALUES (?, ?, ?)";
            try (PreparedStatement ps = s.getConnection().prepareStatement(sql)) {
                ps.setString(1, username);
                ps.setString(2, passwordHasher.hashPassword(password));
                ps.setString(3, email);

                int rowsAffected = ps.executeUpdate();
                if (rowsAffected > 0) {
                    resp.getWriter().write("{\"message\": \"User added successfully\"}");
                } else {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    handleError(resp, "{\"error\": \"Failed to add user\"}", new Exception("Failed to add user"));
                }
            }
        } catch (SQLException e) {
            // Handle database-specific errors
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            handleError(resp, "{\"error\": \"Database error: " + e.getMessage() + "\"}", e);
        } catch (Exception e) {
            // Handle unexpected errors
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            handleError(resp, "{\"error\": \"Unexpected error: " + e.getMessage() + "\"}", e);
        }
    }
}
