package fstp.models;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FileDownload extends FileInfo {
    private final Map<Long, byte[]> gotten;

    public FileDownload(String path, Date lastModified) {
        super(path, lastModified);
        this.gotten = new HashMap<>();
    }

    public void add(long block, byte[] data) {
        this.gotten.put(block, data);
    }

    public boolean gotten(long block) {
        return this.gotten.containsKey(block);
    }
}
