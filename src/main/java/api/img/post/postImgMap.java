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
import util.centralisedLogger;
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
import java.util.*;
import java.util.List;

public class postImgMap implements apiCommandHandler {
    private final Map<String, apiCommandHandler> userInfoCommands = new HashMap<>();
    private final String[] commands;

    public postImgMap(String[] commands) {
        this.commands = commands;
        userInfoCommands.put("upload", new postPostImage(commands));
        userInfoCommands.put("settopost", new postSetToPost(commands));
    }

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp, Statement s) throws IOException, SQLException {
        try {
            userInfoCommands.get(commands[1]).handle(req, resp, s);
        } catch (Exception e) {
            handleError(resp, "{\"error\": \"Invalid post command\"}", e);
        }
    }
}

class postPostImage implements apiCommandHandler {

    private static final Logger log = LoggerFactory.getLogger(postPostImage.class);
    private static final String BUCKET_NAME = "hhdbstorage";
    private static final Region REGION = Region.EU_WEST_2; // Replace with your S3 region
    private static final String ACCESS_KEY = "AKIA54WIGLXL5TLKTH4D";
    private static final String SECRET_KEY = "79vfbJSiPydqbp21fK6a8TOuDOXcgbad6EA5Y3tJ";

    String[] commands;

    public postPostImage(String[] commands) {
        this.commands = commands;
    }

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp, Statement s)
            throws Exception {
        centralisedLogger.log("Command: " + Arrays.toString(commands));
        if (!userAuthenticator.checkSession(req, resp, s.getConnection())) {
            return;
        }

        boolean isMultipart = ServletFileUpload.isMultipartContent(req);
        if (!isMultipart) {
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            resp.getWriter().write("{\"error\": \"Request is not multipart\"}");
            return;
        }

        DiskFileItemFactory factory = new DiskFileItemFactory();
        factory.setSizeThreshold(4 * 1024 * 1024); // Set memory threshold
        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setSizeMax(50 * 1024 * 1024); // Set max file size

        Map<Integer, Map<String, String>> imageData = new HashMap<>();
        ArrayList<FileItem> imageSaveData = new ArrayList<>();

        try {
            List<FileItem> fileItems = upload.parseRequest(req);

            for (FileItem item : fileItems) {
                if (item.isFormField()) {
                    String fieldName = item.getFieldName();
                    String fieldValue = item.getString();
                    int index = Integer.parseInt(getLastCharacter(fieldName));
                    imageData.putIfAbsent(index, new HashMap<>());
                    imageData.get(index).put(chopLastTwoCharacters(fieldName), fieldValue);
                } else {
                    imageSaveData.add(item);
                }
            }

            uploadImagesToS3(imageData, imageSaveData, Integer.parseInt(commands[2]), s, resp);

        } catch (FileUploadException e) {
            handleError(resp, "{\"error\": \"File upload failed\"}", e);
        } catch (Exception e) {
            handleError(resp, "{\"error\": \"An error occurred\"}", e);
        }
    }

    private void uploadImagesToS3(Map<Integer, Map<String, String>> imageData, ArrayList<FileItem> imageSaveData, int postId, Statement s, HttpServletResponse resp) throws SQLException, IOException {
        S3Client s3Client = S3Client.builder()
                .region(REGION)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(ACCESS_KEY, SECRET_KEY)))
                .build();

        for (FileItem item : imageSaveData) {
            Map<String, String> imageDetails = imageData.get(Integer.parseInt(getLastCharacter(item.getFieldName())));
            int orderIndex = Integer.parseInt(imageDetails.get("order_index"));
            String imageFileName = imageDetails.get("image_file_name");
            int imageId = 0;
            try {
                String insertSQL = "INSERT INTO Lpost_images (post_id, order_index, image_file_name) VALUES (" +
                        "-1" + ", " + orderIndex + ", '" + imageFileName + "') RETURNING image_id";

                try (ResultSet rs = s.executeQuery(insertSQL)) {
                    if (rs.next()) {
                        imageId = rs.getInt("image_id");
                    }
                }
                String s3Key = "uploads/fullResPostImages/" + "["+imageId + "][" + postId + "][" + orderIndex + "]" + FilenameUtils.getName(item.getName());
                s.executeUpdate("UPDATE Lpost_images SET image_path = '" + s3Key + "' WHERE image_id = '" + imageId + "'");
                // Upload the full-resolution image directly to S3
                PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                        .bucket(BUCKET_NAME)
                        .key(s3Key)
                        .build();

                s3Client.putObject(putObjectRequest, software.amazon.awssdk.core.sync.RequestBody.fromBytes(item.get()));
                s3Key = "uploads/fullResPostImages/" + "[thumbnail]"+"["+imageId + "][" + postId + "][" + orderIndex + "]" + FilenameUtils.getName(item.getName());
                createAndUploadThumbnail(item, imageId, postId, s3Key, s3Client, s);

                resp.setContentType("application/json");
                resp.setCharacterEncoding("UTF-8");
                resp.getWriter().write("{\"message\": \"File uploaded successfully to S3\"}");

            } catch (Exception e) {
                log.error("Error uploading to S3: ", e);
                throw new RuntimeException(e);
            }
        }
    }

    private void createAndUploadThumbnail(FileItem item, int fullResId, int postId, String s3Key, S3Client s3Client, Statement s) throws SQLException, IOException {
        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(item.get()));
        int maxThumbnailHeight = 250;
        int maxThumbnailWidth = 250;

        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        int thumbnailWidth = originalWidth;
        int thumbnailHeight = originalHeight;

        // Calculate new dimensions while maintaining the aspect ratio
        if (originalWidth > maxThumbnailWidth || originalHeight > maxThumbnailHeight) {
            double widthScale = (double) maxThumbnailWidth / originalWidth;
            double heightScale = (double) maxThumbnailHeight / originalHeight;
            double scale = Math.min(widthScale, heightScale);

            thumbnailWidth = (int) (originalWidth * scale);
            thumbnailHeight = (int) (originalHeight * scale);
        }

        BufferedImage thumbnailImage = new BufferedImage(thumbnailWidth, thumbnailHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = thumbnailImage.createGraphics();
        g.drawImage(originalImage, 0, 0, thumbnailWidth, thumbnailHeight, null);
        g.dispose();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(thumbnailImage, "jpg", outputStream);
        byte[] thumbnailBytes = outputStream.toByteArray();

        String thumbnailKey = s3Key.replace("fullResPostImages", "thumbnailPostImages");

        PutObjectRequest thumbnailRequest = PutObjectRequest.builder()
                .bucket(BUCKET_NAME)
                .key(thumbnailKey)
                .build();

        s3Client.putObject(thumbnailRequest, software.amazon.awssdk.core.sync.RequestBody.fromBytes(thumbnailBytes));

        String insertSQL = "INSERT INTO Lpost_images_thumbnails (post_id, ref_image_id, image_path) VALUES (" +
                postId + ", " + fullResId + ", '" + thumbnailKey + "')";
        s.execute(insertSQL);
    }

    private String chopLastTwoCharacters(String input) {
        return input.substring(0, input.length() - 2);
    }

    private String getLastCharacter(String input) {
        return String.valueOf(input.charAt(input.length() - 1));
    }
}
