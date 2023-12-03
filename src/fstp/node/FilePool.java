package fstp.node;

import java.util.ArrayList;
import java.util.List;

import fstp.models.FileDownload;
import fstp.utils.Tuple;

public class FilePool {
    private final List<Tuple<String, Integer>> peers;
    private final FileDownload fileDownload;
    private int iteration = 0;

    public FilePool(FileDownload fileDownload, List<Tuple<String, Integer>> peers) {
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

    public List<Tuple<String, Integer>> getPeers() {
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

    public String getProgress() {
        return this.fileDownload.getProgress();
    }
}
