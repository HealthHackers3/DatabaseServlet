package util;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class errorHandler {
    public errorHandler(HttpServletResponse resp, String errorMessage, Exception e) throws IOException {
        super();
        System.out.println(e);
        String logEntry = "Error: " + errorMessage + " | Exception: " + e;
        centralisedLogger.log(logEntry);
        resp.setContentType("application/json");
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        resp.getWriter().write("{\"error\": \"" + errorMessage + "\"}");
    }
}
