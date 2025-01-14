package servlet;

// Importing necessary libraries
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import api.handlers.getHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import api.handlers.postHandler;

import javax.servlet.ServletConfig;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
import java.util.*;
//increment
// Annotation to map the servlet to the "/api/*" URL and configure file upload limits
@WebServlet(urlPatterns = {"/api/*"}, loadOnStartup = 1)
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,       // Files larger than this are written to disk
        maxFileSize = 1024 * 1024 * 10,        // Maximum file size: 10 MB
        maxRequestSize = 1024 * 1024 * 100     // Maximum request size: 100 MB
)
//increm
public class Servlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(Servlet.class); // Logger for debugging
    private HikariDataSource dataSource; // HikariCP DataSource for database connection pooling

    // Initialize the servlet and configure the database connection pool
    @Override
    public void init(ServletConfig config) {
        try {
            // Load PostgreSQL driver
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("PostgreSQL Driver not found", e);
        }

        // Build the database URL using environment variables
        String dbUrl = "jdbc:postgresql://" + System.getenv("PGHOST") + ":" + System.getenv("PGPORT") + "/" + System.getenv("PGDATABASE");
        log.info("Connecting to {}", dbUrl);

        // Configure HikariCP for connection pooling
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(dbUrl);
        hikariConfig.setUsername(System.getenv("PGUSER")); // Database username from environment variables
        hikariConfig.setPassword(System.getenv("PGPASSWORD")); // Database password from environment variables
        hikariConfig.setMaximumPoolSize(10);               // Maximum number of connections
        hikariConfig.setMinimumIdle(2);                   // Minimum number of idle connections
        hikariConfig.setIdleTimeout(30000);               // Idle timeout in milliseconds
        hikariConfig.setConnectionTimeout(30000);         // Connection timeout in milliseconds
        hikariConfig.setLeakDetectionThreshold(20000);    // Leak detection threshold in milliseconds

        // Initialize the DataSource
        dataSource = new HikariDataSource(hikariConfig);
    }

    // Handles HTTP GET requests
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setHeader("Access-Control-Allow-Origin", "http://localhost:3000"); // Allow CORS for specific origin
        resp.setContentType("application/json"); // Set response content type to JSON

        try (Connection conn = dataSource.getConnection(); Statement s = conn.createStatement()) {
            // Parse the request path into components
            String[] pathComponents = req.getPathInfo().substring(1).split("/");
            log.info("Executing GET query at: {}", Arrays.toString(pathComponents));

            // Delegate handling to the `getHandler`
            new getHandler(pathComponents, req, resp, s).execute();
        } catch (SQLException e) {
            // Log and handle database errors
            log.error("Database error", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"A database error occurred.\"}");
        } catch (Exception e) {
            // Handle unexpected errors
            throw new RuntimeException(e);
        }
    }

    // Handles HTTP POST requests
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setHeader("Access-Control-Allow-Origin", "http://localhost:3000"); // Allow CORS for specific origin
        resp.setContentType("application/json"); // Set response content type to JSON

        try (Connection conn = dataSource.getConnection(); Statement s = conn.createStatement()) {
            // Parse the request path into components
            String[] pathComponents = req.getPathInfo().substring(1).split("/");
            log.info("Executing POST query at: {}", Arrays.toString(pathComponents));

            // Delegate handling to the `postHandler`
            new postHandler(pathComponents, req, resp, s).execute();
        } catch (SQLException e) {
            // Log and handle database errors
            log.error("Database error", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"A database error occurred.\"}");
        } catch (Exception e) {
            // Handle unexpected errors
            throw new RuntimeException(e);
        }
    }

    // Cleans up resources when the servlet is destroyed
    @Override
    public void destroy() {
        if (dataSource != null) {
            dataSource.close(); // Close the HikariCP DataSource
            log.info("HikariCP DataSource closed.");
        }
    }
}
