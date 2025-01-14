package api.img.get;

import api.interfaces.apiCommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import util.centralisedLogger;
import util.userAuthenticator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import com.google.gson.JsonObject;

public class getImgMap implements apiCommandHandler {
    private static final Logger log = LoggerFactory.getLogger(getImgMap.class);
    private final Map<String, apiCommandHandler> userInfoCommands = new HashMap<>();
    private final String[] commands;

    public getImgMap(String[] commands) {
        this.commands = commands;
        userInfoCommands.put("fullres", new getFullres(commands));
        userInfoCommands.put("thumbnail", new getThumbnail(commands));
        userInfoCommands.put("properties", new getProperties(commands));
    }

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp, Statement s) throws IOException, SQLException {
        try {
            userInfoCommands.get(commands[1]).handle(req, resp, s);
        } catch (Exception e) {
            handleError(resp, "{\"error\": \"Invalid image command\"}", e);
        }
    }
}

class getFullres implements apiCommandHandler {
    private static final Logger log = LoggerFactory.getLogger(getFullres.class);
    private static final String BUCKET_NAME = "cellverse";
    private static final Region REGION = Region.EU_NORTH_1; // Replace with your S3 region
    private static final String ACCESS_KEY = "AKIA3C6FMJINPQ4YFWCN";
    private static final String SECRET_KEY = "wfE6VSrfsQvmSMmU/PG3yO1kUPorwQZR8TPHdNoH";
    private final String[] commands;

    public getFullres(String[] commands) {
        this.commands = commands;
    }

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp, Statement s) throws Exception {
        centralisedLogger.log("Command: " + Arrays.toString(commands));
        if (!userAuthenticator.checkSession(req, resp, s.getConnection())) {
            return;
        }

        int image_id = Integer.parseInt(commands[2]);
        try {
            ResultSet rs = s.executeQuery("SELECT image_path FROM lpost_images WHERE image_id = " + image_id);
            if (rs.next()) {
                String s3Key = rs.getString("image_path");

                S3Client s3Client = S3Client.builder()
                        .region(REGION)
                        .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(ACCESS_KEY, SECRET_KEY)))
                        .build();

                GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                        .bucket(BUCKET_NAME)
                        .key(s3Key)
                        .build();

                try (InputStream s3InputStream = s3Client.getObject(getObjectRequest);
                     OutputStream os = resp.getOutputStream()) {
                    resp.setContentType(req.getServletContext().getMimeType(s3Key));
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = s3InputStream.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                }
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Image not found");
            }
        } catch (Exception e) {
            handleError(resp, "Error fetching image", e);
        }
    }
}

class getThumbnail implements apiCommandHandler {
    private static final Logger log = LoggerFactory.getLogger(getThumbnail.class);
    private static final String BUCKET_NAME = "cellverse";
    private static final Region REGION = Region.EU_NORTH_1; // Replace with your S3 region
    private static final String ACCESS_KEY = "AKIA3C6FMJINPQ4YFWCN";
    private static final String SECRET_KEY = "wfE6VSrfsQvmSMmU/PG3yO1kUPorwQZR8TPHdNoH";
    private final String[] commands;

    public getThumbnail(String[] commands) {
        this.commands = commands;
    }

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp, Statement s) throws Exception {
        centralisedLogger.log("Command: " + Arrays.toString(commands));
        if (!userAuthenticator.checkSession(req, resp, s.getConnection())) {
            return;
        }

        int image_id = Integer.parseInt(commands[2]);
        try {
            ResultSet rs = s.executeQuery("SELECT image_path FROM lpost_images_thumbnails WHERE ref_image_id = " + image_id);
            if (rs.next()) {
                String s3Key = rs.getString("image_path");

                S3Client s3Client = S3Client.builder()
                        .region(REGION)
                        .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(ACCESS_KEY, SECRET_KEY)))
                        .build();

                GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                        .bucket(BUCKET_NAME)
                        .key(s3Key)
                        .build();

                try (InputStream s3InputStream = s3Client.getObject(getObjectRequest);
                     OutputStream os = resp.getOutputStream()) {
                    resp.setContentType(req.getServletContext().getMimeType(s3Key));
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = s3InputStream.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                }
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Thumbnail not found");
            }
        } catch (Exception e) {
            handleError(resp, "Error fetching thumbnail", e);
        }
    }
}

class getProperties implements apiCommandHandler {
    private static final Logger log = LoggerFactory.getLogger(getProperties.class);
    private final String[] commands;

    public getProperties(String[] commands) {
        this.commands = commands;
    }

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp, Statement s) throws Exception {
        centralisedLogger.log("Command: " + Arrays.toString(commands));
        if (!userAuthenticator.checkSession(req, resp, s.getConnection())) {
            return;
        }

        int image_id = Integer.parseInt(commands[2]);
        try {
            ResultSet rs = s.executeQuery("SELECT post_id, cell_count, order_index, image_file_name FROM lpost_images WHERE image_id = " + image_id);

            if (rs.next()) {
                int cellCount = rs.getInt("cell_count");
                float cellDimensionsY = rs.getFloat("cell_dimensions_y");
                float cellDimensionsX = rs.getFloat("cell_dimensions_x");
                float cellDensity = rs.getFloat("cell_density");

                JsonObject json = new JsonObject();
                json.addProperty("cell_count", cellCount);
                json.addProperty("cell_density", cellDensity);
                json.addProperty("cell_dimensions_y", cellDimensionsY);
                json.addProperty("cell_dimensions_x", cellDimensionsX);

                resp.setContentType("application/json");
                resp.setCharacterEncoding("UTF-8");
                resp.getWriter().write(json.toString());
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "No data found for the given image ID");
            }
        } catch (SQLException e) {
            handleError(resp, "{\"error\": \"Database error while fetching image properties\"}", e);
        } catch (Exception e) {
            handleError(resp, "{\"error\": \"Unexpected error\"}", e);
        }
    }
}
