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
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class postImgMap implements apiCommandHandler {
    private final Map<String, apiCommandHandler> userInfoCommands = new HashMap<>();
    private final String[] commands;

    public postImgMap(String[] commands) {
        this.commands = commands;
        // Initialize subcommands for handling image uploads
        userInfoCommands.put("upload", new postPostImage(commands));
        userInfoCommands.put("settopost", new postSetToPost(commands));
    }

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp, Statement s) throws IOException, SQLException {
        // Dispatch the subcommand based on the second command
        try {
            userInfoCommands.get(commands[1]).handle(req, resp, s);
        } catch (Exception e) {
            handleError(resp, "{\"error\": \"Invalid post command\"}", e);
        }
    }
}

// Handles the "upload" command for uploading images to S3
