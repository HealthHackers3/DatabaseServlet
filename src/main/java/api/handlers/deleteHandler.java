package api.handlers;

import api.debug.getErrorConsole;
import api.interfaces.apiCommandHandler;
import api.img.getImgMap;
import api.post.delete.deletePost;
import api.post.get.deletePostMap;
import api.post.get.getPostMap;
import api.user.get.getUserMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class deleteHandler {
    //class variables
    private final Map<String, apiCommandHandler> commandHandlers = new HashMap<>();
    HttpServletRequest req;
    HttpServletResponse resp;
    String[] commands;
    Statement s;

    public deleteHandler(String[] commandPath, HttpServletRequest req, HttpServletResponse resp, Statement s) throws SQLException, IOException {
        this.req = req;
        this.resp = resp;
        this.s = s;
        this.commands = commandPath;
        //define get commands commands
        commandHandlers.put("post", new deletePostMap(commands));


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
