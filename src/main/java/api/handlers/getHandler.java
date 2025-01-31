package api.handlers;

import api.debug.getErrorConsole;
import api.debug.getRefreshAllTables;
import api.debug.getRefreshFiles;
import api.img.get.getImgMap;
import api.interfaces.apiCommandHandler;
import api.post.get.getPostMap;
import api.user.get.getUserMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class getHandler {
    //class variables
    private final Map<String, apiCommandHandler> commandHandlers = new HashMap<>();
    HttpServletRequest req;
    HttpServletResponse resp;
    String[] commands;
    Statement s;

    public getHandler(String[] commandPath, HttpServletRequest req, HttpServletResponse resp, Statement s) throws SQLException, IOException {
        this.req = req;
        this.resp = resp;
        this.s = s;
        this.commands = commandPath;
        //define get commands commands
        commandHandlers.put("users", new getUserMap(commands));
        commandHandlers.put("img", new getImgMap(commands));
        commandHandlers.put("post", new getPostMap(commands));
        commandHandlers.put("console", new getErrorConsole());
        commandHandlers.put("refreshAll", new getRefreshAllTables());
        commandHandlers.put("refreshFiles", new getRefreshFiles());


    }

    //execute command
    public void execute() throws Exception {
        //check if command exists
        if (commands[0] == null || !commandHandlers.containsKey(commands[0])) {
            //resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Invalid or missing command\"}");
            return;
        }
        //run class of command
        commandHandlers.get(commands[0]).handle(req, resp, s);
    }
}
