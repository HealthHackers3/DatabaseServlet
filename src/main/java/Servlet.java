import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.sql.*;
@WebServlet(urlPatterns={"/patients","/doctors"},loadOnStartup = 1)
public class Servlet extends HttpServlet {
    Connection conn;
    @Override
    public void init(ServletConfig config) throws ServletException {
        //String dbUrl = "jdbc:postgresql://localhost:5432/postgres";
        String dbUrl = "jdbc:postgresql://"+System.getenv("PGHOST")+":"+System.getenv("PGPORT")+"/"+System.getenv("PGDATABASE");
        System.out.println("Connecting to " + dbUrl);
        try {
            // Registers the driver
            Class.forName("org.postgresql.Driver");
        } catch (Exception e) {
        }
        try {
            //conn= DriverManager.getConnection(dbUrl, "postgres", "guardspine");
            conn= DriverManager.getConnection(dbUrl,System.getenv("PGUSER"),System.getenv("PGPASSWORD"));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        //setUpDatabase

    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("doGet");
        resp.setContentType("text/html");

    }
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //SQL EXECUTE
        String sqlQuery=req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        resp.setContentType("application/json");

        try (Statement s = conn.createStatement()){
            boolean wantsResponse = s.execute(sqlQuery);
            if(wantsResponse){
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
            else{
                int updateCount = s.getUpdateCount();
                resp.getWriter().write("Update successful :) Rows affected: " + updateCount);
            }

        } catch (SQLException e) {
            resp.getWriter().write("SQL Error :( : " + e.getMessage());
            e.printStackTrace();
        }


    }


}