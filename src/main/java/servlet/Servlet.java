package servlet;

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

@WebServlet(urlPatterns = {"/api/*"}, loadOnStartup = 1)
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = 1024 * 1024 * 10,
        maxRequestSize = 1024 * 1024 * 100
)
public class Servlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(Servlet.class);
    private HikariDataSource dataSource;

    @Override
    public void init(ServletConfig config) {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("PostgreSQL Driver not found", e);
        }

        String dbUrl = "jdbc:postgresql://" + System.getenv("PGHOST") + ":" + System.getenv("PGPORT") + "/" + System.getenv("PGDATABASE");
        log.info("Connecting to {}", dbUrl);

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(dbUrl);
        hikariConfig.setUsername(System.getenv("PGUSER"));
        hikariConfig.setPassword(System.getenv("PGPASSWORD"));
        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.setMinimumIdle(2);
        hikariConfig.setIdleTimeout(30000);
        hikariConfig.setConnectionTimeout(30000);
        hikariConfig.setLeakDetectionThreshold(20000);

        dataSource = new HikariDataSource(hikariConfig);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        try (Connection conn = dataSource.getConnection(); Statement s = conn.createStatement()) {
            String[] pathComponents = req.getPathInfo().substring(1).split("/");
            log.info("Executing GET query at: {}", Arrays.toString(pathComponents));
            new getHandler(pathComponents, req, resp, s).execute();
        } catch (SQLException e) {
            log.error("Database error", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"A database error occurred.\"}");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        try (Connection conn = dataSource.getConnection(); Statement s = conn.createStatement()) {
            String[] pathComponents = req.getPathInfo().substring(1).split("/");
            log.info("Executing POST query at: {}", Arrays.toString(pathComponents));
            new postHandler(pathComponents, req, resp, s).execute();
        } catch (SQLException e) {
            log.error("Database error", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"A database error occurred.\"}");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void destroy() {
        if (dataSource != null) {
            dataSource.close();
            log.info("HikariCP DataSource closed.");
        }
    }
}
