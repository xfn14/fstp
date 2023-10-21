package fstp.node;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import fstp.models.FileInfo;

public class Interperter {
    private static final BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
    
    private NodeHandler nodeHandler;
    private NodeStatus nodeStatus;
    private boolean running = true;

    public Interperter(NodeHandler nodeHandler, NodeStatus nodeStatus) {
        this.nodeHandler = nodeHandler;
        this.nodeStatus = nodeStatus;
    }

    public void run() {
        while (running) {
            try {
                System.out.print(">> ");
                String command = stdin.readLine();
                String[] args = command.split(" ");
                switch (args[0]) {
                    case "LIST":
                        FSNode.logger.info("\n" + list());
                        break;
                    case "GET":
                        get(args[1]);
                        break;
                    case "EXIT":
                        exit();
                        break;
                    default:
                        System.out.println("Invalid command.");
                }
            } catch (Exception e) {
                System.out.println("Error reading command.");
            }
        }
    }

    private String list() {
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

        return sb.toString();
    }

    private void get(String filename) {
        FSNode.logger.info("Still needs to be implemented.");
    }

    private void exit() {
        running = false;
    }
}
