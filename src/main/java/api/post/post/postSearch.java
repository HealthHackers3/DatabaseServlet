package api.post.post;

import api.interfaces.apiCommandHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import util.centralisedLogger;
import util.userAuthenticator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.*;
import java.util.stream.Collectors;

public class postSearch implements apiCommandHandler {
    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp, Statement s) throws Exception {
        if (!userAuthenticator.checkSession(req, resp, s.getConnection())) {
            return;
        }

        // Read the search query from the request body
        String searchQuery = req.getReader().lines().collect(Collectors.joining(System.lineSeparator())).trim();
        ObjectMapper objectMapper = new ObjectMapper();
        String searchString = objectMapper.readTree(searchQuery).get("query").asText();
        System.out.println("Search String: " + searchString);

        if (searchString.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Search query cannot be empty.\"}");
            return;
        }

        // SQL query combining Full-Text Search and Trigram Similarity
        String sql = "SELECT post_id, " +
                "ts_rank(to_tsvector('english', post_search_key), to_tsquery('english', ?)) AS full_text_rank, " +
                "similarity(post_search_key, ?) AS similarity_rank " +
                "FROM Lposts_composite " +
                "WHERE to_tsvector('english', post_search_key) @@ to_tsquery('english', ?) " +
                "OR post_search_key % ? " +
                "OR post_search_key ILIKE '%' || ? || '%' " + // Add ILIKE for substring matching
                "ORDER BY similarity_rank DESC, full_text_rank DESC";

        System.out.println(sql);
        try (Connection conn = s.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Prepare the search query
            String tsQuery = searchString.replace(" ", " & "); // Convert spaces to logical ANDs for tsquery syntax

            // Set placeholders
            pstmt.setString(1, tsQuery);  // For ts_rank
            pstmt.setString(2, searchString); // For similarity
            pstmt.setString(3, tsQuery);  // For Full-Text WHERE clause
            pstmt.setString(4, searchString); // For Trigram similarity
            pstmt.setString(5, searchString);// For ILIKE condition
            try (ResultSet rs = pstmt.executeQuery()) {
                StringBuilder resultJson = new StringBuilder();
                resultJson.append("[");

                boolean hasResults = false;
                while (rs.next()) {
                    hasResults = true;
                    int postId = rs.getInt("post_id");
                    float fullTextRank = rs.getFloat("full_text_rank");
                    float similarityRank = rs.getFloat("similarity_rank");
                    System.out.println(postId + " " + fullTextRank + " " + similarityRank);
                    // Append result to JSON array
                    resultJson.append(String.format("{\"post_id\":%d,\"full_text_rank\":%f,\"similarity_rank\":%f},", postId, fullTextRank, similarityRank));
                }

                if (hasResults) {
                    // Remove trailing comma
                    resultJson.setLength(resultJson.length() - 1);
                }
                resultJson.append("]");

                // Send the result back to the client
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write(resultJson.toString());
            }
        } catch (SQLException e) {
            handleError(resp, "Database error while searching posts", e);
        }
    }
}
