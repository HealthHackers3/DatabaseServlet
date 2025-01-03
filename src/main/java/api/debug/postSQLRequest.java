package api.debug;

import api.interfaces.apiCommandHandler;
import com.google.gson.Gson;
import util.centralisedLogger;
import util.userAuthenticator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

public class postSQLRequest implements apiCommandHandler{
    private final String[] commands;
    public postSQLRequest(String[] commands) {
        this.commands = commands;

    }


    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp, Statement s) throws Exception {
        centralisedLogger.log("Command: " + Arrays.toString(commands));
        if(!userAuthenticator.checkSession(req, resp, s.getConnection())){return;}
        String sqlQuery = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        centralisedLogger.log("Executing SQL: " + sqlQuery);
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
            centralisedLogger.log("Update successful: " + updateCount + " row(s) affected");
        }
    }
}