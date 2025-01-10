package api.img;

import api.interfaces.apiCommandHandler;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.centralisedLogger;
import util.errorHandler;
import util.userAuthenticator;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.List;


public class postImgMap implements apiCommandHandler {
    private final Map<String, apiCommandHandler> userInfoCommands = new HashMap<>();
    private final String[] commands;

    public postImgMap(String[] commands){
        this.commands = commands;
        userInfoCommands.put("upload", new postPostImage(commands));
    }

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp, Statement s) throws IOException, SQLException {
        try{
            userInfoCommands.get(commands[1]).handle(req, resp, s);
        }catch (Exception e){
            handleError(resp,"{\"error\": \"Invalid post command\"}", e);
            return;
        }
    }

}


class postPostImage implements apiCommandHandler {

    //private static final String UPLOAD_DIRECTORY = "var\\uploads"; // Directory to store images
    private static final int MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB
    private static final int MAX_MEM_SIZE = 4 * 1024 * 1024;   // 4MB

    private static final String UPLOAD_DIRECTORY = "var\\uploads"; // Directory to store images
    private static final Logger log = LoggerFactory.getLogger(postPostImage.class);
    String[] commands;
    public postPostImage(String[] commands) {
    this.commands = commands;
    }

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp, Statement s)
            throws Exception {
        centralisedLogger.log("Command: " + Arrays.toString(commands));
        if(!userAuthenticator.checkSession(req, resp, s.getConnection())){return;}
        int order_index;
        String image_file_name;
        int cell_count;
        float cell_dimensions_y;
        float cell_dimensions_x;
        float cell_density;
        // Check if the request is multipart
        boolean isMultipart = ServletFileUpload.isMultipartContent(req);
        if (!isMultipart) {
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            resp.getWriter().write("{\"error\": \"Request is not multipart\"}");
            return;
        }

        // Set up the file upload configuration
        DiskFileItemFactory factory = new DiskFileItemFactory();
        factory.setSizeThreshold(MAX_MEM_SIZE); // Set memory threshold
        factory.setRepository(new File(System.getProperty("java.io.tmpdir"))); // Temporary directory

        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setSizeMax(MAX_FILE_SIZE); // Set max file size

        // Define the upload path relative to your project folder
        String projectPath = System.getProperty("user.dir"); // Base directory of the project
        String uploadPath = projectPath + File.separator + UPLOAD_DIRECTORY;
        centralisedLogger.log("Upload path: " + uploadPath);
        centralisedLogger.log("File seperator: " + File.separator);
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            centralisedLogger.log("Making a new file");
            uploadDir.mkdirs();
        }
        Map<Integer, Map<String, String>> imageData = new HashMap<>();
        ArrayList<FileItem> imageSaveData = new ArrayList<>();
        try {
            String filePath = null;
            // Parse the request to extract file items
            List<FileItem> fileItems = upload.parseRequest(req);

            for (FileItem item : fileItems) {
                // Process form fields or file items

                if (item.isFormField()) {
                    // Get the field name and value
                    String fieldName = item.getFieldName();
                    String fieldValue = item.getString();

                    // Extract the index from the field name (e.g., "order_index_0" -> index = 0)
                        int index = Integer.parseInt(getLastCharacter(fieldName));

                        // Create a map for each image index if not already created
                        imageData.putIfAbsent(index, new HashMap<>());

                        // Store the field value in the corresponding map
                        imageData.get(index).put(chopLastTwoCharacters(fieldName), fieldValue);
                }
                else{
                    imageSaveData.add(item);
                }
            }

            insertImages(imageData, imageSaveData, Integer.parseInt(commands[2]), s, uploadPath, resp);

        } catch (FileUploadException e) {
            handleError(resp, "{\"error\": \"File upload failed\"}", e);
        } catch (Exception e) {
            handleError(resp, "{\"error\": \"An error occurred\"}", e);
        }
    }
    public static String chopLastTwoCharacters(String input) {
        if (input == null || input.length() < 2) {
            throw new IllegalArgumentException("Input string must have at least two characters");
        }
        return input.substring(0, input.length() - 2); // Return the string without the last two characters
    }
    public static String getLastCharacter(String input) {
        if (input == null || input.isEmpty()) {
            throw new IllegalArgumentException("Input string cannot be null or empty");
        }

        return String.valueOf(input.charAt(input.length() - 1)); // Get the last character
    }
    public static void insertImages(Map<Integer, Map<String, String>> imageData, ArrayList<FileItem> imageSaveData, int postId, Statement s, String uploadPath, HttpServletResponse resp) throws SQLException, IOException {
        Statement stmt = null;

        try {
            for (FileItem item : imageSaveData) {
                Map<String, String> imageDetails = imageData.get(Integer.parseInt(getLastCharacter(item.getFieldName())));

                // Extract fields from the map
                int orderIndex = Integer.parseInt(imageDetails.get("order_index"));
                String imageFileName = imageDetails.get("image_file_name");
                int cellCount = Integer.parseInt(imageDetails.get("cell_count"));
                int cellDimensionsX = Integer.parseInt(imageDetails.get("cell_dimensions_x"));
                int cellDimensionsY = Integer.parseInt(imageDetails.get("cell_dimensions_y"));
                int cellDensity = Integer.parseInt(imageDetails.get("cell_density"));

                // Construct the SQL query dynamically
                String insertSQL = "INSERT INTO Lpost_images (post_id, order_index, image_file_name, cell_count, cell_dimensions_x, cell_dimensions_y, cell_density) "
                        + "VALUES (" + postId + ", " + orderIndex + ", '" + imageFileName + "', " + cellCount + ", "
                        + cellDimensionsX + ", " + cellDimensionsY + ", " + cellDensity + ") RETURNING image_id";

                // Execute the insert and retrieve the image_id
                int imageId = 0;
                try (ResultSet rs = s.executeQuery(insertSQL)) {
                    if (rs.next()) {
                        imageId = rs.getInt("image_id");
                    }
                    String fileName = "[" + imageId + "][" + postId + "][" + orderIndex +"]"+FilenameUtils.getName(item.getName());
                    String filePath = uploadPath  + File.separator +  "fullResPostImages" + File.separator + fileName;
                    centralisedLogger.log("File path: " + filePath);
                    s.executeUpdate("UPDATE Lpost_images SET image_path = '" + filePath + "' WHERE image_id = '" + imageId + "'");
                    File uploadedFile = new File(filePath);
                    item.write(uploadedFile); // Save the file
                    insertThumbnails(uploadPath, imageId, postId, fileName, s);
                    resp.setContentType("application/json");
                    resp.setCharacterEncoding("UTF-8");

                    resp.getWriter().write("{\"message\": \"File uploaded successfully\", \"file_location\": \"" + filePath + "\"}");
                }catch (Exception e) {
                    new errorHandler(resp, "{\"error\": \"An error occurred\"}", e);
                }


            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (stmt != null) {
                stmt.close();
            }
        }
    }
    public static void insertThumbnails(String uploadPath, int fullResId, int postId, String fileName, Statement s) throws SQLException, IOException {
        int thumbnailHeight = 500;
        int thumbnailWidth = 500;
        try {
            File inputFile = new File(uploadPath + File.separator + "fullResPostImages" + File.separator + fileName);
            System.out.println(inputFile.getAbsoluteFile());
            BufferedImage originalImage = ImageIO.read(inputFile);

            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();

            // Calculate new dimensions while maintaining aspect ratio
            int newWidth = thumbnailWidth;
            int newHeight = thumbnailHeight;

            if (originalWidth > originalHeight) {
                // Landscape image
                newHeight = (int) (thumbnailWidth * ((double) originalHeight / originalWidth));
            } else {
                // Portrait or square image
                newWidth = (int) (thumbnailHeight * ((double) originalWidth / originalHeight));
            }

            BufferedImage thumbnailImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);

            Graphics2D g = thumbnailImage.createGraphics();
            g.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
            g.dispose();
            String thumbnailPath =uploadPath + "\\thumbnailPostImages" + File.separator + "thumbnail_" + fileName;;
            File outputFile = new File(thumbnailPath);


            String insertSQL = "INSERT INTO Lpost_images_thumbnails (post_id, ref_image_id, image_path) "
                    + "VALUES (" +postId + ", " + fullResId + ", '" + thumbnailPath + "')";
            try {
                s.execute(insertSQL);
                ImageIO.write(thumbnailImage, "jpg", outputFile);
            }catch (Exception e) {
                throw new RuntimeException(e);
            }


        } catch (Exception e) {
            log.error("e: ", e);
            throw new RuntimeException(e);
        }
    }

}

