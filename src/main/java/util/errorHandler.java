package util;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class errorHandler {
    public errorHandler(HttpServletResponse resp, String errorMessage, Exception e) throws IOException {
        super();
        System.out.println(e);
        String logEntry = "Error: " + errorMessage + " | Exception: " + e;
        centralisedLogger.log(logEntry);
        errorMessage = errorMessage.replace(":", "->");
        errorMessage = errorMessage.replace("\"", "|");
        errorMessage = errorMessage.replace("\n", " ");
        resp.getWriter().write("{\"error\": \"" + errorMessage + "\"}");
    }
}
