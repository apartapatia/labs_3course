package logging;

import java.util.logging.Logger;

public class LoggerSingleton {
    private static final Logger instance = Logger.getLogger(LoggerSingleton.class.getName());

    private LoggerSingleton() {
    }

    public static Logger getInstance() {
        return instance;
    }
}