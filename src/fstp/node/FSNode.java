package fstp.node;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Logger;

import fstp.Constants;
import fstp.handlers.LoggerHandler;
import fstp.node.handlers.TCPHandler;
import fstp.node.handlers.UDPHandler;
import fstp.sockets.TCPConnection;
import fstp.sockets.UDPConnection;

public class FSNode {
    public static Logger logger = Logger.getLogger("FS-Node");

    public static void main(String[] args) {
        LoggerHandler.loadLoggerSettings(logger, true);

        // Check arguments
        if (args.length < 2 || args.length > 3) {
            logger.severe("Invalid number of arguments. Usage: java FSNode <folder_path> <tracker_ip> [tracker_port]");
            return;
        }

        // Get arguments
        String path = args[0].charAt(args[0].length() - 1) == '/' ? args[0] : args[0] + "/";
        String ip = args[1];
        int port = args.length == 3 ? Integer.parseInt(args[2]) : Constants.DEFAULT_PORT;

        // Check if folder exists and load files to NodeStatus
        final File dir = new File(path);
        if (dir.isFile()) {
            logger.severe(path + " is not a folder.");
            return;
        } else if (!dir.exists()) {
            logger.severe("Folder " + path + " does not exist, creating one.");
            if (!dir.mkdir()) {
                logger.severe("Error creating folder " + path);
                return;
            }
        }

        NodeStatus nodeStatus;
        try {
            nodeStatus = new NodeStatus(dir);
        } catch (IOException e) {
            logger.severe("Error loading files from " + path);
            return;
        }

        try {
            Socket socket = new Socket(ip, Constants.DEFAULT_PORT);
            TCPConnection tcpConnection = new TCPConnection(socket);
            try {
                UDPConnection udpConnection = new UDPConnection(port);
                NodeHandler nodeHandler = new NodeHandler(
                    nodeStatus,
                    new TCPHandler(tcpConnection),
                    new UDPHandler(udpConnection)
                );

                new Thread(nodeHandler.initTCPListener()).start();
                new Thread(nodeHandler.initUDPListener()).start();
                new Thread(nodeHandler.initDownloadHandler()).start();
            } catch (SocketException e) {
                logger.severe("Error binding UDP socket to port " + port + ".");
            } catch (Exception e) {
                logger.severe("Error creating UDP socket.");
            }
        } catch (UnknownHostException e) {
            logger.severe(ip + " is not a valid IP address.");
        } catch (IOException e) {
            logger.severe("Error connecting to " + ip + " on port " + port + ". Is the Tracker running?");
        }
    }
}