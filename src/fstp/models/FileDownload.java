package fstp.models;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileDownload extends FileInfo {
    private final Map<Long, byte[]> gotten;

    public FileDownload(String path, Date lastModified, List<Long> chunks, int lastChunkSize) {
        super(path, lastModified, chunks, lastChunkSize);
        this.gotten = new HashMap<>();
    }

    public void add(long block, byte[] data) {
        this.gotten.put(block, data);
    }

    public byte[] get(long block) {
        return this.gotten.get(block);
    }

    public boolean gotten(long block) {
        return this.gotten.containsKey(block);
    }

    public boolean isComplete() {
        return this.gotten.size() == this.getChunksSize();
    }

    public int gottenSize() {
        return this.gotten.size();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("FileDownload {\n");
        sb.append("\tpath: " + this.getPath() + "\n");
        sb.append("\tlastModified: " + this.getLastModified() + "\n");
        sb.append("\tchunks: [");
        for (long chunk : this.getChunks()) sb.append(chunk + ", ");
        sb.append("]\n");
        sb.append("\tgotten: [");
        for (long chunk : this.gotten.keySet()) sb.append(chunk + ", ");
        sb.append("]\n");
        sb.append("}");
        return sb.toString();
    }

    public String getProgress() {
        return this.gotten.size() + "/" + this.getChunksSize();
    }
}
