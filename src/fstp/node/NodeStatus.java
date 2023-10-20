package fstp.node;

import java.io.File;
import java.util.List;

import fstp.utils.FileUtils;

public class NodeStatus {
    private List<File> files;

    public NodeStatus(String path, File dir) {
        this.files = FileUtils.getFiles(dir);
    }

    public List<File> getFiles() {
        return this.files;
    }
}
