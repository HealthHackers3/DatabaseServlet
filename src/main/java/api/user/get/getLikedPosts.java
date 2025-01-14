package api.user.get;

import api.interfaces.apiCommandHandler;
import util.userAuthenticator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Statement;

public class getLikedPosts implements apiCommandHandler {
    private final String[] commands;

    public getLikedPosts(String[] commands) {
        this.commands = commands;
    }

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp, Statement s) throws Exception {
        if (!userAuthenticator.checkSession(req, resp, s.getConnection())) {return;}
        //returns liked posts for a given userid
        String userId = commands[1];
        String query = "SELECT post_id FROM lpost_likes WHERE user_id = " + userId;
        s.execute(query);
        statement2Json(req, resp, s);
    }
}
