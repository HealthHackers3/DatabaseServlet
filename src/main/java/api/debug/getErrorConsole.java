package api.debug;

import api.interfaces.apiCommandHandler;
import util.centralisedLogger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;

public class getErrorConsole implements apiCommandHandler {
    public getErrorConsole() {
    }

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp, Statement s) throws IOException, SQLException {
        resp.setContentType("text/html"); // Set the response type to HTML

        // Build the HTML content
        StringBuilder htmlResponse = new StringBuilder();
        htmlResponse.append("<!DOCTYPE html>")
                .append("<html>")
                .append("<head>")
                .append("<title>Serlvet Console</title>")
                .append("<style>")
                .append("body { font-family: monospace; background-color: #000; color: #fff; padding: 10px; }")
                .append(".log { background-color: #111; border: 1px solid #333; padding: 10px; margin-bottom: 5px; border-radius: 5px; }")
                .append(".command { color: #fff; }")
                .append(".error { color: #f00; }")
                .append("h1 { color: #ff0; }")
                .append("</style>")
                .append("</head>")
                .append("<body>")
                .append("<h1>Serlvet Console</h1>");

        // Add logs
        for (String log : centralisedLogger.getLogs()) {
            if (log.startsWith("Error:")) {
                htmlResponse.append("<div class=\"log error\">")
                        .append(log.replace("<", "&lt;").replace(">", "&gt;")) // Escape HTML special characters
                        .append("</div>");
            } else if (log.startsWith("Command:")) {
                htmlResponse.append("<div class=\"log command\">")
                        .append(log.replace("<", "&lt;").replace(">", "&gt;")) // Escape HTML special characters
                        .append("</div>");
            } else {
                htmlResponse.append("<div class=\"log\">")
                        .append(log.replace("<", "&lt;").replace(">", "&gt;")) // Escape HTML special characters
                        .append("</div>");
            }
        }

        htmlResponse.append("</body>")
                .append("</html>");

        // Write HTML response
        resp.getWriter().write(htmlResponse.toString());
    }
}
