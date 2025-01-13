package api.img.get;

import api.interfaces.apiCommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class getImgMap implements apiCommandHandler {
    private static final Logger log = LoggerFactory.getLogger(getImgMap.class);
    private final Map<String, apiCommandHandler> userInfoCommands = new HashMap<>();
    private final String[] commands;

    public getImgMap(String[] commands) {
        this.commands = commands;
        // Map subcommands to handlers
        userInfoCommands.put("fullres", new getFullres(commands));
        userInfoCommands.put("thumbnail", new getThumbnail(commands));
        userInfoCommands.put("properties", new getProperties(commands));
    }

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp, Statement s) throws IOException, SQLException {
        // Delegate to the appropriate subcommand handler
        try {
            userInfoCommands.get(commands[1]).handle(req, resp, s);
        } catch (Exception e) {
            handleError(resp, "{\"error\": \"Invalid image command\"}", e);
        }
    }
}

