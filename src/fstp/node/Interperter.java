package fstp.node;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fstp.models.FileDownload;
import fstp.models.FileInfo;
import fstp.utils.Tuple;

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
            try {
                System.out.print(">> ");
                String command = stdin.readLine();
                String[] args = command.split(" ");

                List<String> peers = nodeHandler.ping();
                if (peers.size() != 0) {
                    this.nodeStatus.clearPeers();
                    this.nodeStatus.getPeers().addAll(peers);
                } else FSNode.logger.info("No peers connected to Tracker.");
                
                this.nodeStatus.verifyUpdateList();

                switch (args[0]) {
                    case "LIST":
                        Map<FileInfo, List<String>> response = this.nodeHandler.getUpdateList();
                        this.nodeStatus.setUpdateMap(response);

                        String list = this.list();
                        if (!list.contains("\n")) FSNode.logger.info(list);
                        else FSNode.logger.info("\n" + list);
                        break;
                    case "GET":
                        if (args.length < 2) {
                            FSNode.logger.warning("Invalid usage. Usage: GET <file_path>");
                            break;
                        }

                        Tuple<FileInfo, List<String>> updatFileInfo = this.nodeStatus.getUpdateFileInfo(args[1]);
                        if (updatFileInfo == null) {
                            FSNode.logger.warning("File " + args[1] + " can't be updated. Use LIST to check for available files to update.");
                            break;
                        }

                        this.get(updatFileInfo.getX(), updatFileInfo.getY());
                        break;
                    case "QUIT":
                    case "q":
                        this.exit();
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
            sb.append("Files to update:\n");
            for (Map.Entry<FileInfo, List<String>> entry : updateFiles.entrySet()) {
                FileInfo fileInfo = entry.getKey();
                List<String> peers = entry.getValue();

                sb.append("\t").append(fileInfo.getPath()).append(" - ").append(fileInfo.getLastModified()).append(" - ").append(peers.size()).append(" peers\n");
            }
        }

        return sb.toString();
    }

    private void get(FileInfo updateFile, List<String> peers) {
        FileDownload res = this.nodeHandler.get(updateFile, peers);
        if (res == null) {
            FSNode.logger.warning("Error getting file " + updateFile.getPath());
            return;
        }

        this.nodeStatus.saveFile(res);
        FSNode.logger.info("File " + updateFile.getPath() + " updated successfully.");
    }

    private void exit() {
        this.nodeHandler.exit();
        this.nodeStatus.setRunning(false);
    }
}
