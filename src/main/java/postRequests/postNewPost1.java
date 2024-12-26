//package postRequests;
//
//import org.apache.commons.fileupload.FileItem;
//import org.apache.commons.fileupload.FileUploadException;
//import org.apache.commons.fileupload.disk.DiskFileItemFactory;
//import org.apache.commons.fileupload.servlet.ServletFileUpload;
//import org.apache.commons.io.FilenameUtils;
//
//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.File;
//import java.io.IOException;
//import java.sql.Statement;
//import java.util.List;
//
//public class postNewPost1 implements postCommandHandler {
//
//    //private static final String UPLOAD_DIRECTORY = "var\\uploads"; // Directory to store images
//    private static final int MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB
//    private static final int MAX_MEM_SIZE = 4 * 1024 * 1024;   // 4MB
//
//    private static final String UPLOAD_DIRECTORY = "var\\uploads"; // Directory to store images
//
//    @Override
//    public void handle(HttpServletRequest req, HttpServletResponse resp, Statement s)
//            throws IOException, ServletException {
//
//        // Check if the request is multipart
//        boolean isMultipart = ServletFileUpload.isMultipartContent(req);
//        if (!isMultipart) {
//            resp.setContentType("application/json");
//            resp.setCharacterEncoding("UTF-8");
//            resp.getWriter().write("{\"error\": \"Request is not multipart\"}");
//            return;
//        }
//
//        // Set up the file upload configuration
//        DiskFileItemFactory factory = new DiskFileItemFactory();
//        factory.setSizeThreshold(MAX_MEM_SIZE); // Set memory threshold
//        factory.setRepository(new File(System.getProperty("java.io.tmpdir"))); // Temporary directory
//
//        ServletFileUpload upload = new ServletFileUpload(factory);
//        upload.setSizeMax(MAX_FILE_SIZE); // Set max file size
//
//        // Define the upload path relative to your project folder
//        String projectPath = System.getProperty("user.dir"); // Base directory of the project
//        String uploadPath = projectPath + File.separator + UPLOAD_DIRECTORY;
//        File uploadDir = new File(uploadPath);
//        if (!uploadDir.exists()) {
//            uploadDir.mkdirs();
//        }
//
//        try {
//            // Parse the request to extract file items
//            List<FileItem> fileItems = upload.parseRequest(req);
//
//            for (FileItem item : fileItems) {
//                // Process form fields or file items
//                if (!item.isFormField()) {
//                    String fileName = FilenameUtils.getName(item.getName());
//                    if (fileName != null && !fileName.isEmpty()) {
//                        String filePath = uploadPath + File.separator + fileName;
//                        System.out.println(filePath);
//                        File uploadedFile = new File(filePath);
//                        item.write(uploadedFile); // Save the file
//                    }
//                }
//            }
//
//            // Send a success response
//            resp.setContentType("application/json");
//            resp.setCharacterEncoding("UTF-8");
//            resp.getWriter().write("{\"message\": \"File uploaded successfully\"}");
//
//        } catch (FileUploadException e) {
//            resp.setContentType("application/json");
//            resp.setCharacterEncoding("UTF-8");
//            resp.getWriter().write("{\"error\": \"File upload failed: " + e.getMessage() + "\"}");
//        } catch (Exception e) {
//            resp.setContentType("application/json");
//            resp.setCharacterEncoding("UTF-8");
//            resp.getWriter().write("{\"error\": \"An error occurred: " + e.getMessage() + "\"}");
//        }
//    }
//
//}
////