package fstp.tracker;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import fstp.models.FileInfo;

public class TrackerStatus {
    private final Map<String, List<FileInfo>> files;

    public TrackerStatus() {
        this.files = new HashMap<>();
    }

    public FileInfo getMostRecentFile(String path) {
        return files.values().stream()
            .flatMap(List::stream)
            .filter(fileInfo -> fileInfo.getPath().equals(path))
            .max(Comparator.comparing(FileInfo::getLastModified))
            .orElse(null);
    }

    public List<String> getFilePeers(String path) {
        FileInfo file = this.getMostRecentFile(path);
        if (file == null) return null;

        return this.files.entrySet().stream()
            .filter(entry -> entry.getValue().contains(file))
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    // public Map<FileInfo, List<String>> getUpdateList(String clientAddress) {
    //     Map<FileInfo, List<String>> updateList = new HashMap<>();
    //     List<FileInfo> clientFiles = this.files.get(clientAddress);

    //     for (Entry<String, List<FileInfo>> entry : this.files.entrySet()) {
    //         String peerAddress = entry.getKey();
    //         if (peerAddress.equals(clientAddress)) continue;

    //         List<FileInfo> peerFiles = entry.getValue();

    //         for (FileInfo peerFileInfo : peerFiles)
    //             if (!fileExistsInList(peerFileInfo, clientFiles))
    //                 addOrUpdateFileInfo(updateList, peerFileInfo, peerAddress);
    //     }

    //     return updateList;
    // }

    // private boolean fileExistsInList(FileInfo fileInfo, List<FileInfo> fileInfoList) {
    //     return fileInfoList.stream()
    //         .anyMatch(clientFileInfo -> clientFileInfo.getPath().equals(fileInfo.getPath()));
    // }

    // private void addOrUpdateFileInfo(Map<FileInfo, List<String>> updateList, FileInfo fileInfo, String peerAddress) {
    //     updateList.compute(fileInfo, (key, value) -> {
    //         if (value == null) {
    //             return new ArrayList<>(Collections.singletonList(peerAddress));
    //         } else {
    //             if (fileInfo.getLastModified().before(key.getLastModified())) {
    //                 key.setLastModified(fileInfo.getLastModified());
    //                 key.setChecksum(fileInfo.getChecksum());
    //                 return new ArrayList<>(Collections.singletonList(peerAddress));
    //             } else if (fileInfo.getLastModified().equals(key.getLastModified()))
    //                 value.add(peerAddress);
    //             return value;
    //         }
    //     });
    // }

    public Map<FileInfo, List<String>> getUpdateList(String clientAddress) {
        Map<FileInfo, List<String>> updateList = new HashMap<>();
        List<FileInfo> clientFiles = this.files.get(clientAddress);
        System.out.println(clientFiles);
        System.out.println(clientAddress);

        for (Entry<String, List<FileInfo>> entry : this.files.entrySet()) {
            String peerAddress = entry.getKey();
            if (peerAddress.equals(clientAddress)) continue;

            List<FileInfo> peerFiles = entry.getValue();

            for (FileInfo peerFileInfo : peerFiles) {
                boolean found = false;
                for (FileInfo clientFileInfo : clientFiles) {
                    if (peerFileInfo.getPath().equals(clientFileInfo.getPath())) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    if (updateList.containsKey(peerFileInfo)) {
                        updateList.get(peerFileInfo).add(peerAddress);
                        continue;
                    }

                    for (Entry<FileInfo, List<String>> updateEntry : updateList.entrySet()) {
                        FileInfo updateFileInfo = updateEntry.getKey();
                        if (updateFileInfo.getPath().equals(peerFileInfo.getPath())) {
                            if (updateFileInfo.getLastModified().before(peerFileInfo.getLastModified())) {
                                updateEntry.getKey().setLastModified(peerFileInfo.getLastModified());
                                updateEntry.getKey().setChecksum(peerFileInfo.getChecksum());
                                updateEntry.setValue(List.of(peerAddress));
                            } else if (updateFileInfo.getLastModified().equals(peerFileInfo.getLastModified())) {
                                updateEntry.getValue().add(peerAddress);
                                continue;
                            }
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        updateList.put(peerFileInfo, List.of(peerAddress));
                    }
                } else {
                    for (Entry<FileInfo, List<String>> updateEntry : updateList.entrySet()) {
                        FileInfo updateFileInfo = updateEntry.getKey();
                        if (updateFileInfo.getPath().equals(peerFileInfo.getPath())) {
                            if (updateFileInfo.getLastModified().before(peerFileInfo.getLastModified())) {
                                updateEntry.getKey().setLastModified(peerFileInfo.getLastModified());
                                updateEntry.getKey().setChecksum(peerFileInfo.getChecksum());
                                updateEntry.setValue(List.of(peerAddress));
                            } else if (updateFileInfo.getLastModified().equals(peerFileInfo.getLastModified())) {
                                updateEntry.getValue().add(peerAddress);
                                continue;
                            }
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        updateList.put(peerFileInfo, List.of(peerAddress));
                    }
                }
            }
        }

        return updateList;
    }

    public void addFiles(String key, List<FileInfo> files) {
        this.files.put(key, files);
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
}
