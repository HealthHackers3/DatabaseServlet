package api.user.post;

import api.interfaces.apiCommandHandler;
import api.post.post.postNewPost;
import api.user.get.getCreatedDate;
import api.user.get.getEmail;
import api.user.get.getUsername;
import util.centralisedLogger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class postPostMap implements apiCommandHandler {
    private final Map<String, apiCommandHandler> userInfoCommands = new HashMap<>();
    private final String[] commands;

    public postPostMap(String[] commands){
        this.commands = commands;
        userInfoCommands.put("newpost", new postNewPost(commands));
    }

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp, Statement s) throws IOException, SQLException {
        centralisedLogger.log("Command: " + Arrays.toString(commands));
        try{
            userInfoCommands.get(commands[1]).handle(req, resp, s);
        }catch (Exception e){
            handleError(resp, "{\"error\": \"Invalid Command Field\"}", e);
            return;
        }
    }

}