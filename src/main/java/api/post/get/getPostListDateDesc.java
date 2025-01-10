package api.post.get;

import api.interfaces.apiCommandHandler;
import util.userAuthenticator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Statement;

public class getPostListDateDesc implements apiCommandHandler {
    private final String[] commands;
    public getPostListDateDesc(String[] commands){
        this.commands = commands;
    }
    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp, Statement s) throws Exception {
        if(!userAuthenticator.checkSession(req, resp, s.getConnection())){return;}
        s.execute("SELECT post_id FROM lposts ORDER BY upload_date DESC");
        statement2Json(req,resp,s);
    }
}