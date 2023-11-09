package fstp.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FileDownload extends FileInfo {
    private final List<Long> gotten;

    public FileDownload(String path, Date lastModified) {
        super(path, lastModified);
        this.gotten = new ArrayList<>();
    }

    public void addChunk(long block) {
        this.gotten.add(block);
    }

    public boolean gotten(long block) {
        return this.gotten.contains(block);
    }
}
