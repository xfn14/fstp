package tracker;

import java.util.logging.Logger;

import handlers.LoggerHandler;

public class FSTracker {
    public static final Logger logger = Logger.getLogger("FSTracker");

    public static void main(String[] args) {
        LoggerHandler.loadLoggerSettings(logger);

        logger.info("Hello, World!");
    }
}
