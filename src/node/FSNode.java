package node;

import java.util.logging.Logger;

import handlers.LoggerHandler;

public class FSNode {
    public static final Logger logger = Logger.getLogger("FSNode");

    public static void main(String[] args) {
        LoggerHandler.loadLoggerSettings(logger);

        logger.info("Hello, World!");
    }
}
