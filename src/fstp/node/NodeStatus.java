package fstp.node;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fstp.Constants;
import fstp.models.FileInfo;
import fstp.models.FileDownload;
import fstp.utils.FileUtils;
import fstp.utils.Tuple;

public class NodeStatus {
    private final File dir;
    private final int port;
    private boolean running;
    private FilePool downloading;
    private final List<Tuple<String, Integer>> peers = new ArrayList<>();
    private final Map<String, FileInfo> fileInfos = new HashMap<>();
    private Map<FileInfo, List<Tuple<String, Integer>>> updateMap = new HashMap<>();

    public NodeStatus(File dir, int port) throws IOException {
        this.dir = dir;
        this.running = true;
        this.downloading = null;
        this.port = port;

        for (File file : FileUtils.getFiles(dir))
            this.loadFile(file);
    }

    public FileInfo loadFile(File file) throws IOException {
        String path = file.getPath().replace(dir.getPath() + "/", "");
        List<Long> chunks = FileUtils.getChunks(file, Constants.UDP_BUFFER_SIZE - 9);
        
        FileInfo fileInfo = new FileInfo(
            path,
            FileUtils.getFileData(file),
            chunks,
            (short) (file.length() % (Constants.UDP_BUFFER_SIZE - 9))
        );

        this.fileInfos.put(path, fileInfo);
        return fileInfo;
    }

    public FileInfo saveFile(FileDownload res) {
        File file = new File(this.dir.getPath() + "/" + res.getPath());
        try {
            FileUtils.emptyFile(file);
        } catch (IOException e) {
            FSNode.logger.severe("Error emptying file " + file.getPath());
            e.printStackTrace();
        }

        try (FileOutputStream fos = new FileOutputStream(file)) {
            for (Long chunk : res.getChunks()) {
                if (!res.gotten(chunk)) {
                    FSNode.logger.info("Chunk " + chunk + " not gotten.");
                    return null;
                }

                byte[] chunkData = res.get(chunk);
                if (chunkData == null) {
                    FSNode.logger.info("Chunk " + chunk + " not gotten.");
                    return null;
                }

                int chunkPos = res.getChunkIndex(chunk);
                if (chunkPos == res.getChunks().size() - 1) {
                    byte[] lastChunkData = new byte[res.getLastChunkSize()];
                    System.arraycopy(chunkData, 0, lastChunkData, 0, res.getLastChunkSize());
                    chunkData = lastChunkData;
                }

                for (byte b : chunkData)
                    fos.write(b);
            }

            file.setLastModified(res.getLastModified().getTime());
            return this.loadFile(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void verifyUpdateList() {
        Map<FileInfo, List<Tuple<String, Integer>>> newUpdateMap = new HashMap<>();
        for (Map.Entry<FileInfo, List<Tuple<String, Integer>>> entry : this.updateMap.entrySet()) {
            FileInfo fileInfo = entry.getKey();
            List<Tuple<String, Integer>> peers = entry.getValue();

            if (peers.size() == 0) continue;

            List<Tuple<String, Integer>> newPeers = new ArrayList<>();
            for (Tuple<String, Integer> peer : peers)
                if (this.peers.contains(peer))
                    newPeers.add(peer);
                
            if (newPeers.size() > 0)
                newUpdateMap.put(fileInfo, newPeers);
        }
        this.updateMap = newUpdateMap;
    }

    public Tuple<FileInfo, List<Tuple<String, Integer>>> getUpdateFileInfo(String path) {
        for (Map.Entry<FileInfo, List<Tuple<String, Integer>>> entry : this.updateMap.entrySet()) {
            FileInfo fileInfo = entry.getKey();
            if (fileInfo.getPath().equals(path))
                return new Tuple<>(fileInfo, entry.getValue());
        }
        return null;
    }

    public Map<FileInfo, List<Tuple<String, Integer>>> getUpdateMap() {
        return this.updateMap;
    }

    public void setUpdateMap(Map<FileInfo, List<Tuple<String, Integer>>> updateMap) {
        this.updateMap = updateMap;
    }

    public boolean isRunning() {
        return this.running;
    }

    public void setRunning(boolean running) {
        this.downloading = null;
        this.running = running;
    }

    public FilePool getDownloading() {
        return this.downloading;
    }

    public void setDownloading(FilePool downloading) {
        this.downloading = downloading;
    }

    public List<Tuple<String, Integer>> getPeers() {
        return this.peers;
    }

    public void addPeer(String peer, int port) {
        this.peers.add(new Tuple<>(peer, port));
    }

    public void clearPeers() {
        this.peers.clear();
    }

    public Map<String, FileInfo> getFileInfos() {
        return this.fileInfos;
    }

    public FileInfo getFileInfo(String path) {
        return this.fileInfos.get(path);
    }

    public byte[] getChunkData(String path, int chunkPos) throws IOException {
        File file = new File(this.dir.getPath() + "/" + path);
        if (!file.exists() || !file.isFile())
            return null;

        return FileUtils.getChunk(file, Constants.UDP_BUFFER_SIZE - 9, chunkPos);
    }

    public void addChunkToDownload(long chunkId, byte[] chunkData) {
        if (this.downloading == null) return;

        this.downloading.gotChunk(chunkId, chunkData);
    }

    public int getPort() {
        return this.port;
    }
}
