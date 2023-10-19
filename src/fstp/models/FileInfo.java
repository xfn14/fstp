package fstp.models;

public class FileInfo {
    private final String path;
    private final long checksum;
    private final String lastModified;

    public FileInfo(String path, long checksum, String lastModified) {
        this.path = path;
        this.checksum = checksum;
        this.lastModified = lastModified;
    }

    public String getPath() {
        return this.path;
    }

    public long getChecksum() {
        return this.checksum;
    }

    public String getLastModified() {
        return this.lastModified;
    }

    @Override
    public String toString() {
        return String.format("Path: %s\nChecksum: %d\nLast Modified: %s", this.path, this.checksum, this.lastModified);
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
        return new FileInfo(arr[0], Long.parseLong(arr[1]), arr[2]);
    }
}
