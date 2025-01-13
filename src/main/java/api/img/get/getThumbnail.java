package api.img.get;

import api.interfaces.apiCommandHandler;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import util.centralisedLogger;
import util.userAuthenticator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;

class getThumbnail implements apiCommandHandler {
    //Keys to access AWS s3 volume
    private static final String BUCKET_NAME = "hhdbstorage";
    private static final Region REGION = Region.EU_WEST_2;
    private static final String ACCESS_KEY = "AKIA54WIGLXL5TLKTH4D";
    private static final String SECRET_KEY = "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";
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
            // Query thumbnail path from the database
            ResultSet rs = s.executeQuery("SELECT image_path FROM lpost_images_thumbnails WHERE ref_image_id = " + image_id);
            if (rs.next()) {
                utilS3Grabber.fetchFileFromS3(rs, resp, req);
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Thumbnail not found");
            }
        } catch (Exception e) {
            handleError(resp, "Error fetching thumbnail", e);
        }
    }
}