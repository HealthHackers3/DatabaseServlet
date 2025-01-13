package api.post.post;

import api.interfaces.apiCommandHandler;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import util.centralisedLogger;
import util.userAuthenticator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.sql.*;
import java.util.List;
import java.util.Objects;

public class postNewPost implements apiCommandHandler {
    private final String[] commands;

    public postNewPost(String[] commands) {
        this.commands = commands;
    }

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp, Statement s) throws Exception {
        if (!userAuthenticator.checkSession(req, resp, s.getConnection())) {
            return;
        }

        // Check if the request is multipart
        System.out.println("Content-Type: " + req.getContentType());
        boolean isMultipart = ServletFileUpload.isMultipartContent(req);
        if (!isMultipart) {
            handleError(resp, "Not multipart", null);
            return;
        }

        DiskFileItemFactory factory = new DiskFileItemFactory();
        factory.setRepository(new File(System.getProperty("java.io.tmpdir")));
        ServletFileUpload upload = new ServletFileUpload(factory);
        String poster_id = commands[2];
        System.out.println(poster_id);

        try {
            // Parse the request
            List<FileItem> formItems = upload.parseRequest(req);

            // Variables to store form data
            String post_name = null;
            String category_id = null;
            String cell_type_id = null;
            String image_modality_id = null;
            String category_user_picked = null;
            String cell_type_user_picked = null;
            String image_modality_user_picked = null;
            String description = null;

            // Process the items
            for (FileItem item : formItems) {
                if (item.isFormField()) {
                    // Process regular form fields
                    String fieldName = item.getFieldName();
                    String fieldValue = item.getString();

                    switch (fieldName) {
                        case "post_name":
                            post_name = fieldValue;
                            break;
                        case "category_id":
                            category_id = fieldValue;
                            break;
                        case "cell_type_id":
                            cell_type_id = fieldValue;
                            break;
                        case "image_modality_id":
                            image_modality_id = fieldValue;
                            break;
                        case "category_user_picked":
                            category_user_picked = fieldValue;
                            break;
                        case "cell_type_user_picked":
                            cell_type_user_picked = fieldValue;
                            break;
                        case "image_modality_user_picked":
                            image_modality_user_picked = fieldValue;
                            break;
                        case "description":
                            description = fieldValue;
                            break;
                        default:
                            // Log unexpected fields
                            centralisedLogger.log("Unexpected form field: " + fieldName);
                            break;
                    }
                }
            }

            try (Connection conn = s.getConnection()) {
                conn.setAutoCommit(false); // Enable transaction

                // Insert into Lposts
                String sql = "INSERT INTO Lposts (poster_id, post_name, category_id, cell_type_id, image_modality_id, category_user_picked, cell_type_user_picked, image_modality_user_picked, description) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

                try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    int[] otherIds = checkOtherContradiction(s);

                    validateInput(category_id, category_user_picked, otherIds[0], "category");
                    validateInput(cell_type_id, cell_type_user_picked, otherIds[1], "cell_type");
                    validateInput(image_modality_id, image_modality_user_picked, otherIds[2], "image_modality");

                    pstmt.setInt(1, Integer.parseInt(poster_id));
                    pstmt.setString(2, post_name);
                    pstmt.setInt(3, Integer.parseInt(category_id));
                    pstmt.setInt(4, Integer.parseInt(cell_type_id));
                    pstmt.setInt(5, Integer.parseInt(image_modality_id));
                    pstmt.setString(6, category_user_picked);
                    pstmt.setString(7, cell_type_user_picked);
                    pstmt.setString(8, image_modality_user_picked);
                    pstmt.setString(9, description);

                    int rowsInserted = pstmt.executeUpdate();

                    if (rowsInserted > 0) {
                        try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                            if (generatedKeys.next()) {
                                long postId = generatedKeys.getLong(1);

                                // Create the post_search_key
                                String postSearchKey = createPostSearchKey(postId, post_name, Integer.parseInt(poster_id), cell_type_id, cell_type_user_picked, image_modality_id, image_modality_user_picked, description, conn);

                                // Insert into Lposts_composite
                                insertIntoCompositeTable(postId, postSearchKey, conn);

                                centralisedLogger.log("New post ID: " + postId);

                                conn.commit(); // Commit transaction

                                resp.setStatus(HttpServletResponse.SC_OK);
                                resp.getWriter().write("{\"post_id\":" + postId + "}");
                            } else {
                                conn.rollback();
                                handleError(resp, "Creating post failed, no ID obtained.", null);
                            }
                        }
                    } else {
                        conn.rollback();
                        handleError(resp, "Creating post failed, no rows affected.", null);
                    }
                } catch (SQLException e) {
                    conn.rollback();
                    handleError(resp, "Error inserting into database", e);
                }
            }
        } catch (Exception e) {
            handleError(resp, String.valueOf(e), e);
        }
    }

    private String createPostSearchKey(long postId, String post_name, int posterId, String cellTypeId, String cellTypeUserPicked, String imageModalityId, String imageModalityUserPicked, String description, Connection conn) throws SQLException {
        // Resolve names
        String posterName = resolvePosterName(String.valueOf(posterId), conn); // Assuming poster_id is already a string
        String cellTypeName = resolveName(cellTypeId, cellTypeUserPicked, "Lcell_types", "cell_type_name", "cell_type_id", conn);
        String imageModalityName = resolveName(imageModalityId, imageModalityUserPicked, "Limage_modalities", "image_modality_name", "image_modality_id", conn);

        // Preprocess post_name to split camel-case words
        String processedPostName = splitCamelCase(post_name);

        // Concatenate components into the search key
        return String.join(" ", posterName, processedPostName, cellTypeName, imageModalityName, description);
    }
    private String splitCamelCase(String input) {
        if (input == null || input.isEmpty()) {
            return input; // Return as-is if null or empty
        }
        // Use regex to add a space between lowercase-uppercase transitions
        return input.replaceAll("([a-z])([A-Z])", "$1 $2");
    }

    private String resolvePosterName(String id, Connection conn) throws SQLException {
        String sql = "SELECT username FROM lusers WHERE user_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, Integer.parseInt(id));
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        }
        return "";
    }
    private String resolveName(String id, String userPicked, String tableName, String nameColumn, String idColumn, Connection conn) throws SQLException {
        if ("1".equals(id)) {
            return userPicked; // Custom user-picked value
        }

        String sql = "SELECT " + nameColumn + " FROM " + tableName + " WHERE " + idColumn + " = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, Integer.parseInt(id));
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        }
        return "";
    }

    private void insertIntoCompositeTable(long postId, String postSearchKey, Connection conn) throws SQLException {
        String sql = "INSERT INTO Lposts_composite (post_id, post_search_key) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, postId);
            pstmt.setString(2, postSearchKey);
            pstmt.executeUpdate();
        }
    }

    private void validateInput(String id, String userPicked, int otherId, String type) throws Exception {
        if (Integer.parseInt(id) != otherId && !Objects.equals(userPicked, "")) {
            throw new Exception("Only use user-picked when " + type + " is 'Other'");
        } else if (Integer.parseInt(id) == otherId && Objects.equals(userPicked, "")) {
            throw new Exception("Requires user-picked when " + type + " is 'Other'");
        }
    }

    private int[] checkOtherContradiction(Statement s) throws SQLException {
        int[] otherIds = new int[3];
        ResultSet categoryResult = s.executeQuery("SELECT category_id FROM lcategories WHERE category_name = 'Other'");
        otherIds[0] = categoryResult.next() ? categoryResult.getInt("category_id") : -1;

        ResultSet cellTypeResult = s.executeQuery("SELECT cell_type_id FROM lcell_types WHERE cell_type_name = 'Other'");
        otherIds[1] = cellTypeResult.next() ? cellTypeResult.getInt("cell_type_id") : -1;

        ResultSet imageModalityResult = s.executeQuery("SELECT image_modality_id FROM limage_modalities WHERE image_modality_name = 'Other'");
        otherIds[2] = imageModalityResult.next() ? imageModalityResult.getInt("image_modality_id") : -1;

        return otherIds;
    }
}
