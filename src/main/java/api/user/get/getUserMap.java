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
    private final Map<String, apiCommandHandler> getUserCommands = new HashMap<>();
    private final String[] commands;

    public getUserMap(String[] commands){
        this.commands = commands;
        getUserCommands.put("username", new getUsername(commands));
        getUserCommands.put("email", new getEmail(commands));
        getUserCommands.put("date", new getCreatedDate(commands));
        getUserCommands.put("favourites", new getLikedPosts(commands));
    }

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp, Statement s) throws IOException, SQLException {
        centralisedLogger.log("Command: " + Arrays.toString(commands));
        try{
            Integer.parseInt(commands[1]);
        }catch (Exception e){
            handleError(resp, "Invalid UserID", e);
            return;
        }

        try{
            getUserCommands.get(commands[2]).handle(req, resp, s);
        }catch (Exception e){
            handleError(resp, "Invalid User Field", e);
            return;
        }
    }

}