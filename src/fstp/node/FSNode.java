package fstp.node;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;
import java.net.DatagramSocket;
import java.net.UnknownHostException;
import java.util.logging.Logger;

import fstp.Constants;
import fstp.handlers.LoggerHandler;
import fstp.sockets.TCPConnection;

public class FSNode {
    public static Logger logger = Logger.getLogger("FS-Tracker");
    private static final BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
    
    public static void main(String[] args) {
        LoggerHandler.loadLoggerSettings(logger, true);

        // Check arguments
        if (args.length < 2 || args.length > 3) {
            logger.severe("Invalid number of arguments. Usage: java FSNode <folder_path> <tracker_ip> [tracker_port]");
            return;
        }

        // Get arguments
        String path = args[0];
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
        NodeStatus nodeStatus = new NodeStatus(dir);

        // Connect to tracker
        Runnable tcpRunnable = () -> {
            try (Socket socket = new Socket(ip, port)) {
                TCPConnection tcpConnection = new TCPConnection(socket);
                NodeHandler nodeHandler = new NodeHandler(tcpConnection);
                logger.info("Conexão FS Track Protocol com servidor " + ip + " porta " + port);
                
                String response = nodeHandler.ping(nodeStatus.getFiles());
                logger.info("Resposta do servidor: " + response);

                tcpConnection.close();
            } catch (UnknownHostException e) {
                logger.severe(ip + " is not a valid IP address.");
                e.printStackTrace();
            } catch (IOException e) {
                logger.severe("Error connecting to " + ip + " on port " + port);
                e.printStackTrace();
            }
        };
        tcpRunnable.run();

        // Start UDP server
        Runnable udpRunnable = () -> {
            try {
                DatagramSocket socket = new DatagramSocket();
                logger.info("FS Transfer Protocol à escuta na porta UDP " + port);

                
            } catch (SocketException e) {
                e.printStackTrace();
            }
        };
        udpRunnable.run();
    }
}
