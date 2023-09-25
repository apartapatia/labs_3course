package logging;

import java.util.logging.Logger;

public class LoggerSingleton {

    private static volatile Logger instance;
    private static final Object lock = new Object();
    public static Logger getInstance() {
        Logger result = instance;
        if (result != null){
            return  result;
        }

        synchronized (lock) {
            if (instance == null) {
                instance = Logger.getLogger(LoggerSingleton.class.getName());
            }
            return instance;
        }
    }
    private LoggerSingleton() {

    }

}