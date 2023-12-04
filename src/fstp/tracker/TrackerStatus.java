package fstp.tracker;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import fstp.models.FileInfo;
import fstp.utils.Tuple;

public class TrackerStatus {
    private final Map<String, Integer> peerPorts;
    private final Map<String, List<FileInfo>> files;
    private final Map<String, Map<String, List<Long>>> downloadPool;
    
    public TrackerStatus() {
        this.files = new HashMap<>();
        this.peerPorts = new HashMap<>();
        this.downloadPool = new HashMap<>();
    }

    public FileInfo getMostRecentFile(String path) {
        return this.files.values().stream()
            .flatMap(List::stream)
            .filter(fileInfo -> fileInfo.getPath().equals(path))
            .max(Comparator.comparing(FileInfo::getLastModified))
            .orElse(null);
    }

    public List<String> getPeersWithFile(FileInfo fileInfo) {
        List<String> peers = new ArrayList<>();
        for (Entry<String, List<FileInfo>> entry : this.files.entrySet()) {
            String peer = entry.getKey();
            List<FileInfo> files = entry.getValue();

            for (FileInfo file : files) {
                if (file.equals(fileInfo)) {
                    peers.add(peer);
                    break;
                }
            }
        }
        return peers;
    }

    public Map<FileInfo, List<String>> getUpdateList(String addr) {
        Map<FileInfo, List<String>> updateList = new HashMap<>();
        Map<String, Tuple<FileInfo, List<String>>> mostRecent = new HashMap<>();
        Map<String, FileInfo> clientFiles = this.files.get(addr).stream()
            .collect(Collectors.toMap(FileInfo::getPath, fileInfo -> fileInfo));

        if (clientFiles == null) return null;
        if (this.files.size() == 0) return updateList;

        for (Entry<String, List<FileInfo>> entry : this.files.entrySet()) {
            String peerAddr = entry.getKey();
            if (peerAddr.equals(addr)) continue;

            List<FileInfo> peerFiles = entry.getValue();
            if (peerFiles == null || peerFiles.size() == 0) continue;

            for (FileInfo peerFileInfo : peerFiles) {
                FileInfo mostRecentFileInfo = this.getMostRecentFile(peerFileInfo.getPath());
                if (mostRecent.containsKey(mostRecentFileInfo.getPath())) continue;

                List<String> peersWithFile = this.getPeersWithFile(mostRecentFileInfo);
                if (peersWithFile.size() == 0) continue;
                if (peersWithFile.size() == 1 && peersWithFile.get(0).equals(addr)) continue;
                if (peersWithFile.contains(addr)) continue;

                mostRecent.put(mostRecentFileInfo.getPath(), new Tuple<>(mostRecentFileInfo, peersWithFile));
            }
        }
        
        if (mostRecent.size() == 0)
            return updateList;

        return mostRecent.entrySet().stream()
            .collect(Collectors.toMap(
                entry -> entry.getValue().getX(),
                entry -> entry.getValue().getY()
            ));
    }

    public void addFile(String key, FileInfo file) {
        if (this.files.containsKey(key)) {
            List<FileInfo> files = this.files.get(key);
            boolean found = false;

            for (FileInfo fileInfo : files)
                if (fileInfo.getPath().equals(file.getPath())) {
                    fileInfo.setLastModified(file.getLastModified());
                    fileInfo.setChunks(file.getChunks());
                    break;
                }

            if (!found) files.add(file);
            this.files.put(key, files);
        } else {
            this.files.put(key, new ArrayList<>(List.of(file)));
        }
    }

    public List<FileInfo> getFiles(String key) {
        return this.files.get(key);
    }

    public Map<String, List<FileInfo>> getFiles() {
        return this.files;
    }

    public void removeFiles(String key) {
        this.files.remove(key);
    }

    public void initNode(String devString) {
        this.files.put(devString, new ArrayList<>());
    }

    public void removeNode(String node) {
        if (this.files.containsKey(node))
            this.files.remove(node);

        Map<String, Map<String, List<Long>>> newDowloadPool = new HashMap<>();
        for (Entry<String, Map<String, List<Long>>> entry : this.downloadPool.entrySet()) {
            Map<String, List<Long>> map = entry.getValue();
            map.remove(node);

            if (map.size() > 0)
                newDowloadPool.put(entry.getKey(), map);
        }

        this.downloadPool.clear();
        this.downloadPool.putAll(newDowloadPool);
        this.peerPorts.remove(node);
    }

    public void initDownloadProgress(String path, String addr) {
        if (!this.downloadPool.containsKey(path)) {
            HashMap<String, List<Long>> map = new HashMap<>();
            map.put(addr, new ArrayList<>());
            this.downloadPool.put(path, map);
            return;
        }

        this.downloadPool.get(path).put(addr, new ArrayList<>());
    }

    public void addDownloadProgress(String path, String addr, long chunk) {
        if (!this.downloadPool.containsKey(path)
        ||  !this.downloadPool.get(path).containsKey(addr))
            this.initDownloadProgress(path, addr);
        this.downloadPool.get(path).get(addr).add(chunk);
    }

    public Map<String, List<Long>> getDownloadProgress(String addr) {
        return this.downloadPool.get(addr);
    }

    public boolean isNodeDownloadingFile(String path, String addr) {
        if (!this.downloadPool.containsKey(path)) return false;
        return this.downloadPool.get(path).containsKey(addr);
    }

    public void removeDownloadProgress(String path, String addr) {
        if (!this.downloadPool.containsKey(path)) return;
        this.downloadPool.get(path).remove(addr);
    }

    public void addPeerPort(String addr, int port) {
        this.peerPorts.put(addr, port);
    }

    public void removePeerPort(String addr) {
        this.peerPorts.remove(addr);
    }

    public int getPeerPort(String addr) {
        return this.peerPorts.get(addr);
    }

    public List<Tuple<String, Integer>> getPeersNeedFile(String pathFile, long chunkId) {
        List<Tuple<String, Integer>> res = new ArrayList<>();
        for (Entry<String, Map<String, List<Long>>> entry : this.downloadPool.entrySet()) {
            String path = entry.getKey();
            Map<String, List<Long>> map = entry.getValue();

            if (!path.equals(pathFile)) continue;
            for (Entry<String, List<Long>> entry2 : map.entrySet()) {
                String addr = entry2.getKey();
                List<Long> chunks = entry2.getValue();

                if (!chunks.contains(chunkId))
                    res.add(new Tuple<>(addr, this.peerPorts.get(addr)));
            }
        }
        return null;
    }
}
