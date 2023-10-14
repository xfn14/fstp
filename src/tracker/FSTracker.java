package tracker;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.logging.Logger;

import handlers.LoggerHandler;
import sockets.TCPConnection;

public class FSTracker {
    public static Logger logger = Logger.getLogger("FS-Tracker");
    public static final int DEFAULT_PORT = 9090;
    private boolean running = true;

    public static void main(String[] args) {
        LoggerHandler.loadLoggerSettings(logger);

        FSTracker fsTracker = new FSTracker();
        try {
            fsTracker.init();
        } catch (IOException e) {
            logger.severe("Failed to initialize tracker.");
            e.printStackTrace();
        }
    }

    public void init() throws IOException {
        ServerSocket serverSocket = new ServerSocket(DEFAULT_PORT);
        logger.info("Listening on port " + DEFAULT_PORT);

        Skeleton sk = new Skeleton();

        while (running) {
            TCPConnection connection = new TCPConnection(serverSocket.accept());
            logger.info("New connection from " + connection.getInetAddress().getHostAddress());

            sk.handle(connection);
        }

        serverSocket.close();
        logger.info("Server offline, socket closed. Goodbye!");
    }

    public void stop() {
        this.running = false;
    }
}
