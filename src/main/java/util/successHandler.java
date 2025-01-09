package util;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class successHandler {
    public successHandler(HttpServletResponse resp, String successMessage) throws IOException {
        super();
        System.out.println(successMessage);
        centralisedLogger.log("Success: " + successMessage);
    }
}
