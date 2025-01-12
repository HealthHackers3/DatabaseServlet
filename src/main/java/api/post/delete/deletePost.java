package api.post.delete;

import api.interfaces.apiCommandHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Statement;

public class deletePost implements apiCommandHandler {
    String[] commands;
    public deletePost(String[] commands) {
        this.commands = commands;
    }

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp, Statement s) throws Exception {
         String postId = commands[2];
    }
}
