package api.interfaces;

import com.google.gson.Gson;
import util.errorHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface apiCommandHandler {
    void handle(HttpServletRequest req, HttpServletResponse resp, Statement s) throws Exception;

    default void statement2Json(HttpServletRequest req, HttpServletResponse resp, Statement s) throws SQLException, IOException {
        ResultSet rs = s.getResultSet();
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();
        List<Map<String, Object>> resultList = new ArrayList<>();
        while(rs.next()){
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                String columnName = rsmd.getColumnName(i);
                Object columnValue = rs.getObject(i);
                row.put(columnName, columnValue);
            }
            resultList.add(row);
        }
        // Convert the result to JSON using Gson
        Gson gson = new Gson();
        String jsonResponse = gson.toJson(resultList);
        // Send the JSON response back to the client
        resp.getWriter().write(jsonResponse);
    }

    default void handleError(HttpServletResponse resp, String errorMessage, Exception e) throws SQLException, IOException {
        errorHandler errorHandler = new errorHandler(resp, errorMessage, e);
    }

}
