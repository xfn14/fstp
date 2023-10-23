package fstp.node;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fstp.models.FileInfo;
import fstp.utils.FileUtils;

public class NodeStatus {
    private final Map<String, FileInfo> fileInfos = new HashMap<>();
    private final List<String> peers = new ArrayList<>();

    public NodeStatus(File dir) throws IOException {
        List<File> files = FileUtils.getFiles(dir);
        for (File file : files) {
            String path = file.getPath().replace(dir.getPath(), "");
            this.fileInfos.put(
                path,
                new FileInfo(
                    path,
                    FileUtils.fileToChecksum(file),
                    FileUtils.getFileData(file)
                )
            );
        }
    }

    public List<String> getPeers() {
        return this.peers;
    }

    public void addPeer(String peer) {
        this.peers.add(peer);
    }

    public void clearPeers() {
        this.peers.clear();
    }

    public Map<String, FileInfo> getFileInfos() {
        return this.fileInfos;
    }
}
