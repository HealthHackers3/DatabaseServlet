package api.debug;

import api.interfaces.apiCommandHandler;
import util.centralisedLogger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
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
    }
}
