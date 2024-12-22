package getRequests;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

class getUserInfo implements getCommandHandler {
    private final Map<String, getCommandHandler> userInfoCommands = new HashMap<>();
    private final String[] commands;

    public getUserInfo(String[] commands){
        this.commands = commands;
        userInfoCommands.put("username", new getUsername(commands));
        userInfoCommands.put("email", new getEmail(commands));
        userInfoCommands.put("date", new getCreatedDate(commands));
    }

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp, Statement s) throws IOException, SQLException {
        try{
            Integer.parseInt(commands[1]);
        }catch (Exception e){
            //resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Invalid UserID\"}");
            return;
        }

        try{
            userInfoCommands.get(commands[2]).handle(req, resp, s);
        }catch (Exception e){
            //resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Invalid User Field\"}");
            return;
        }
    }

}
class getUsername implements getCommandHandler {
    private final String[] commands;
    public getUsername(String[] commands){
        this.commands = commands;
    }
    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp, Statement s) throws IOException, SQLException {
        s.execute("SELECT username FROM lusers WHERE user_id = " + commands[1]);
        statement2Json(req,resp,s);
    }
}
class getEmail implements getCommandHandler {
    private final String[] commands;
    public getEmail (String[] commands){
        this.commands = commands;
    }
    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp, Statement s) throws IOException, SQLException {
        s.execute("SELECT email FROM lusers WHERE user_id = " + commands[1]);
        statement2Json(req,resp,s);
    }
}
class getCreatedDate implements getCommandHandler {
    private final String[] commands;
    public getCreatedDate (String[] commands){
        this.commands = commands;
    }
    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp, Statement s) throws IOException, SQLException {
        s.execute("SELECT created_at FROM lusers WHERE user_id = " + commands[1]);
        statement2Json(req,resp,s);
    }
}