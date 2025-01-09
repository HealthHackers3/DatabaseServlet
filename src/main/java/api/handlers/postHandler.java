package api.handlers;
import api.auth.postAuthMap;
import api.img.postImgMap;
import api.interfaces.apiCommandHandler;
import api.debug.postSQLRequest;
import api.user.post.postUserMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class postHandler {
    //class variables
    private final Map<String, apiCommandHandler> commandHandlers = new HashMap<>();
    HttpServletRequest req;
    HttpServletResponse resp;
    String[] commands;
    Statement s;

    public postHandler(String[] commandPath, HttpServletRequest req, HttpServletResponse resp, Statement s) throws SQLException, IOException {
        this.req = req;
        this.resp = resp;
        this.s = s;
        this.commands = commandPath;

        //define get commands commands
        commandHandlers.put("sqlraw", new postSQLRequest(commands));
        commandHandlers.put("img", new postImgMap(commands));
        commandHandlers.put("auth", new postAuthMap(commands));
        commandHandlers.put("users", new postUserMap(commands));
    }

    //execute command
    public void execute() throws Exception {
        //check if command exists in the map from before
        if (commands[0] == null || !commandHandlers.containsKey(commands[0])) {
            //resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Invalid or missing command\"}");
            return;
        }
        //run class of command
        commandHandlers.get(commands[0]).handle(req, resp, s);
    }
}


