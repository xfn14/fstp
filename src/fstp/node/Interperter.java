package fstp.node;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fstp.models.FileInfo;

public class Interperter {
    private static final BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
    
    private NodeHandler nodeHandler;
    private NodeStatus nodeStatus;

    public Interperter(NodeHandler nodeHandler, NodeStatus nodeStatus) {
        this.nodeHandler = nodeHandler;
        this.nodeStatus = nodeStatus;
    }

    public void run() {
        while (nodeStatus.getRunning()) {
            List<String> peers = nodeHandler.ping();
            if (peers.size() == 0)
            FSNode.logger.info("No peers connected to Tracker.");
            
            nodeStatus.clearPeers();
            nodeStatus.getPeers().addAll(peers);

            try {
                System.out.print(">> ");
                String command = stdin.readLine();
                String[] args = command.split(" ");
                switch (args[0]) {
                    case "LIST":
                        FSNode.logger.info("\n" + list());
                        break;
                    case "GET":
                        if (args.length < 2) {
                            FSNode.logger.warning("Invalid usage. Usage: GET <file_path>");
                            break;
                        }

                        get(args[1]);
                        break;
                    case "QUIT":
                    case "q":
                        exit();
                        break;
                    default:
                        FSNode.logger.warning("Invalid command.");
                }
            } catch (Exception e) {
                FSNode.logger.warning("Error reading command.");
            }
        }
    }

    private String list() {
        Map<FileInfo, List<String>> updateList = this.nodeHandler.getUpdateList();
        nodeStatus.setUpdateMap(updateList);
        StringBuilder sb = new StringBuilder();
        if (updateList.size() == 0) {
            sb.append("Everything up-to-date.");
            return sb.toString();
        }

        Map<FileInfo, List<String>> newFiles = new HashMap<>();
        Map<FileInfo, List<String>> updateFiles = new HashMap<>();
        
        for (Map.Entry<FileInfo, List<String>> entry : updateList.entrySet()) {
            FileInfo fileInfo = entry.getKey();
            List<String> peers = entry.getValue();

            if (this.nodeStatus.getFileInfos().containsKey(fileInfo.getPath()))
                updateFiles.put(fileInfo, peers);
            else newFiles.put(fileInfo, peers);
        }

        if (newFiles.size() > 0) {
            sb.append("New files:\n");
            for (Map.Entry<FileInfo, List<String>> entry : newFiles.entrySet()) {
                FileInfo fileInfo = entry.getKey();
                List<String> peers = entry.getValue();

                sb.append("\t").append(fileInfo.getPath()).append(" - ").append(fileInfo.getLastModified()).append(" - ").append(peers.size()).append(" peers\n");
            }
        }

        if (updateFiles.size() > 0) {
            sb.append("Updated files:\n");
            for (Map.Entry<FileInfo, List<String>> entry : updateFiles.entrySet()) {
                FileInfo fileInfo = entry.getKey();
                List<String> peers = entry.getValue();

                sb.append("\t").append(fileInfo.getPath()).append(" - ").append(fileInfo.getLastModified()).append(" - ").append(peers.size()).append(" peers\n");
            }
        }

        return sb.toString();
    }

    private void get(String file) {
    }

    private void exit() {
        this.nodeHandler.exit();
        this.nodeStatus.setRunning(false);
    }
}
