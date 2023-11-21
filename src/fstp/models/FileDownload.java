package fstp.models;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileDownload extends FileInfo {
    private final Map<Long, byte[]> gotten;

    public FileDownload(String path, Date lastModified, List<Long> chunks) {
        super(path, lastModified, chunks);
        this.gotten = new HashMap<>();
    }

    public void add(long block, byte[] data) {
        this.gotten.put(block, data);
    }

    public boolean gotten(long block) {
        return this.gotten.containsKey(block);
    }

    public boolean isComplete() {
        return this.gotten.size() == this.getChunksSize();
    }
}
