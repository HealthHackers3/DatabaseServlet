package postRequests;
import com.google.gson.Gson;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class postHandler {
    //class variables
    private final Map<String, postCommandHandler> commandHandlers = new HashMap<>();
    HttpServletRequest req;
    HttpServletResponse resp;
    String[] commands;
    Statement s;

    public postHandler(String[] commandPath, HttpServletRequest req, HttpServletResponse resp, Statement s) throws SQLException, IOException {
        this.req = req;
        this.resp = resp;
        this.s = s;
        this.commands = commandPath;

        //define get commands commands
        commandHandlers.put("sqlraw", new postSQLCommand(commands));
        commandHandlers.put("img", new postNewPost(commands));

    }

    //execute command
    public void execute() throws IOException, SQLException, ServletException {
        //check if command exists in the map from before
        if (commands[0] == null || !commandHandlers.containsKey(commands[0])) {
            //resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Invalid or missing command\"}");
            return;
        }
        //run class of command
        commandHandlers.get(commands[0]).handle(req, resp, s);
    }
}

class postSQLCommand implements postCommandHandler {
    private final String[] commands;
    public postSQLCommand(String[] commands) {
        this.commands = commands;
    }
    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp, Statement s) throws IOException, SQLException {
        String sqlQuery = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        boolean wantsResponse = s.execute(sqlQuery);
            if (wantsResponse) {
                try (ResultSet rs = s.getResultSet()) { // Auto-close ResultSet
                    ResultSetMetaData rsmd = rs.getMetaData();
                    int columnCount = rsmd.getColumnCount();
                    List<Map<String, Object>> resultList = new ArrayList<>();

                    while (rs.next()) {
                        Map<String, Object> row = new HashMap<>();
                        for (int i = 1; i <= columnCount; i++) {
                            String columnName = rsmd.getColumnName(i);
                            Object columnValue = rs.getObject(i);
                            row.put(columnName, columnValue);
                        }
                        resultList.add(row);
                    }

                    Gson gson = new Gson();
                    String jsonResponse = gson.toJson(resultList);
                    resp.getWriter().write(jsonResponse);
                }
            } else {
                int updateCount = s.getUpdateCount();
                resp.getWriter().write("Update successful: " + updateCount + " row(s) affected");
            }
    }
}
