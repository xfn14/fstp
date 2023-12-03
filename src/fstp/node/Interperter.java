package fstp.node;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fstp.models.FileDownload;
import fstp.models.FileInfo;
import fstp.node.handlers.TCPHandler;
import fstp.utils.Tuple;

public class Interperter {
    private static final BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
    
    private TCPHandler tcpHandler;
    private NodeStatus nodeStatus;

    public Interperter(TCPHandler tcpHandler, NodeStatus nodeStatus) {
        this.tcpHandler = tcpHandler;
        this.nodeStatus = nodeStatus;
    }

    public void run() {
        while (this.nodeStatus.isRunning()) {
            try {
                System.out.print(">> ");
                String command = stdin.readLine();
                String[] args = command.split(" ");

                List<Tuple<String, Integer>> peers = this.tcpHandler.ping(nodeStatus.getPort());
                if (peers.size() != 0) {
                    this.nodeStatus.clearPeers();
                    this.nodeStatus.getPeers().addAll(peers);
                }
                
                Map<FileInfo, List<Tuple<String, Integer>>> response = this.tcpHandler.getUpdateList();
                this.nodeStatus.setUpdateMap(response);
                this.nodeStatus.verifyUpdateList();

                switch (args[0].toLowerCase()) {
                    case "l":
                    case "list":
                        if (peers.size() == 0)
                            FSNode.logger.info("No peers connected to Tracker.");

                        String list = this.list();
                        if (!list.contains("\n")) FSNode.logger.info(list);
                        else FSNode.logger.info("\n" + list);
                        break;
                    case "g":
                    case "get":
                        if (args.length < 2) {
                            FSNode.logger.warning("Invalid usage. Usage: GET <file_path>");
                            break;
                        }

                        Tuple<FileInfo, List<Tuple<String, Integer>>> updateFileInfo = this.nodeStatus.getUpdateFileInfo(args[1]);
                        if (updateFileInfo == null) {
                            FSNode.logger.warning("File " + args[1] + " can't be updated. Use LIST to check for available files to update.");
                            break;
                        }

                        this.get(updateFileInfo.getX(), updateFileInfo.getY());
                        break;
                    case "q":
                    case "quit":
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
        Map<FileInfo, List<Tuple<String, Integer>>> updateList = this.tcpHandler.getUpdateList();
        nodeStatus.setUpdateMap(updateList);
        StringBuilder sb = new StringBuilder();
        if (updateList.size() == 0) {
            sb.append("Everything up-to-date.");
            return sb.toString();
        }

        Map<FileInfo, List<Tuple<String, Integer>>> newFiles = new HashMap<>();
        Map<FileInfo, List<Tuple<String, Integer>>> updateFiles = new HashMap<>();
        
        for (Map.Entry<FileInfo, List<Tuple<String, Integer>>> entry : updateList.entrySet()) {
            FileInfo fileInfo = entry.getKey();
            List<Tuple<String, Integer>> peers = entry.getValue();

            if (this.nodeStatus.getFileInfos().containsKey(fileInfo.getPath()))
                updateFiles.put(fileInfo, peers);
            else newFiles.put(fileInfo, peers);
        }

        if (newFiles.size() > 0) {
            sb.append("New files:\n");
            for (Map.Entry<FileInfo, List<Tuple<String, Integer>>> entry : newFiles.entrySet()) {
                FileInfo fileInfo = entry.getKey();
                List<Tuple<String, Integer>> peers = entry.getValue();

                sb.append("\t").append(fileInfo.getPath()).append(" - ").append(fileInfo.getLastModified()).append(" - ").append(peers.size()).append(" peers\n");
            }
        }

        if (updateFiles.size() > 0) {
            sb.append("Files to update:\n");
            for (Map.Entry<FileInfo, List<Tuple<String, Integer>>> entry : updateFiles.entrySet()) {
                FileInfo fileInfo = entry.getKey();
                List<Tuple<String, Integer>> peers = entry.getValue();

                sb.append("\t").append(fileInfo.getPath()).append(" - ").append(fileInfo.getLastModified()).append(" - ").append(peers.size()).append(" peers\n");
            }
        }

        return sb.toString();
    }

    private void get(FileInfo updateFile, List<Tuple<String, Integer>> peers) {
        Tuple<Short, List<Long>> res = this.tcpHandler.getFileChunks(updateFile.getPath());
        if (res == null) {
            FSNode.logger.warning("Error getting file " + updateFile.getPath());
            return;
        } else if (res.getY() == null || res.getY().size() == 0) {
            FSNode.logger.info("File " + updateFile.getPath() + " is empty.");
            this.nodeStatus.saveFile(
                new FileDownload(
                    updateFile.getPath(),
                    updateFile.getLastModified(),
                    new ArrayList<>(),
                    (short) 0
                )
            );
            return;
        }

        FilePool filePool = new FilePool(
            new FileDownload(
                updateFile.getPath(),
                updateFile.getLastModified(),
                res.getY(),
                res.getX()
            ),
            peers
        );

        this.nodeStatus.setDownloading(filePool);

        FSNode.logger.info("Download started for file " + updateFile.getPath() + " from " + peers.size() + " peers.");
        while (this.nodeStatus.getDownloading() != null) {
            try {
                FSNode.logger.info("Download progress: " + this.nodeStatus.getDownloading().getProgress());
                Thread.sleep(1000);
            } catch (Exception e) {
                FSNode.logger.warning("Error sleeping thread.");
            }
        }

        FSNode.logger.info("File " + updateFile.getPath() + " finished downloading.");
    }

    private void exit() {
        this.tcpHandler.exit();
        this.nodeStatus.setRunning(false);
    }
}
