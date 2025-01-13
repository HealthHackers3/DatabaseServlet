package api.user.post;

import api.interfaces.apiCommandHandler;
import util.centralisedLogger;
import util.userAuthenticator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.stream.Collectors;

public class postUsername implements apiCommandHandler {
    private final String[] commands;
    public postUsername(String[] commands){
        this.commands = commands;
    }
    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp, Statement s) throws Exception {
        if(!userAuthenticator.checkSession(req, resp, s.getConnection())){return;}
        System.out.println("doing");
        try {
            //update username
            String username = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
            s.execute("UPDATE lusers SET username = '" + username + "' WHERE user_id = " + commands[1]);
        }catch(SQLException e){
            handleError(resp, "Invalid request", e);

        }
    }
}
