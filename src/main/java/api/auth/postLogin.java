package api.auth;

import api.interfaces.apiCommandHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import util.centralisedLogger;
import util.passwordHasher;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

public class postLogin implements apiCommandHandler {
    String[] commands;

    public postLogin(String[] commands) {
        this.commands = commands;
    }

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp, java.sql.Statement s) throws IOException, SQLException {
        resp.setContentType("application/json");
        try {
            // Retrieve email and password from request parameters
            String email = req.getParameter("email");
            String password = req.getParameter("password");

            if (email == null || password == null) {
                // Missing credentials
                handleError(resp, "Missing username or password", new Exception("Missing username or password"));
                return;
            }

            // Query the database for the user
            String sql = "SELECT user_id, password FROM lusers WHERE email = ?";
            try (PreparedStatement ps = s.getConnection().prepareStatement(sql)) {
                ps.setString(1, email);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int userId = rs.getInt("user_id");
                        String hashedPassword = rs.getString("password");

                        // Verify password
                        if (passwordHasher.verifyPassword(password, hashedPassword)) {
                            // Create a session token
                            String sessionToken = UUID.randomUUID().toString();
                            long sessionDuration = 60 * 60 * 24; // 24 hours
                            long expiryTimestamp = System.currentTimeMillis() + (sessionDuration * 1000);

                            // Insert session into database
                            String insertSessionSql = "INSERT INTO Lsessions (user_id, session_token, expires_at) VALUES (?, ?, ?)";
                            try (PreparedStatement sessionPs = s.getConnection().prepareStatement(insertSessionSql)) {
                                sessionPs.setInt(1, userId);
                                sessionPs.setString(2, sessionToken);
                                sessionPs.setTimestamp(3, new java.sql.Timestamp(expiryTimestamp));
                                sessionPs.executeUpdate();
                            }

                            // Add session token as a cookie
                            Cookie sessionCookie = new Cookie("session_token", sessionToken);
                            sessionCookie.setHttpOnly(true);
                            sessionCookie.setPath("/");
                            resp.addCookie(sessionCookie);


                            centralisedLogger.log("User authenticated successfully: " + email);
                            resp.getWriter().write("{\"message\": \"Login successful\", \"userId\": " + userId + "}");
                        } else {
                            // Invalid credentials
                            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            resp.getWriter().write("{\"error\": \"Invalid username or password\"}");
                        }
                    } else {
                        // User not found
                        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        resp.getWriter().write("{\"error\": \"Invalid username or password\"}");
                    }
                }
            }
        } catch (SQLException e) {
            // Handle database errors
            handleError(resp, "Database error", e);
        }
    }
}
