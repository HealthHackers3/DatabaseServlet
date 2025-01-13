package api.user.get;

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

public class getUserMap implements apiCommandHandler {
    // Stores mapping of subcommands (e.g., "username", "email", "date") to their handlers
    private final Map<String, apiCommandHandler> getUserCommands = new HashMap<>();
    private final String[] commands; // Holds the command path as an array

    public getUserMap(String[] commands) {
        this.commands = commands;

        // Initialize subcommands and their corresponding handlers
        getUserCommands.put("username", new getUsername(commands)); // Fetches the username
        getUserCommands.put("email", new getEmail(commands));       // Fetches the email
        getUserCommands.put("date", new getCreatedDate(commands));  // Fetches the account creation date
    }

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp, Statement s) throws IOException, SQLException {
        // Log the incoming command for debugging purposes
        centralisedLogger.log("Command: " + Arrays.toString(commands));

        // Validate the user ID (commands[1]) to ensure it is a valid integer
        try {
            Integer.parseInt(commands[1]); // Attempt to parse user ID
        } catch (Exception e) {
            handleError(resp, "Invalid UserID", e); // Return error if the ID is not valid
            return;
        }

        // Route the request to the appropriate subcommand handler based on commands[2]
        try {
            getUserCommands.get(commands[2]).handle(req, resp, s); // Delegate to the correct handler
        } catch (Exception e) {
            handleError(resp, "Invalid User Field", e); // Handle errors for invalid or missing subcommands
            return;
        }
    }
}
