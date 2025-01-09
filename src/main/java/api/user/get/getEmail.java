package api.user.get;

import api.interfaces.apiCommandHandler;
import util.centralisedLogger;
import util.userAuthenticator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Statement;
import java.util.Arrays;

public class getEmail implements apiCommandHandler {
    private final String[] commands;
    public getEmail(String[] commands){
        this.commands = commands;
    }
    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp, Statement s) throws Exception {
        if(!userAuthenticator.checkSession(req, resp, s.getConnection())){return;}
        centralisedLogger.log("Command: " + Arrays.toString(commands));
        s.execute("SELECT email FROM lusers WHERE user_id = " + commands[1]);
        statement2Json(req,resp,s);
    }
}
