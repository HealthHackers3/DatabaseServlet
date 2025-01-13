package api.interfaces;

import com.google.gson.Gson;
import util.errorHandler;
import util.successHandler;

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
    // Primary method to handle API commands, implemented by subclasses
    void handle(HttpServletRequest req, HttpServletResponse resp, Statement s) throws Exception;

    // Converts a SQL ResultSet into JSON and writes it to the HTTP response
    default void statement2Json(HttpServletRequest req, HttpServletResponse resp, Statement s) throws SQLException, IOException {
        ResultSet rs = s.getResultSet();
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();
        List<Map<String, Object>> resultList = new ArrayList<>();

        // Iterate over rows in the ResultSet
        while (rs.next()) {
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                row.put(rsmd.getColumnName(i), rs.getObject(i));
            }
            resultList.add(row);
        }

        // Convert results to JSON and send the response
        String jsonResponse = new Gson().toJson(resultList);
        resp.getWriter().write(jsonResponse);
    }

    // Handles errors by invoking a utility class to manage error responses
    default void handleError(HttpServletResponse resp, String errorMessage, Exception e) throws SQLException, IOException {
        new util.errorHandler(resp, errorMessage, e);
    }

    // Handles success responses via a utility class
    default void handleSuccess(HttpServletResponse resp, String successMessage) throws SQLException, IOException {
        new util.successHandler(resp, successMessage);
    }
}

