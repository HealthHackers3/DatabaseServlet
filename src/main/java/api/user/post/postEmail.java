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

public class postEmail implements apiCommandHandler {
    private final String[] commands;
    public postEmail(String[] commands){
        this.commands = commands;
    }
    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp, Statement s) throws Exception {
        if(!userAuthenticator.checkSession(req, resp, s.getConnection())){return;}
        try {
            String email = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
            centralisedLogger.log("Command: " + Arrays.toString(commands));
            s.execute("UPDATE lusers SET email = '" + email + "' WHERE user_id = " + commands[1]);
        }catch(SQLException e){
            handleError(resp, "Invalid request", e);
        }
    }
}
