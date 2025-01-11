//package api.img;
//
//import api.interfaces.apiCommandHandler;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.File;
//import java.io.IOException;
//import java.io.OutputStream;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.sql.Statement;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.Map;
//import java.io.FileInputStream;
//import com.google.gson.JsonObject; // For JSON conversion (use Gson library)
//import util.centralisedLogger;
//import util.userAuthenticator;
//
//
//public class getImgMap implements apiCommandHandler {
//    private static final Logger log = LoggerFactory.getLogger(getImgMap.class);
//    private final Map<String, apiCommandHandler> userInfoCommands = new HashMap<>();
//    private final String[] commands;
//
//    public getImgMap(String[] commands){
//        this.commands = commands;
//        userInfoCommands.put("fullres", new getFullres(commands));
//        userInfoCommands.put("thumbnail", new getThumbnail(commands));
//        userInfoCommands.put("properties", new getProperties(commands));
//    }
//    @Override
//    public void handle(HttpServletRequest req, HttpServletResponse resp, Statement s) throws IOException, SQLException {
//        try{
//            userInfoCommands.get(commands[1]).handle(req, resp, s);
//        }catch (Exception e){
//            handleError(resp, "{\"error\": \"Invalid image command\"}", e);
//            return;
//        }
//    }
//}
//
//class getFullres implements apiCommandHandler {
//    int image_id;
//    String[] commands;
//    private static final Logger log = LoggerFactory.getLogger(getImgMap.class);
//
//    public getFullres(String[] commands) {
//        this.commands = commands;
//
//    }
//
//    @Override
//    public void handle(HttpServletRequest req, HttpServletResponse resp, Statement s) throws Exception {
//
//        centralisedLogger.log("Command: " + Arrays.toString(commands));
//        if(!userAuthenticator.checkSession(req, resp, s.getConnection())){return;}
//        this.image_id = Integer.parseInt(commands[2]);
//        try {
//            ResultSet rs = s.executeQuery("SELECT image_path, image_file_name FROM lpost_images WHERE image_id = " + image_id);
//            rs.next();
//            String img_path = rs.getString("image_path");
//            String img_name = rs.getString("image_file_name");
//            File imageFile = new File(img_path);
//
//
//            if (imageFile.exists() && !imageFile.isDirectory()) {
//                resp.setContentType(req.getServletContext().getMimeType(imageFile.getName()));
//                try (FileInputStream fis = new FileInputStream(imageFile);
//                     OutputStream os = resp.getOutputStream()) {
//                    byte[] buffer = new byte[1024];
//                    int bytesRead;
//                    while ((bytesRead = fis.read(buffer)) != -1) {
//                        os.write(buffer, 0, bytesRead);
//                    }
//                }
//            } else {
//                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Image not found");
//            }
//        }catch (Exception e){
//            handleError(resp, "Error fetching image", e);
//        }
//    }
//}
//
//class getThumbnail implements apiCommandHandler {
//    int image_id;
//    String[] commands;
//    private static final Logger log = LoggerFactory.getLogger(getImgMap.class);
//
//    public getThumbnail(String[] commands) {
//        this.commands = commands;
//    }
//
//    @Override
//    public void handle(HttpServletRequest req, HttpServletResponse resp, Statement s) throws IOException, SQLException {
//        try {
//            centralisedLogger.log("Command: " + Arrays.toString(commands));
//            if(!userAuthenticator.checkSession(req, resp, s.getConnection())){return;}
//            this.image_id = Integer.parseInt(commands[2]);
//            ResultSet rs = s.executeQuery("SELECT image_path FROM lpost_images_thumbnails WHERE ref_image_id = " + image_id);
//            rs.next();
//            String img_path = rs.getString("image_path");
//            File imageFile = new File(img_path);
//            if (imageFile.exists() && !imageFile.isDirectory()) {
//                resp.setContentType(req.getServletContext().getMimeType(imageFile.getName()));
//                try (FileInputStream fis = new FileInputStream(imageFile);
//                     OutputStream os = resp.getOutputStream()) {
//                    byte[] buffer = new byte[1024];
//                    int bytesRead;
//                    while ((bytesRead = fis.read(buffer)) != -1) {
//                        os.write(buffer, 0, bytesRead);
//                    }
//                }
//            } else {
//                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Image not found");
//            }
//        }catch (Exception e){
//            handleError(resp, "{\"error\": \"Invalid image command\"}", e);
//        }
//    }
//}
//
//class getProperties implements apiCommandHandler {
//    int image_id;
//    String[] commands;
//    private static final Logger log = LoggerFactory.getLogger(getProperties.class);
//
//    public getProperties(String[] commands) {
//        this.commands = commands;
//        // Assuming ID is at index 2 in the commands array
//    }
//
//    @Override
//    public void handle(HttpServletRequest req, HttpServletResponse resp, Statement s) throws IOException, SQLException {
//        try {
//            centralisedLogger.log("Command: " + Arrays.toString(commands));
//            if(!userAuthenticator.checkSession(req, resp, s.getConnection())){return;}
//            this.image_id = Integer.parseInt(commands[2]);
//            // Execute the query to fetch image data
//            ResultSet rs = s.executeQuery("SELECT cell_count, cell_density, cell_dimensions_y, cell_dimensions_x FROM lpost_images WHERE image_id = " + image_id);
//
//            // Check if data exists
//            if (rs.next()) {
//                // Fetch data from the result set
//                int cellCount = rs.getInt("cell_count");
//                float cellDimensionsY = Float.parseFloat(rs.getString("cell_dimensions_y"));
//                float cellDimensionsX = Float.parseFloat(rs.getString("cell_dimensions_x"));
//                float cellDensity = rs.getInt("cell_density");
//
//                // Build a JSON object with the fetched data
//                JsonObject json = new JsonObject();
//                json.addProperty("cell_count", cellCount);
//                json.addProperty("cell_density", cellDensity);
//                json.addProperty("cell_dimensions_y", cellDimensionsY);
//                json.addProperty("cell_dimensions_x", cellDimensionsX);
//
//                // Set response content type and write the JSON response
//                resp.setContentType("application/json");
//                resp.setCharacterEncoding("UTF-8");
//                resp.getWriter().write(json.toString());
//            } else {
//                // No data found for the given ID
//                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "No data found for the given image ID");
//            }
//        } catch (SQLException e) {
//            handleError(resp, "{\"error\": \"Database error while fetching image properties\"}", e);
//        } catch (Exception e) {
//            handleError(resp, "{\"error\": \"Unexpected error\"}", e);
//        }
//    }
//}
//
