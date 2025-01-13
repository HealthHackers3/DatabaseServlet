package api.user.post;

import api.interfaces.apiCommandHandler;
import util.centralisedLogger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class postUserMap implements apiCommandHandler {
    // Map to store subcommands and their corresponding handlers
    private final Map<String, apiCommandHandler> postUserCommands = new HashMap<>();
    private final String[] commands; // Stores the parsed command path

    public postUserMap(String[] commands) {
        this.commands = commands;

        // Initialize subcommands for user-related POST operations
        postUserCommands.put("username", new postUsername(commands)); // Updates username
        postUserCommands.put("email", new postEmail(commands));       // Updates email
    }

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp, Statement s) throws IOException, SQLException {
        // Log the incoming command for debugging purposes
        centralisedLogger.log("Command: " + Arrays.toString(commands));

        // Validate the user ID (commands[1]) to ensure it is an integer
        try {
            Integer.parseInt(commands[1]); // Check if user ID is a valid integer
        } catch (Exception e) {
            handleError(resp, "{\"error\": \"Invalid UserID\"}", e); // Return error if validation fails
            return;
        }

        // Route the request to the appropriate subcommand handler based on commands[2]
        try {
            postUserCommands.get(commands[2]).handle(req, resp, s); // Delegate handling to the appropriate handler
        } catch (Exception e) {
            handleError(resp, "{\"error\": \"Invalid User Field\"}", e); // Handle invalid or missing subcommands
        }
    }
}
