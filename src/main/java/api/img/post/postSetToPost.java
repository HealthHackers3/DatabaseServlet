package api.img.post;

import api.interfaces.apiCommandHandler;
import util.centralisedLogger;
import util.userAuthenticator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.stream.Collectors;

public class postSetToPost implements apiCommandHandler {
    String[] commands;
    public postSetToPost(String[] commands) {
        this.commands = commands;
    }

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp, Statement s) throws Exception {
        if(!userAuthenticator.checkSession(req, resp, s.getConnection())){return;}
        try {
            String img_id = commands[1];
            String post_id = commands[2];
            centralisedLogger.log("Command: " + Arrays.toString(commands));
            s.execute("UPDATE lpost_images SET post_id = '" + post_id + "' WHERE image_id = " + img_id);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("{\"message\":\"successfully added image to " +post_id+ "\"}");
        }catch(SQLException e){
            handleError(resp, "Invalid request", e);
        }
    }
}
