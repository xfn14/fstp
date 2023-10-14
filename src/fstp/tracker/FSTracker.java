package fstp.tracker;

import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.logging.Logger;

import fstp.Constants;
import fstp.handlers.LoggerHandler;
import fstp.sockets.TCPConnection;

public class FSTracker {
    public static Logger logger = Logger.getLogger("FS-Tracker");
    private boolean running = true;

    public static void main(String[] args) {
        LoggerHandler.loadLoggerSettings(logger, false);

        FSTracker fsTracker = new FSTracker();
        try {
            fsTracker.init();
        } catch (IOException e) {
            logger.severe("Failed to initialize tracker.");
            e.printStackTrace();
        }
    }

    public void init() throws IOException {
        ServerSocket serverSocket = new ServerSocket(Constants.DEFAULT_PORT);
        logger.info("Listening on port " + Constants.DEFAULT_PORT);

        Skeleton sk = new Skeleton();

        while (running) {
            TCPConnection connection = new TCPConnection(serverSocket.accept());
            logger.info("New connection from " + connection.getInetAddress().getHostAddress());

            Runnable r = () -> {
                try (connection) {
                    for (; ; ) sk.handle(connection);
                } catch (EOFException ignored){
                } catch (Exception e){
                    logger.warning("Error handling connection. " + e.getMessage());
                    e.printStackTrace();
                }
            };
            
            new Thread(r).start();
        }

        serverSocket.close();
        logger.info("Server offline, socket closed. Goodbye!");
    }

    public void stop() {
        this.running = false;
    }
}
