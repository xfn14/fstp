package fstp.tracker;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import fstp.models.FileInfo;
import fstp.utils.Tuple;

public class TrackerStatus {
    private final Map<String, List<FileInfo>> files;
    private final Map<FileInfo, Tuple<List<String>, List<String>>> downloadPool;

    public TrackerStatus() {
        this.files = new HashMap<>();
        this.downloadPool = new HashMap<>();
    }

    public FileInfo getMostRecentFile(String path, Date date) {
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
                FileInfo mostRecentFileInfo = this.getMostRecentFile(peerFileInfo.getPath(), peerFileInfo.getLastModified());
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

    public void removeNode(String devString) {
        if (this.files.containsKey(devString))
            this.files.remove(devString);

        Map<FileInfo, Tuple<List<String>, List<String>>> newDowloadPool = new HashMap<>();
        for (Entry<FileInfo, Tuple<List<String>, List<String>>> entry : this.downloadPool.entrySet()) {
            Tuple<List<String>, List<String>> tuple = entry.getValue();
            tuple.getX().remove(devString);
            tuple.getY().remove(devString);

            if (tuple.getX().size() > 0 && tuple.getY().size() > 0)
                newDowloadPool.put(entry.getKey(), tuple);
        }

        this.downloadPool.clear();
        this.downloadPool.putAll(newDowloadPool);
    }
}
