package getRequests;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class getPostInfo implements getCommandHandler {
    private final Map<String, getCommandHandler> userInfoCommands = new HashMap<>();
    private final String[] commands;

    public getPostInfo(String[] commands){
        this.commands = commands;
        userInfoCommands.put("username", new getUsername(commands));
        userInfoCommands.put("email", new getEmail(commands));
        userInfoCommands.put("date", new getCreatedDate(commands));
    }
    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp, Statement s) throws IOException, SQLException {

    }
}
