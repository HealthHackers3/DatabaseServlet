package api.post.get;

import api.interfaces.apiCommandHandler;
import util.userAuthenticator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Statement;

public class getPostInfo implements apiCommandHandler {
    private final String[] commands;
    public getPostInfo(String[] commands){
        this.commands = commands;
    }
    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp, Statement s) throws Exception {
        if(!userAuthenticator.checkSession(req, resp, s.getConnection())){return;}
        //return post info
        s.execute("SELECT * FROM lposts WHERE post_id = " + commands[2]);
        statement2Json(req,resp,s);
    }
}