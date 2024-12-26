package getRequests;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class getImgInfo implements getCommandHandler {
    private final Map<String, getCommandHandler> userInfoCommands = new HashMap<>();
    private final String[] commands;

    public getImgInfo(String[] commands){
        this.commands = commands;
        userInfoCommands.put("fullres", new getFullres(commands));
        userInfoCommands.put("thumbnail", new getEmail(commands));
        userInfoCommands.put("properties", new getCreatedDate(commands));
    }
    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp, Statement s) throws IOException, SQLException {
        try{
            userInfoCommands.get(commands[2]).handle(req, resp, s);
        }catch (Exception e){
            //resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Invalid User Field\"}");
            return;
        }
    }
}

class getFullres implements getCommandHandler{
    int image_id;
    public getFullres(String[] commands){
        this.image_id = Integer.parseInt(commands[3]);
    }

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp, Statement s) throws IOException, SQLException {
        if (image_id == Integer.parseInt(null)) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"invalid image id\"}");
            return;
        }
        s.execute("SELECT image_path FROM Lpost_images WHERE image_id = " + image_id);
        ResultSet rs = s.getResultSet();
        String image_path = rs.getString("image_path");

    }
}
