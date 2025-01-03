package util;

import javax.imageio.IIOException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

//if(!userAuthenticator.checkSession(request, response, s.getConnection())){return;}
//add this line to a handle to make it check the session
public class userAuthenticator{
    public static boolean checkSession(HttpServletRequest req, HttpServletResponse resp, Connection conn) throws Exception {

        if (!isAuthenticated(req, conn)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"error\": \"Unauthorized: Please log in\"}");
            return false;
        }
        return true;
    }
    private static boolean isAuthenticated(HttpServletRequest req, Connection conn) {
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("session_token".equals(cookie.getName())) {
                    String sessionToken = cookie.getValue();

                    // Validate session token in the database
                    String sql = "SELECT COUNT(*) FROM Lsessions WHERE session_token = ? AND expires_at > NOW()";
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setString(1, sessionToken);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next() && rs.getInt(1) > 0) {
                                return true; // Valid session
                            }
                        }
                    } catch (SQLException e) {
                        centralisedLogger.log("Error validating session token: " + e.getMessage());
                    }
                }
            }
        }
        return false; // No valid session token
    }
}