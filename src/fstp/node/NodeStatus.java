package fstp.node;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import fstp.models.FileInfo;
import fstp.utils.FileUtils;

public class NodeStatus {
    private final List<FileInfo> fileInfos = new ArrayList<>();

    public NodeStatus(String path, File dir) throws IOException {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        List<File> files = FileUtils.getFiles(dir);
        for (File file : files) {
            this.fileInfos.add(new FileInfo(
                file.getPath().replace(path, ""),
                FileUtils.fileToChecksum(file),
                FileUtils.getFileData(file)
            ));
        }
    }

    public List<FileInfo> getFileInfos() {
        return this.fileInfos;
    }
}
