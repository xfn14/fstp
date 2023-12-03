package fstp.tests;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import fstp.Constants;
import fstp.models.FileInfo;
import fstp.node.NodeStatus;
import fstp.utils.FileUtils;

public class Test {
    public static void main(String[] args) throws IOException {
        File dir = new File("/home/fn14/workspace/lei/fstp/shared/");
        System.out.println(dir.getPath());
        NodeStatus nodeStatus = new NodeStatus(dir, Constants.DEFAULT_PORT);

        for (Map.Entry<String, FileInfo> entry : nodeStatus.getFileInfos().entrySet()) {
            String path = entry.getKey();
            FileInfo fileInfo = entry.getValue();
            System.out.println(path);
            for (long chunk : fileInfo.getChunks()) {
                int chunkPos = fileInfo.getChunkIndex(chunk);
                System.out.println(chunk + " " + chunkPos);
                byte[] chunkData = nodeStatus.getChunkData(path, chunkPos);
                if (chunkData == null) {
                    System.out.println("null");
                    continue;
                }
                long checksum = FileUtils.checksumByteArr(chunkData);
                System.out.println(checksum);
            }
            
        }

        
    }
}
