package api.debug;

import api.interfaces.apiCommandHandler;
import com.google.gson.Gson;
import util.centralisedLogger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

public class postSQLRequest implements apiCommandHandler {
    private final String[] commands;

    public postSQLRequest(String[] commands) {
        this.commands = commands;
    }

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp, Statement s) throws Exception {
        // Log the command for debugging
        centralisedLogger.log("Command: " + Arrays.toString(commands));


        // Read the SQL query from the request body
        String sqlQuery = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        centralisedLogger.log("Executing SQL: " + sqlQuery);

        // Execute the SQL query
        try {
            boolean wantsResponse = s.execute(sqlQuery);

            if (wantsResponse) {
                // Handle SELECT queries and return the result set
                try (ResultSet rs = s.getResultSet()) {
                    ResultSetMetaData rsmd = rs.getMetaData();
                    int columnCount = rsmd.getColumnCount();
                    List<Map<String, Object>> resultList = new ArrayList<>();

                    // Process each row in the result set
                    while (rs.next()) {
                        Map<String, Object> row = new HashMap<>();
                        for (int i = 1; i <= columnCount; i++) {
                            String columnName = rsmd.getColumnName(i);
                            Object columnValue = rs.getObject(i);
                            row.put(columnName, columnValue);
                        }
                        resultList.add(row);
                    }

                    // Convert the result set to JSON and write it to the response
                    Gson gson = new Gson();
                    String jsonResponse = gson.toJson(resultList);
                    resp.getWriter().write(jsonResponse);
                }
            } else {
                // Handle non-SELECT queries (e.g., INSERT, UPDATE, DELETE)
                int updateCount = s.getUpdateCount();
                resp.getWriter().write("Update successful: " + updateCount + " row(s) affected");
                centralisedLogger.log("Update successful: " + updateCount + " row(s) affected");
            }
        }catch (Exception e) {
            handleError(resp, "Error executing SQL", e);
        }
    }
}
