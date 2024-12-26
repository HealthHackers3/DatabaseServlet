package getRequests;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class getHandler {
    //class variables
    private final Map<String, getCommandHandler> commandHandlers = new HashMap<>();
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
        commandHandlers.put("users", new getUserInfo(commands));
        commandHandlers.put("img", new getImgInfo(commands));
        commandHandlers.put("post", new getUserInfo(commands));


    }

    //execute command
    public void execute() throws IOException, SQLException {
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
