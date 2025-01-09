package api.user.get;

import api.interfaces.apiCommandHandler;
import util.userAuthenticator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Statement;


public class getCreatedDate implements apiCommandHandler {
    private final String[] commands;
    public getCreatedDate(String[] commands){
        this.commands = commands;
    }
    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp, Statement s) throws Exception {
        if(!userAuthenticator.checkSession(req, resp, s.getConnection())){return;}
        s.execute("SELECT created_at FROM lusers WHERE user_id = " + commands[1]);
        statement2Json(req,resp,s);
    }
}