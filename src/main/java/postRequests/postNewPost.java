package postRequests;

import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.disk.*;
import org.apache.commons.fileupload.servlet.*;
import org.apache.commons.io.FilenameUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;


public class postNewPost implements postCommandHandler {

    private static final String UPLOAD_DIRECTORY = "var/uploads"; // Directory to store images

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp, Statement s) throws IOException, SQLException, ServletException {
        System.out.println("Handling the new post request");
        Part filePart = req.getPart("file");
        String fileName = filePart.getSubmittedFileName();
        for (Part part : req.getParts()) {
            part.write(UPLOAD_DIRECTORY + "/" + fileName);
        }
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write("{\"message\": \"Successfully Uploaded\"}");
    }

//        req.getHeaderNames().asIterator().forEachRemaining(header ->
//                System.out.println(header + ": " + req.getHeader(header))
//        );
//
//        System.out.println("Content-Type: " + req.getContentType());
//        System.out.println("=================");
//        InputStream inputStream = req.getInputStream();
//        byte[] data = new byte[inputStream.available()];
//        inputStream.read(data);
//
//// Convert byte array to string
//        String rawBody = new String(data, StandardCharsets.UTF_8);
//
//// Log the raw body to check the form data
//        System.out.println("Raw request body: " + rawBody);
//        // Check if the request is multipart (contains file data)
//        if (ServletFileUpload.isMultipartContent(req)) {
//            // Create a file item factory and configure upload handler
//            DiskFileItemFactory factory = new DiskFileItemFactory();
//            factory.setSizeThreshold(DiskFileItemFactory.DEFAULT_SIZE_THRESHOLD);
//
//            ServletFileUpload upload = new ServletFileUpload(factory);
//            upload.setFileSizeMax(1024 * 1024 * 10); // Max file size (10MB)
//            upload.setSizeMax(1024 * 1024 * 50); // Max request size (50MB)
//
//            try {
//                // Parse the request
//                List<FileItem> formItems = upload.parseRequest(req);
//
//                String title = null;
//                String description = null;
//                String categoryId = null;
//                String cellTypeId = null;
//                File imageFile = null;
//
//                System.out.println(formItems.size());
//                System.out.println(formItems);
//                // Iterate over form items to extract metadata and image
//                for (FileItem item : formItems) {
//                    System.out.println(item.getFieldName());
//                    if (item.isFormField()) {
//                        // Handle form fields (metadata)
//                        if ("title".equals(item.getFieldName())) {
//                            System.out.println("Test Orange1");
//                            title = item.getString();
//                        } else if ("description".equals(item.getFieldName())) {
//                            System.out.println("Test Orange2");
//                            description = item.getString();
//                        } else if ("category_id".equals(item.getFieldName())) {
//                            System.out.println("Test Orange3");
//                            categoryId = item.getString();
//                        } else if ("cell_type_id".equals(item.getFieldName())) {
//                            System.out.println("Test Orange4");
//                            cellTypeId = item.getString();
//                        }
//                    } else {
//                        System.out.println("Test Orange5");
//                        // Handle file field (image)
//                        if ("image".equals(item.getFieldName())) {
//                            System.out.println("Test Cyan");
//                            // Get the image file name and save it locally
//                            String fileName = FilenameUtils.getName(item.getName());
//                            String filePath = req.getServletContext().getRealPath(UPLOAD_DIRECTORY) + File.separator + fileName;
//                            File storeFile = new File(filePath);
//                            System.out.println(filePath);
//                            //item.write(storeFile); // Write the file to the disk
//                            imageFile = storeFile;
//                        }
//                    }
//                }
//                // At this point, metadata and imageFile are ready to be saved
//                if (title != null && description != null && categoryId != null && cellTypeId != null && imageFile != null) {
//                    // Save the metadata and image path to the database
//                    saveMetadataToDatabase(s, title, description, categoryId, cellTypeId, imageFile);
//
//                    // Respond with success message
//                    resp.getWriter().write("{\"message\": \"Post created successfully!\"}");
//                } else {
//                    resp.setContentType("application/json");
//                    resp.setCharacterEncoding("UTF-8");
//                    resp.getWriter().write("{\"error\": \"Missing required fields or file.\"}");
//                }
//
//            } catch (Exception ex) {
//                resp.setContentType("application/json");
//                resp.setCharacterEncoding("UTF-8");
//                resp.getWriter().write("{\"error\": \"Error uploading post: " + ex.getMessage() + "\"}");
//            }
//        } else {
//            resp.setContentType("application/json");
//            resp.setCharacterEncoding("UTF-8");
//            resp.getWriter().write("{\"error\": \"Request is not multipart.\"}");
//        }
//    }

//    private void saveMetadataToDatabase(Statement s, String title, String description, String categoryId, String cellTypeId, File imageFile) throws SQLException {
//        // SQL query to insert the metadata and image path into the database
//        String sql = "INSERT INTO posts (title, description, category_id, cell_type_id, image_path) VALUES (?, ?, ?, ?, ?)";
//        System.out.println(title + ":" + description + ":" + categoryId + ":" + cellTypeId + ":" + imageFile);
////        try (PreparedStatement pstmt = s.getConnection().prepareStatement(sql)) {
//////            pstmt.setString(1, title);
//////            pstmt.setString(2, description);
//////            pstmt.setString(3, categoryId);
//////            pstmt.setString(4, cellTypeId);
//////            pstmt.setString(5, imageFile.getAbsolutePath());
//////            System.out.println("");// Save the file path to the database
////
////            //pstmt.executeUpdate(); // Execute the insert
////        }
//    }
}
