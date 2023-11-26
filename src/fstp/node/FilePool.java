package fstp.node;

import java.util.ArrayList;
import java.util.List;

import fstp.models.FileDownload;

public class FilePool {
    private final List<String> peers;
    private final FileDownload fileDownload;
    private int iteration = 0;

    public FilePool(FileDownload fileDownload, List<String> peers) {
        this.peers = peers;
        this.fileDownload = fileDownload;
    }

    public long getNextChunkToRequest() {
        List<Long> toRequest = this.getChunksToRequest();
        if (toRequest.size() == 0) return -1L;
        else if (toRequest.size() == 1) return toRequest.get(0);

        int pos = (this.iteration + 1) % toRequest.size();
        long chunkId = toRequest.get(pos);
        return chunkId;
    }

    public List<Long> getChunksToRequest() {
        List<Long> toRequest = new ArrayList<>();
        for (long chunkId : this.fileDownload.getChunks())
            if (!this.fileDownload.gotten(chunkId))
                toRequest.add(chunkId);
        return toRequest;
    }

    public void nextIteration() {
        ++this.iteration;
    }

    public List<String> getPeers() {
        return this.peers;
    }

    public FileDownload getFileDownload() {
        return this.fileDownload;
    }

    public boolean isComplete() {
        return this.fileDownload.isComplete();
    }

    public String getPath() {
        return this.fileDownload.getPath();
    }

    public void gotChunk(long chunkId, byte[] chunkData) {
        this.fileDownload.add(chunkId, chunkData);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("FilePool {\n");
        sb.append("\tpeers: [");
        for (String peer : this.peers) sb.append(peer + ", ");
        sb.append("]\n");
        sb.append("\tfileDownload: " + this.fileDownload + "\n");
        sb.append("}");
        return sb.toString();
    }

    public String getProgress() {
        return this.fileDownload.getProgress();
    }
}
