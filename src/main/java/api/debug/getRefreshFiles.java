package api.debug;

import api.interfaces.apiCommandHandler;
import util.centralisedLogger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.sql.Statement;

public class getRefreshFiles implements apiCommandHandler {

    public getRefreshFiles() {

    }
    private static final String UPLOAD_DIRECTORY = "var/uploads";
    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp, Statement s) throws Exception {
        String projectPath = System.getProperty("user.dir").replace("\\", "/");

        String uploadPath = projectPath + File.separator + UPLOAD_DIRECTORY;
        centralisedLogger.log("projectPath: " + projectPath);
        centralisedLogger.log("Upload path: " + uploadPath);
        centralisedLogger.log("File seperator: " + File.separator);
        centralisedLogger.log("uploadDirectory: " + UPLOAD_DIRECTORY);
        File uploadDir = new File(uploadPath);
//        try {
//            ensureDirectoryExists(uploadPath);
//            ensureDirectoryExists(uploadPath + "/fullResPostImages");
//            ensureDirectoryExists(uploadPath + "/thumbnailPostImages");
//        } catch (IOException e) {
//            centralisedLogger.log("Error ensuring directory exists: " + e.getMessage());
//            throw new RuntimeException(e);
//        }
        if (uploadDir.canWrite()) {
            System.out.println("Write permission is granted for: " + uploadPath);
        } else {
            System.out.println("Write permission is NOT granted for: " + uploadPath);
        }
        File[] files = uploadDir.listFiles();

        if (!uploadDir.exists()) {
            centralisedLogger.log("Directory does not exist: " + uploadPath);
            return;
        }

        if (!uploadDir.isDirectory()) {
            centralisedLogger.log("Path is not a directory: " + uploadPath);
            return;
        }

        if (files == null || files.length == 0) {
            centralisedLogger.log("No files or directories found in: " + uploadPath);
        }
        else {

            centralisedLogger.log("Contents of directory: " + uploadPath);

            // Iterate through and log directories
            for (File file : files) {
                if (file.isDirectory()) {
                    centralisedLogger.log("Directory: " + file.getName());
                } else if (file.isFile()) {
                    centralisedLogger.log("File: " + file.getName());
                }
            }
        }
        resp.setContentType("application/json");
        resp.getWriter().write("{\"message\": \"Directories verified and listed successfully\"}");

    }
    public static void ensureDirectoryExists(String path) throws IOException {
        File directory = new File(path);
        if (!directory.exists()) {
            if (directory.mkdirs()) {
                centralisedLogger.log("Directory created: " + path);
            } else {
                throw new IOException("Failed to create directory: " + path);
            }
        } else {
            centralisedLogger.log("Directory already exists: " + path);
        }
    }
}
