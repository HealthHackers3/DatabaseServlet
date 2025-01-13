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
        resp.setContentType("application/json"); // Set response type to JSON
        String postName = req.getParameter("name"); // Retrieve the 'name' parameter from the request

        // Validate the 'name' parameter
        if (postName == null || postName.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Missing or empty 'name' parameter\"}");
            return;
        }

        // Query to check if the post name exists in the database
        String query = "SELECT COUNT(*) FROM Lposts WHERE post_name = ?";
        try (PreparedStatement pstmt = s.getConnection().prepareStatement(query)) {
            pstmt.setString(1, postName); // Set the parameter in the query

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Check if the name exists and return the result as JSON
                    boolean exists = rs.getInt(1) > 0;
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.getWriter().write("{\"exists\": " + exists + "}");
                } else {
                    // Handle unexpected database results
                    handleError(resp, "Unexpected result from database", new Exception("Database error"));
                }
            }
        } catch (Exception e) {
            // Handle unexpected errors
            handleError(resp, "Unexpected error", e);
        }
    }
}
