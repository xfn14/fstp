package fstp.node;

import java.io.File;
import java.io.IOException;
import java.util.List;

import fstp.utils.FileUtils;

public class NodeStatus {
    private List<File> files;

    public NodeStatus(File dir) {
        this.files = FileUtils.getFiles(dir);
    }

    public List<File> getFiles() {
        return this.files;
    }
}
