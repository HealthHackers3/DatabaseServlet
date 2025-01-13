package api.img.post;

import api.interfaces.apiCommandHandler;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import util.userAuthenticator;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class postPostImage implements apiCommandHandler {
    private static final Logger log = LoggerFactory.getLogger(postPostImage.class);
    private static final String BUCKET_NAME = "hhdbstorage";
    private static final Region REGION = Region.EU_WEST_2;
    private static final String ACCESS_KEY = "AWS_ACCESS_KEY";
    private static final String SECRET_KEY = "AWS_SECRET_KEY";

    private final String[] commands;

    public postPostImage(String[] commands) {
        this.commands = commands;
    }

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp, Statement s) throws Exception {
        // Check if the request is authenticated
        if (!userAuthenticator.checkSession(req, resp, s.getConnection())) return;

        // Validate that the request is multipart
        if (!ServletFileUpload.isMultipartContent(req)) {
            resp.setContentType("application/json");
            resp.getWriter().write("{\"error\": \"Request is not multipart\"}");
            return;
        }

        // Parse form fields and file data
        Map<Integer, Map<String, String>> imageData = new HashMap<>();
        ArrayList<FileItem> imageSaveData = new ArrayList<>();
        parseMultipartRequest(req, imageData, imageSaveData);

        // Upload images and metadata to S3
        uploadImagesToS3(imageData, imageSaveData, s, resp);
    }

    private void parseMultipartRequest(HttpServletRequest req, Map<Integer, Map<String, String>> imageData, ArrayList<FileItem> imageSaveData) throws FileUploadException {
        DiskFileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);

        for (FileItem item : upload.parseRequest(req)) {
            if (item.isFormField()) {
                // Parse form fields
                int index = Integer.parseInt(getLastCharacter(item.getFieldName()));
                imageData.putIfAbsent(index, new HashMap<>());
                imageData.get(index).put(chopLastTwoCharacters(item.getFieldName()), item.getString());
            } else {
                // Collect file items
                imageSaveData.add(item);
            }
        }
    }

    private void uploadImagesToS3(Map<Integer, Map<String, String>> imageData, ArrayList<FileItem> imageSaveData, Statement s, HttpServletResponse resp) throws SQLException, IOException {
        // Initialize S3 client
        S3Client s3Client = S3Client.builder()
                .region(REGION)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(ACCESS_KEY, SECRET_KEY)))
                .build();

        for (FileItem item : imageSaveData) {
            // Retrieve metadata and insert into the database
            Map<String, String> imageDetails = imageData.get(Integer.parseInt(getLastCharacter(item.getFieldName())));
            int orderIndex = Integer.parseInt(imageDetails.get("order_index"));
            String imageFileName = imageDetails.get("image_file_name");

            int imageId = insertImageMetadata(s, orderIndex, imageFileName);
            String s3Key = "uploads/fullResPostImages/[" + imageId + "][" + orderIndex + "]" + FilenameUtils.getName(item.getName());

            // Upload full-resolution image to S3
            uploadToS3(s3Client, s3Key, item.get());

            // Create and upload thumbnail
            createAndUploadThumbnail(item, imageId, s3Key, s3Client, s);

            resp.getWriter().write("{\"image_id\": \"" + imageId + "\", \"message\": \"File uploaded successfully to S3\"}");
        }
    }

    private int insertImageMetadata(Statement s, int orderIndex, String imageFileName) throws SQLException {
        String insertSQL = "INSERT INTO Lpost_images (post_id, order_index, image_file_name) VALUES (-1, " + orderIndex + ", '" + imageFileName + "') RETURNING image_id";
        try (ResultSet rs = s.executeQuery(insertSQL)) {
            if (rs.next()) return rs.getInt("image_id");
            throw new SQLException("Failed to retrieve image ID");
        }
    }

    private void uploadToS3(S3Client s3Client, String s3Key, byte[] data) {
        PutObjectRequest request = PutObjectRequest.builder().bucket(BUCKET_NAME).key(s3Key).build();
        s3Client.putObject(request, software.amazon.awssdk.core.sync.RequestBody.fromBytes(data));
    }

    private void createAndUploadThumbnail(FileItem item, int fullResId, String s3Key, S3Client s3Client, Statement s) throws IOException, SQLException {
        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(item.get()));

        // Scale image while maintaining aspect ratio
        int maxDim = 250;
        double scale = Math.min((double) maxDim / originalImage.getWidth(), (double) maxDim / originalImage.getHeight());
        int thumbWidth = (int) (originalImage.getWidth() * scale);
        int thumbHeight = (int) (originalImage.getHeight() * scale);

        // Create thumbnail
        BufferedImage thumbnail = new BufferedImage(thumbWidth, thumbHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = thumbnail.createGraphics();
        g.drawImage(originalImage, 0, 0, thumbWidth, thumbHeight, null);
        g.dispose();

        // Upload thumbnail to S3
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(thumbnail, "jpg", os);
        String thumbnailKey = s3Key.replace("fullResPostImages", "thumbnailPostImages");
        uploadToS3(s3Client, thumbnailKey, os.toByteArray());

        // Save thumbnail metadata
        String insertSQL = "INSERT INTO Lpost_images_thumbnails (post_id, ref_image_id, image_path) VALUES (-1, " + fullResId + ", '" + thumbnailKey + "')";
        s.execute(insertSQL);
    }

    private String chopLastTwoCharacters(String input) {
        return input.substring(0, input.length() - 2);
    }

    private String getLastCharacter(String input) {
        return String.valueOf(input.charAt(input.length() - 1));
    }
}
