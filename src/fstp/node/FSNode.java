package fstp.node;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;
import java.net.DatagramSocket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import fstp.Constants;
import fstp.handlers.LoggerHandler;
import fstp.models.FileInfo;
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
            nodeStatus = new NodeStatus(path, dir);
        } catch (IOException e) {
            logger.severe("Error loading files from " + path);
            return;
        }

        // Connect to tracker
        Runnable tcpRunnable = () -> {
            try (Socket socket = new Socket(ip, port)) {
                TCPConnection tcpConnection = new TCPConnection(socket);
                NodeHandler nodeHandler = new NodeHandler(path, tcpConnection);
                boolean running = true;

                String response = nodeHandler.ping(nodeStatus.getFileInfos());
                if (response.equals("Pong!")) {
                    logger.info("FS Track Protocol connected to Tracker on " + ip + ":" + port);
                
                    // Start listening for commands
                    while (running) {
                        System.out.print(">> ");
                        String command = stdin.readLine();
                        String[] commandArgs = command.split(" ");

                        switch (commandArgs[0]) {
                            case "EXIT":
                                running = false;
                                break;
                            case "LIST":
                                Map<String, List<String>> res = nodeHandler.list();
                                Map<FileInfo, List<String>> resFiles = res.entrySet().stream()
                                    .collect(
                                        Collectors.toMap(
                                            entry -> FileInfo.fromString(entry.getKey()),
                                            entry -> entry.getValue()
                                        )
                                    );

                                StringBuilder sb = new StringBuilder();
                                Map<String, FileInfo> upToDate = nodeStatus.getFileInfos().stream()
                                    .collect(Collectors.toMap(
                                        FileInfo::getPath,
                                        fileInfo -> fileInfo
                                    ));
                                Map<FileInfo, List<String>> missing = new HashMap<>();
                                Map<FileInfo, List<String>> needingUpdate = new HashMap<>();

                                for (FileInfo fileInfo : resFiles.keySet()) {
                                    if (upToDate.get(fileInfo.getPath()) == null) {
                                        missing.put(fileInfo, resFiles.get(fileInfo));
                                    } else if (upToDate.get(fileInfo.getPath()).getChecksum() != fileInfo.getChecksum()
                                    || !upToDate.get(fileInfo.getPath()).getLastModified().equals(fileInfo.getLastModified())) {
                                        needingUpdate.put(fileInfo, resFiles.get(fileInfo));
                                    }
                                }

                                sb.append("[Up-to-date]\n");
                                for (FileInfo fileInfo : upToDate.values())
                                    sb.append(" Path: ").append(fileInfo.getPath()).append(" Last Modified: ").append(fileInfo.getLastModified()).append(" Checksum: ").append(fileInfo.getChecksum()).append("\n");
                                
                                if (missing.size() > 0) {
                                    sb.append("\n[Missing]\n");
                                    for (FileInfo fileInfo : missing.keySet())
                                        sb.append(" Path: ").append(fileInfo.getPath()).append(" Last Modified: ").append(fileInfo.getLastModified()).append(" Checksum: ").append(fileInfo.getChecksum()).append("\n").append("  ").append(String.join(", ", missing.get(fileInfo))).append("\n");
                                }

                                if (needingUpdate.size() > 0) {
                                    sb.append("\n[Needing Update]\n");
                                    for (FileInfo fileInfo : needingUpdate.keySet())
                                        sb.append(" Path: ").append(fileInfo.getPath()).append(" Last Modified: ").append(fileInfo.getLastModified()).append(" (NEW: ").append(upToDate.get(fileInfo.getPath()).getLastModified()).append(") Checksum: ").append(fileInfo.getChecksum()).append("\n").append("  ").append(String.join(", ", needingUpdate.get(fileInfo))).append("\n");
                                }

                                break;
                            default:
                                logger.info("Invalid command.");
                                continue;
                        }

                        logger.info(response);
                    }
                } else {
                    logger.severe("Error connecting to Tracker.");
                    return;
                }

                tcpConnection.close();
            } catch (UnknownHostException e) {
                logger.severe(ip + " is not a valid IP address.");
            } catch (IOException e) {
                logger.severe("Error connecting to " + ip + " on port " + port + ". Is the Tracker running?");
            }
        };
        tcpRunnable.run();

        // Start UDP server
        Runnable udpRunnable = () -> {
            try {
                DatagramSocket socket = new DatagramSocket();
                logger.info("FS Transfer Protocol listening using UDP on " + port);
                
            } catch (SocketException e) {
                e.printStackTrace();
            }
        };
        udpRunnable.run();
    }
}
