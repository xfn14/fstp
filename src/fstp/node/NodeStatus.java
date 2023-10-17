package fstp.node;

import java.io.File;
import java.io.IOException;
import java.util.List;

import fstp.utils.FileUtils;

public class NodeStatus {
    private List<File> files;

    /**
     * 
     * @param dir Directory to get files from
     */
    public NodeStatus(File dir) {
        this.files = FileUtils.getFiles(dir);
        for (File file : files)
            try {
                FSNode.logger.info("String: " + FileUtils.fileToString(file));
            } catch (IOException e) {
                e.printStackTrace();
            }
    }
}
