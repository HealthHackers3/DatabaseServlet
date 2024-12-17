import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.ServletConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.*;

@WebServlet(urlPatterns={"/patients","/doctors"},loadOnStartup = 1)
public class Servlet extends HttpServlet {
    private HikariDataSource dataSource;

    @Override
    public void init(ServletConfig config) {
        //String dbUrl = "jdbc:postgresql://localhost:5432/postgres";
        String dbUrl = "jdbc:postgresql://"+System.getenv("PGHOST")+":"+System.getenv("PGPORT")+"/"+System.getenv("PGDATABASE");
        System.out.println("Connecting to " + dbUrl);
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(dbUrl);
        hikariConfig.setUsername(System.getenv("PGUSER"));
        hikariConfig.setPassword(System.getenv("PGPASSWORD"));
        //hikariConfig.setUsername("postgres");
        //hikariConfig.setPassword("guardspine");
        hikariConfig.setMaximumPoolSize(10); // Set the maximum number of connections in the pool
        dataSource = new HikariDataSource(hikariConfig);

        //setUpDatabase

    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        System.out.println("doGet");
        resp.setContentType("text/html");
        resp.getWriter().write("Server Running");

    }
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String sqlQuery = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        resp.setContentType("application/json");

        try (Connection conn = dataSource.getConnection();
             Statement s = conn.createStatement()) {

            boolean wantsResponse = s.execute(sqlQuery);
            if (wantsResponse) {
                ResultSet rs = s.getResultSet();
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
            } else {
                int updateCount = s.getUpdateCount();
                resp.getWriter().write("Update successful :) Rows affected: " + updateCount);
            }

        } catch (SQLException e) {
            resp.getWriter().write("SQL Error :( : " + e.getMessage());
            e.printStackTrace();
        }
    }


}