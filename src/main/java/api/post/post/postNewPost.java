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
    public postNewPost(String[] commands){
        this.commands = commands;
    }
    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp, Statement s) throws Exception {
        if(!userAuthenticator.checkSession(req, resp, s.getConnection())){return;}
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
                String sql = "INSERT INTO Lposts (poster_id, post_name, category_id, cell_type_id, image_modality_id, category_user_picked, cell_type_user_picked, image_modality_user_picked, description) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

                // Use PreparedStatement with RETURN_GENERATED_KEYS
                try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    // Determine if any of the input don't use other correctly
                    int[] otherIds = checkOtherContradiction(s);

                    assert category_id != null;
                    assert otherIds != null;
                    System.out.println(category_id);
                    System.out.println(otherIds[0]);
                    System.out.println(category_user_picked);
                    System.out.println("============");
                    System.out.println(cell_type_id);
                    System.out.println(otherIds[1]);
                    System.out.println(cell_type_user_picked);
                    System.out.println("============");
                    System.out.println(image_modality_id);
                    System.out.println(otherIds[2]);
                    System.out.println(image_modality_user_picked);
                    System.out.println("============");
                    if(Integer.parseInt(category_id) != otherIds[0] && !Objects.equals(category_user_picked, "")){
                        throw new Exception("Only use user picked when category is other");
                    }
                    else if(Integer.parseInt(category_id) == otherIds[0] && Objects.equals(category_user_picked, "")){
                        throw new Exception("Requires user picked when category is other");
                    }
                    assert cell_type_id != null;
                    if(Integer.parseInt(cell_type_id) != otherIds[1] && !Objects.equals(cell_type_user_picked, "")){
                        throw new Exception("Only use user picked when cell_type is other");
                    }
                    else if(Integer.parseInt(cell_type_id) == otherIds[1] && Objects.equals(cell_type_user_picked, "")){
                        throw new Exception("Requires user picked when cell_type is other");
                    }
                    assert image_modality_id != null;
                    if(Integer.parseInt(image_modality_id) != otherIds[2] && !Objects.equals(image_modality_user_picked, "")){
                        throw new Exception("Only use user picked when image_modality is other");
                    }
                    else if(Integer.parseInt(image_modality_id) == otherIds[2] && Objects.equals(image_modality_user_picked, "")){
                        throw new Exception("Requires user picked when image_modality is other");
                    }

                    // Set the values for the placeholders
                    pstmt.setInt(1, Integer.parseInt(poster_id));
                    pstmt.setString(2, post_name);
                    assert category_id != null;
                    pstmt.setInt(3, Integer.parseInt(category_id));
                    assert cell_type_id != null;
                    pstmt.setInt(4, Integer.parseInt(cell_type_id));
                    assert image_modality_id != null;
                    pstmt.setInt(5, Integer.parseInt(image_modality_id));
                    pstmt.setString(6, category_user_picked);
                    pstmt.setString(7, cell_type_user_picked);
                    pstmt.setString(8, image_modality_user_picked);
                    pstmt.setString(9, description);

                    // Execute the insert
                    int rowsInserted = pstmt.executeUpdate();

                    if (rowsInserted > 0) {
                        // Retrieve the generated keys
                        try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                            if (generatedKeys.next()) {
                                long postId = generatedKeys.getLong(1); // Retrieve the auto-generated key
                                centralisedLogger.log("New post ID: " + postId);

                                // Send response with the new post ID
                                resp.setStatus(HttpServletResponse.SC_OK);
                                resp.getWriter().write("{\"post_id\":" + postId + "}");
                            } else {
                                handleError(resp, "Creating post failed, no ID obtained.", null);
                            }
                        }
                    } else {
                        handleError(resp, "Creating post failed, no rows affected.", null);

                    }
                }
            } catch (SQLException e){
                handleError(resp, "Error inserting into database", e);
            }


        }catch (Exception e){
            handleError(resp, String.valueOf(e), e);
        }
        }

        private int[] checkOtherContradiction(Statement s) throws SQLException {
            try {
                int[] otherIds = new int[3];
                // Query for lcategories
                ResultSet categoryResult = s.executeQuery("SELECT category_id FROM lcategories WHERE category_name = 'Other'");
                int categoryId = -1;
                if (categoryResult.next()) {
                    categoryId = categoryResult.getInt("category_id");
                }

                // Query for lcell_type
                ResultSet cellTypeResult = s.executeQuery("SELECT cell_type_id FROM lcell_types WHERE cell_type_name = 'Other'");
                int cellTypeId = -1;
                if (cellTypeResult.next()) {
                    cellTypeId = cellTypeResult.getInt("cell_type_id");
                }

                // Query for limage_modalities
                ResultSet imageModalityResult = s.executeQuery("SELECT image_modality_id FROM limage_modalities WHERE image_modality_name = 'Other'");
                int imageModalityId = -1;
                if (imageModalityResult.next()) {
                    imageModalityId = imageModalityResult.getInt("image_modality_id");
                }

                // Log the retrieved IDs
                System.out.println("Category ID: " + categoryId);
                System.out.println("Cell Type ID: " + cellTypeId);
                System.out.println("Image Modality ID: " + imageModalityId);
                otherIds[0] = categoryId;
                otherIds[1] = cellTypeId;
                otherIds[2] = imageModalityId;
                return otherIds;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }
}
