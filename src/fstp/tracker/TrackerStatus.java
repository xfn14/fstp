package fstp.tracker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fstp.models.FileInfo;

public class TrackerStatus {
    private final Map<String, List<FileInfo>> files;

    public TrackerStatus() {
        this.files = new HashMap<>();
    }

    public void addFiles(String key, List<FileInfo> files) {
        this.files.put(key, files);
    }

    public List<FileInfo> getFiles(String key) {
        return this.files.get(key);
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
