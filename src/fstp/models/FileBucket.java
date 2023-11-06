package fstp.models;

import java.util.Date;

public class FileBucket extends FileInfo {
    private final byte[] data;

    public FileBucket(String path, long checksum, Date lastModified, byte[] data) {
        super(path, checksum, lastModified);
        this.data = data;
    }
}
