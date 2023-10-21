package fstp.models;

import java.util.Date;

public class FileInfo {
    private String path;
    private long checksum;
    private Date lastModified;

    public FileInfo(String path, long checksum, Date lastModified) {
        this.path = path;
        this.checksum = checksum;
        this.lastModified = lastModified;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getChecksum() {
        return this.checksum;
    }

    public void setChecksum(long checksum) {
        this.checksum = checksum;
    }

    public Date getLastModified() {
        return this.lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
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
        return String.format("%s*%d*%s", this.path, this.checksum, this.lastModified.getTime());
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
        return new FileInfo(arr[0], Long.parseLong(arr[1]), new Date(Long.parseLong(arr[2])));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FileInfo) {
            FileInfo other = (FileInfo) obj;
            return this.path.equals(other.path) && this.checksum == other.checksum
                    && this.lastModified.equals(other.lastModified);
        }
        return false;
    }

    public boolean sameFile(FileInfo other) {
        return this.path.equals(other.path);
    }
}
