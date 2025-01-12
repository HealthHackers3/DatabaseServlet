package api.post.get;

import api.interfaces.apiCommandHandler;
import api.post.delete.deletePost;
import util.centralisedLogger;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class deletePostMap implements apiCommandHandler {
    private final Map<String, apiCommandHandler> deletePostCommands = new HashMap<>();
    private final String[] commands;

    public deletePostMap(String[] commands){
        this.commands = commands;
        deletePostCommands.put("deletepost", new deletePost(commands));
    }
    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp, Statement s) throws IOException, SQLException {
        centralisedLogger.log("Command: " + Arrays.toString(commands));
        try{
            deletePostCommands.get(commands[1]).handle(req, resp, s);
        }catch (Exception e){
            handleError(resp, "{\"error\": \"Invalid Command Field\"}", e);
            return;
        }
    }
}
