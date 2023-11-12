package fstp.node;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fstp.Constants;
import fstp.models.FileInfo;
import fstp.models.FileDownload;
import fstp.utils.FileUtils;
import fstp.utils.Tuple;

public class NodeStatus {
    private final File dir;
    private boolean running;
    private final List<String> peers = new ArrayList<>();
    private final Map<String, FileInfo> fileInfos = new HashMap<>();
    private Map<FileInfo, List<String>> updateMap = new HashMap<>();

    public NodeStatus(File dir) throws IOException {
        this.dir = dir;
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

    public void saveFile(FileDownload res) {
        String filePath = this.dir.getPath() + "/" + res.getPath();
        // TODO save file to disk
    }

    public void verifyUpdateList() {
        Map<FileInfo, List<String>> newUpdateMap = new HashMap<>();
        for (Map.Entry<FileInfo, List<String>> entry : this.updateMap.entrySet()) {
            FileInfo fileInfo = entry.getKey();
            List<String> peers = entry.getValue();

            if (peers.size() == 0) continue;

            List<String> newPeers = new ArrayList<>();
            for (String peer : peers)
                if (this.peers.contains(peer))
                    newPeers.add(peer);
                
            if (newPeers.size() > 0)
                newUpdateMap.put(fileInfo, newPeers);
        }
        this.updateMap = newUpdateMap;
    }

    public Tuple<FileInfo, List<String>> getUpdateFileInfo(String path) {
        for (Map.Entry<FileInfo, List<String>> entry : this.updateMap.entrySet()) {
            FileInfo fileInfo = entry.getKey();
            if (fileInfo.getPath().equals(path))
                return new Tuple<>(fileInfo, entry.getValue());
        }
        return null;
    }

    public Map<FileInfo, List<String>> getUpdateMap() {
        return this.updateMap;
    }

    public void setUpdateMap(Map<FileInfo, List<String>> updateMap) {
        this.updateMap = updateMap;
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
}
