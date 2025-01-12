package api.post.get;

import api.interfaces.apiCommandHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class getCheckName implements apiCommandHandler {

    public getCheckName(String[] commands) {
    }

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp, Statement s) throws Exception {
        resp.setContentType("application/json");
        String postName = req.getParameter("name"); // Retrieve the 'name' parameter from the request

        if (postName == null || postName.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Missing or empty 'name' parameter\"}");
            return;
        }

        String query = "SELECT COUNT(*) FROM Lposts WHERE post_name = ?";
        try (PreparedStatement pstmt = s.getConnection().prepareStatement(query)) {
            pstmt.setString(1, postName);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    boolean exists = count > 0;

                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.getWriter().write("{\"exists\": " + exists + "}");
                } else {
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    resp.getWriter().write("{\"error\": \"Unexpected result from database\"}");
                }
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
            e.printStackTrace();
        }
    }
}
