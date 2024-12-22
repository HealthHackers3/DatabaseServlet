
import com.zaxxer.hikari.HikariDataSource;
import getRequests.getHandler;
import postRequests.postHandler;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
import java.util.*;

@WebServlet(urlPatterns = {"/api/*"}, loadOnStartup = 1)
@MultipartConfig (
        fileSizeThreshold = 1024 * 1024 * 1,
        maxFileSize = 1024 * 1024 * 10,
        maxRequestSize = 1024 * 1024 * 100
)
public class Servlet_Local extends HttpServlet {
    private HikariDataSource dataSource;
    private String dbUrl = "jdbc:postgresql://localhost:5432/postgres"; //connection to SQL Db URL
    @Override
    public void init(ServletConfig config) {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("PostgresSQL Driver not found", e);
        }

        // HikariCP Configuration
        //String dbUrl = "jdbc:postgresql://" + System.getenv("PGHOST") + ":" + System.getenv("PGPORT") + "/" + System.getenv("PGDATABASE");
        String dbUrl = "jdbc:postgresql://localhost:5432/postgres";
        System.out.println("Connecting to " + dbUrl);

//        HikariConfig hikariConfig = new HikariConfig();
//        hikariConfig.setJdbcUrl(dbUrl);
////        hikariConfig.setUsername(System.getenv("PGUSER"));
////        hikariConfig.setPassword(System.getenv("PGPASSWORD"));
//        hikariConfig.setUsername("postgres");
//        hikariConfig.setPassword("guardspine");
//        hikariConfig.setMaximumPoolSize(10);          // Max connections in the pool
//        hikariConfig.setMinimumIdle(2);               // Minimum idle connections
//        hikariConfig.setIdleTimeout(30000);           // Close idle connections after 30 seconds
//        hikariConfig.setConnectionTimeout(30000);     // Wait max 30 seconds for a connection
//        hikariConfig.setLeakDetectionThreshold(20000);// Detect leaks after 20 seconds
//
//        dataSource = new HikariDataSource(hikariConfig);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json"); //Respond in JSON format
        String[] pathComponenets = req.getPathInfo().substring(1).split("/");//Remove the first character (always '/') then split an array at every subsequent '/' to get the command specifics

        try (Connection conn = DriverManager.getConnection(dbUrl, "postgres", "guardspine"); Statement s = conn.createStatement()) { //attempt SQL Db connection with statement s for SQL queries
            System.out.println("Executing GET query at: " + Arrays.toString(pathComponenets));
              getHandler gH = new getHandler(pathComponenets, req, resp, s);//create new getHandler to do deal with the get requeset.
                gH.execute();
        } catch (SQLException e) {
            resp.getWriter().write("{\"error\": \"Issue connecting to PostgreSQL from server\"}");
        }
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json"); //Respond in JSON format
        String[] pathComponenets = req.getPathInfo().substring(1).split("/");//Remove the first character (always '/') then split an array at every subsequent '/' to get the command specifics
        try (Connection conn = DriverManager.getConnection(dbUrl, "postgres", "guardspine"); Statement s = conn.createStatement()) { //attempt SQL Db connection with statement s for SQL queries
            System.out.println("Executing POST query at: " + Arrays.toString(pathComponenets));
            postHandler pH = new postHandler(pathComponenets, req, resp, s);//create new getHandler to do deal with the get requeset.
            pH.execute();
        } catch (SQLException e) {
            resp.getWriter().write("{\"error\": \"Issue connecting to PostgreSQL from server\"}");
        } catch (ServletException e) {
            throw new RuntimeException(e);
        }
    }

}
