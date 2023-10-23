package fstp.node;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.DatagramSocket;
import java.net.UnknownHostException;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import fstp.Constants;
import fstp.handlers.LoggerHandler;
import fstp.sockets.TCPConnection;

public class FSNode {
    public static Logger logger = Logger.getLogger("FS-Tracker");

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

        Runnable tcpRunnable = () -> {
            try (Socket socket = new Socket(ip, port)) {
                TCPConnection tcpConnection = new TCPConnection(socket);
                NodeHandler nodeHandler = new NodeHandler(path, tcpConnection);

                String response = nodeHandler.ping(nodeStatus.getFileInfos().values().stream().collect(Collectors.toList()));
                if (response.equals("Error")) {
                    logger.severe("Error connecting to Tracker on " + ip + ":" + port);
                    return;
                }

                nodeStatus.clearPeers();
                if (response.length() != 0) {
                    String[] peers = response.split(",");
                    for (String peer : peers)
                        nodeStatus.addPeer(peer);
                }

                logger.info("FS Track Protocol connected to Tracker on " + ip + ":" + port);
                
                Interperter interperter = new Interperter(nodeHandler, nodeStatus);
                interperter.run();

                tcpConnection.close();
            } catch (UnknownHostException e) {
                logger.severe(ip + " is not a valid IP address.");
            } catch (IOException e) {
                logger.severe("Error connecting to " + ip + " on port " + port + ". Is the Tracker running?");
            }
        };

        Runnable udpRunnable = () -> {
            try {
                DatagramSocket socket = new DatagramSocket();
                logger.info("FS Transfer Protocol listening using UDP on " + port);
                
            } catch (SocketException e) {
                e.printStackTrace();
            }
        };

        udpRunnable.run();
        tcpRunnable.run();
    }
}
