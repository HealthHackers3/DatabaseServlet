//import com.google.gson.Gson;
//import com.zaxxer.hikari.HikariConfig;
//import com.zaxxer.hikari.HikariDataSource;
//
//import javax.servlet.ServletConfig;
//import javax.servlet.annotation.WebServlet;
//import javax.servlet.http.HttpServlet;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.debug.*;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//@WebServlet(urlPatterns = {"/serverstatus"}, loadOnStartup = 1)
//public class Servlet extends HttpServlet {
//    private HikariDataSource dataSource;
//
//    @Override
//    public void init(ServletConfig config) {
//        try {
//            Class.forName("org.postgresql.Driver");
//        } catch (ClassNotFoundException e) {
//            throw new RuntimeException("PostgreSQL Driver not found", e);
//        }
//
//        // HikariCP Configuration
//        String dbUrl = "jdbc:postgresql://" + System.getenv("PGHOST") + ":" + System.getenv("PGPORT") + "/" + System.getenv("PGDATABASE");
//        System.out.println("Connecting to " + dbUrl);
//
//        HikariConfig hikariConfig = new HikariConfig();
//        hikariConfig.setJdbcUrl(dbUrl);
//        hikariConfig.setUsername(System.getenv("PGUSER"));
//        hikariConfig.setPassword(System.getenv("PGPASSWORD"));
//        hikariConfig.setMaximumPoolSize(10);          // Max connections in the pool
//        hikariConfig.setMinimumIdle(2);               // Minimum idle connections
//        hikariConfig.setIdleTimeout(30000);           // Close idle connections after 30 seconds
//        hikariConfig.setConnectionTimeout(30000);     // Wait max 30 seconds for a connection
//        hikariConfig.setLeakDetectionThreshold(20000);// Detect leaks after 20 seconds
//
//        dataSource = new HikariDataSource(hikariConfig);
//    }
//
//    @Override
//    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
//        System.out.println(req.getPathInfo());
//        System.out.println("doGet");
//        resp.setContentType("text/html");
//        resp.getWriter().write("Server Running");
//    }
//
//    @Override
//    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
//        String sqlQuery = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
//        resp.setContentType("application/json");
//        System.out.println(req.getPathInfo());
//
//        try (Connection conn = dataSource.getConnection();
//             Statement s = conn.createStatement()) {
//
//            // Set query timeout (e.g., 30 seconds)
//            s.setQueryTimeout(30);
//
//            boolean wantsResponse = s.execute(sqlQuery);
//            if (wantsResponse) {
//                try (ResultSet rs = s.getResultSet()) { // Auto-close ResultSet
//                    ResultSetMetaData rsmd = rs.getMetaData();
//                    int columnCount = rsmd.getColumnCount();
//                    List<Map<String, Object>> resultList = new ArrayList<>();
//
//                    while (rs.next()) {
//                        Map<String, Object> row = new HashMap<>();
//                        for (int i = 1; i <= columnCount; i++) {
//                            String columnName = rsmd.getColumnName(i);
//                            Object columnValue = rs.getObject(i);
//                            row.put(columnName, columnValue);
//                        }
//                        resultList.add(row);
//                    }
//
//                    Gson gson = new Gson();
//                    String jsonResponse = gson.toJson(resultList);
//                    resp.getWriter().write(jsonResponse);
//                }
//            } else {
//                int updateCount = s.getUpdateCount();
//                resp.getWriter().write("Update successful: " + updateCount + " row(s) affected");
//            }
//
//        } catch (SQLException e) {
//            // Improved error logging
//            resp.getWriter().write(e.getMessage());
//            e.printStackTrace();
//        }
//    }
//
//    @Override
//    public void destroy() {
//        if (dataSource != null) {
//            dataSource.close();
//            System.out.println("HikariCP DataSource closed.");
//        }
//    }
//}
