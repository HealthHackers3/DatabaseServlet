package api.post.get;

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

public class getPostMap implements apiCommandHandler {
    private final Map<String, apiCommandHandler> getPostCommands = new HashMap<>();
    private final String[] commands;

    public getPostMap(String[] commands){
        this.commands = commands;
        getPostCommands.put("postslistdateasc", new getPostListDateAsc(commands));
        getPostCommands.put("postslistdatedesc", new getPostListDateDesc(commands));
        getPostCommands.put("postslistAZ", new getPostListAZ(commands));
        getPostCommands.put("postslistZA", new getPostListZA(commands));
        getPostCommands.put("coverImgId", new getPostCoverImgID(commands));
        getPostCommands.put("info", new getPostInfo(commands));
        getPostCommands.put("likestatus", new getLikeStatus(commands));

    }
    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp, Statement s) throws IOException, SQLException {
        centralisedLogger.log("Command: " + Arrays.toString(commands));
        try{
            getPostCommands.get(commands[1]).handle(req, resp, s);
        }catch (Exception e){
            handleError(resp, "{\"error\": \"Invalid Command Field\"}", e);
            return;
        }
    }
}
