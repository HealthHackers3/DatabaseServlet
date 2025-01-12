package servlet;

import api.handlers.deleteHandler;
import com.zaxxer.hikari.HikariDataSource;
import api.handlers.getHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import api.handlers.postHandler;
import util.centralisedLogger;
import util.errorHandler;
import util.userAuthenticator;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
import java.util.*;

@WebServlet(urlPatterns = {"/api/*"}, loadOnStartup = 1)
@MultipartConfig ( //set image upload thresholds
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = 1024 * 1024 * 10,
        maxRequestSize = 1024 * 1024 * 100
)

public class Servlet_Local extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(Servlet_Local.class);
    private HikariDataSource dataSource;
    private String dbUrl = "jdbc:postgresql://localhost:5432/postgres"; //connection to SQL Db URL

    @Override
    public void init(ServletConfig config) {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("PostgresSQL Driver not found", e);
        }

        String dbUrl = "jdbc:postgresql://localhost:5432/postgres";
        System.out.println("Connecting to " + dbUrl);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json"); //Respond in JSON format
        try{
            String[] pathComponents = req.getPathInfo().substring(1).split("/");//Remove the first character (always '/') then split an array at every subsequent '/' to get the command specifics
            try (Connection conn = DriverManager.getConnection(dbUrl, "postgres", "guardspine"); Statement s = conn.createStatement()) { //attempt SQL Db connection with statement s for SQL queries
                System.out.println("Executing GET query at: " + Arrays.toString(pathComponents));
                getHandler gH = new getHandler(pathComponents, req, resp, s);//create new getHandler to do deal with the get requeset.
                gH.execute();
            } catch (SQLException e) {
                new errorHandler(resp, "{\"error\": \"Issue connecting to PostgreSQL from server\"}", e);
            }
        }catch(Exception e){
            new errorHandler(resp, "{\"error\": \"Invalid request path\"}", e);
        }



    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        resp.setContentType("application/json"); //Respond in JSON format
        String[] pathComponents = req.getPathInfo().substring(1).split("/");//Remove the first character (always '/') then split an array at every subsequent '/' to get the command specifics
        try (Connection conn = DriverManager.getConnection(dbUrl, "postgres", "guardspine"); Statement s = conn.createStatement()) { //attempt SQL Db connection with statement s for SQL queries
            System.out.println("Executing POST query at: " + Arrays.toString(pathComponents));
            postHandler pH = new postHandler(pathComponents, req, resp, s);//create new getHandler to do deal with the get requeset.
            pH.execute();
        } catch (SQLException e) {
            resp.getWriter().write("{\"error\": \"Issue connecting to PostgreSQL from server\"}");
        } catch (ServletException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        resp.setContentType("application/json"); //Respond in JSON format
        String[] pathComponents = req.getPathInfo().substring(1).split("/");//Remove the first character (always '/') then split an array at every subsequent '/' to get the command specifics
        try (Connection conn = DriverManager.getConnection(dbUrl, "postgres", "guardspine"); Statement s = conn.createStatement()) { //attempt SQL Db connection with statement s for SQL queries
            System.out.println("Executing DELETE query at: " + Arrays.toString(pathComponents));
            deleteHandler dH = new deleteHandler(pathComponents, req, resp, s);//create new getHandler to do deal with the get requeset.
            dH.execute();
        } catch (SQLException e) {
            resp.getWriter().write("{\"error\": \"Issue connecting to PostgreSQL from server\"}");
        } catch (ServletException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



}
