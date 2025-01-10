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
    private final Map<String, apiCommandHandler> postUserCommands = new HashMap<>();
    private final String[] commands;

    public postUserMap(String[] commands){
        this.commands = commands;
        postUserCommands.put("username", new postUsername(commands));
        postUserCommands.put("email", new postEmail(commands));
    }

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp, Statement s) throws IOException, SQLException {
        centralisedLogger.log("Command: " + Arrays.toString(commands));
        try{
            Integer.parseInt(commands[1]);
        }catch (Exception e){
            handleError(resp, "{\"error\": \"Invalid UserID\"}", e);
            return;
        }

        try{
            postUserCommands.get(commands[2]).handle(req, resp, s);
        }catch (Exception e){
            handleError(resp, "{\"error\": \"Invalid User Field\"}", e);
            return;
        }
    }

}