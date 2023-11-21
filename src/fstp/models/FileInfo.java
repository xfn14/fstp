package fstp.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FileInfo implements Comparable<FileInfo> {
    private final String path;
    private Date lastModified;
    private List<Long> chunks;

    public FileInfo(String path, Date lastModified) {
        this.path = path;
        this.lastModified = lastModified;
        this.chunks = new ArrayList<>();
    }

    public FileInfo(String path, Date lastModified, List<Long> chunks) {
        this.path = path;
        this.lastModified = lastModified;
        this.chunks = chunks;
    }

    public String getPath() {
        return this.path;
    }

    public Date getLastModified() {
        return this.lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public List<Long> getChunks() {
        return this.chunks;
    }

    public void setChunks(List<Long> chunks) {
        this.chunks = chunks;
    }

    public int getChunksSize() {
        return this.chunks.size();
    }

    /**
     * Convert a FileInfo object to a string
     * 
     * String format: <code>path*checksum*last_modified</code>
     * 
     * @return String representation of FileInfo object
     */
    @Override
    public String toString() {
        return String.format("%s*%s", this.path, this.lastModified.getTime());
    }

    public String toStringExtended() {
        return String.format("%s*%s*%s", this.path, this.lastModified.getTime(), this.chunks.toString());
    }

    /**
     * Convert a string to a FileInfo object
     * 
     * String format: <code>path*checksum*last_modified</code>
     * 
     * @param str String to convert
     * @return FileInfo object
     */
    public static FileInfo fromString(String str) {
        String[] arr = str.split("\\*");
        return new FileInfo(arr[0], new Date(Long.parseLong(arr[1])));
    }

    @Override
    public int compareTo(FileInfo o) {
        return o.getLastModified().compareTo(this.lastModified);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FileInfo) {
            FileInfo other = (FileInfo) obj;
            boolean sameChuncks = true;
            if (this.chunks.size() != other.chunks.size())
                return false;

            for (int i = 0; i < this.chunks.size(); i++)
                if (!this.chunks.get(i).equals(other.chunks.get(i))) {
                    sameChuncks = false;
                    break;
                }

            return this.path.equals(other.path) && this.lastModified.equals(other.lastModified) && sameChuncks;
        }
        return false;
    }

    public int getChunkPos(long chunk) {
        return this.chunks.indexOf(chunk);
    }
}
