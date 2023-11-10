package fstp.node;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fstp.Constants;
import fstp.models.FileInfo;
import fstp.utils.FileUtils;

public class NodeStatus {
    private boolean running;
    private final List<String> peers = new ArrayList<>();
    private final Map<String, FileInfo> fileInfos = new HashMap<>();
    private Map<FileInfo, List<String>> updateMap = new HashMap<>();

    public NodeStatus(File dir) throws IOException {
        this.running = true;

        List<File> files = FileUtils.getFiles(dir);
        for (File file : files) {
            String path = file.getPath().replace(dir.getPath() + "/", "");
            List<Long> chunks = FileUtils.getChunks(file, Constants.UDP_BUFFER_SIZE);            
            this.fileInfos.put(
                path,
                new FileInfo(
                    path,
                    FileUtils.getFileData(file),
                    chunks
                )
            );
        }
    }

    public boolean getRunning() {
        return this.running;
    }

    public void setRunning(boolean running) {
        this.running = running;
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

    public Map<FileInfo, List<String>> getUpdateMap() {
        return this.updateMap;
    }

    public void setUpdateMap(Map<FileInfo, List<String>> updateMap) {
        this.updateMap = updateMap;
    }
}
