package api.auth;

import api.interfaces.apiCommandHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
import java.util.*;


public class postAuthMap implements apiCommandHandler {
    private final Map<String, apiCommandHandler> userInfoCommands = new HashMap<>();
    private final String[] commands;

    public postAuthMap(String[] commands){
        this.commands = commands;
        userInfoCommands.put("register", new postNewUser(commands));
        userInfoCommands.put("login", new postLogin(commands));
    }

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp, Statement s) throws IOException, SQLException {
        try{
            userInfoCommands.get(commands[1]).handle(req, resp, s);
        }catch (Exception e){
            handleError(resp,"{\"error\": \"Invalid post command\"}", e);
            return;
        }
    }

}