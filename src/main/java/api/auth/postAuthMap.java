package api.auth;

import api.interfaces.apiCommandHandler;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
import java.util.*;

// Handles authentication-related commands (e.g., register, login).
public class postAuthMap implements apiCommandHandler {
    private final Map<String, apiCommandHandler> userInfoCommands = new HashMap<>(); // Maps commands to handlers.
    private final String[] commands; // Stores command input.

    // Constructor initializes command handlers.
    public postAuthMap(String[] commands) {
        this.commands = commands;
        userInfoCommands.put("register", new postNewUser(commands)); // Handles registration.
        userInfoCommands.put("login", new postLogin(commands)); // Handles login.
    }

    // Processes commands by delegating to the appropriate handler.
    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp, Statement s) throws IOException, SQLException {
        try {
            userInfoCommands.get(commands[1]).handle(req, resp, s); // Execute sub-command.
        } catch (Exception e) {
            handleError(resp, "{\"error\": \"Invalid post command\"}", e); // Send error response.
        }
    }
}
