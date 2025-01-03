package util;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class centralisedLogger {
    private static final List<String> logs = Collections.synchronizedList(new LinkedList<>());

    // Add a log entry
    public static void log(String message) {
        synchronized (logs) {
            logs.add(message);
            // Keep the log size manageable by removing the oldest entries if needed
            if (logs.size() > 1000) { // Retain only the last 1000 logs
                logs.remove(0);
            }
        }
    }

    // Retrieve all logs
    public static List<String> getLogs() {
        synchronized (logs) {
            return new LinkedList<>(logs); // Return a copy to avoid concurrency issues
        }
    }

    // Clear logs
    public static void clearLogs() {
        synchronized (logs) {
            logs.clear();
        }
    }
}
