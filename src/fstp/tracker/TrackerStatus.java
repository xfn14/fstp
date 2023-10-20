package fstp.tracker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import fstp.models.FileInfo;
import fstp.utils.Tuple;

public class TrackerStatus {
    private final Map<String, List<FileInfo>> files;

    public TrackerStatus() {
        this.files = new HashMap<>();
    }

    public Map<FileInfo, List<String>> getUpdateList(String clientAddress) {
        Map<String, Map<FileInfo, List<String>>> needUpdate = new HashMap<>();
        Map<String, FileInfo> clientFiles = this.getFiles(clientAddress).stream()
            .collect(Collectors.toMap(
                FileInfo::getPath,
                file -> file
            ));

        // TODO: Calculate the files that need to be updated

        return needUpdate.entrySet().stream()
            .flatMap(entry -> entry.getValue().entrySet().stream()
                .map(file -> new Tuple<>(file.getKey(), file.getValue()))
            )
            .collect(Collectors.toMap(
                Tuple::getX,
                Tuple::getY
            ));
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String key : this.files.keySet()) {
            sb.append(key + "\n");
            for (FileInfo fileInfo : this.files.get(key))
                sb.append("\t" + fileInfo + "\n");
        }
        return sb.toString();
    }
}
