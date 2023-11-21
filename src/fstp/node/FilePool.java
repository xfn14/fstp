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

        long chunkId = toRequest.get(this.iteration);
        this.iteration = (this.iteration + 1) % toRequest.size();
        return chunkId;
    }

    public List<Long> getChunksToRequest() {
        List<Long> toRequest = new ArrayList<>();
        for (long i = 0; i < this.fileDownload.getChunksSize(); i++)
            if (!this.fileDownload.gotten(i)) toRequest.add(i);
        return toRequest;
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
}
