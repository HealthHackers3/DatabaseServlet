import com.google.gson.Gson;

import java.io.IOException;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.sql.*;
@WebServlet(urlPatterns={"/patients","/doctors"},loadOnStartup = 1)
public class Servlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("doGet");
        String la = "lalala";
        String dbUrl = "jdbc:postgresql://"+System.getenv("PGHOST")+":"+System.getenv("PGPORT")+"/"+System.getenv("PGDATABASE");
//        System.out.println(dbUrl);
//        try {
//            Class.forName("org.postgresql.Driver");
//            Connection conn= DriverManager.getConnection(dbUrl,System.getenv("PGUSER"),System.getenv("PGPASSWORD"));
//            Statement stmt = conn.createStatement();
//            ResultSet rs = stmt.executeQuery("SELECT * FROM patients");
//        } catch (Exception e) {
//        }
        resp.setContentType("text/html");
        resp.getWriter().write(dbUrl);
    }
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Read the bytes of the body – ie the message
        String reqBody=req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        Gson gson = new Gson();
        Patient p=gson.fromJson(reqBody,Patient.class);
        //resp.setContentType("application/json");
        resp.setContentType("text/html");
        resp.getWriter().write("Thank you client! "+reqBody);
    }


}