package fstp.node;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fstp.models.FileDownload;
import fstp.utils.Tuple;

public class FilePool {
    private int iteration = 0;
    private final FileDownload fileDownload;
    private final List<Tuple<String, Integer>> peers;
    private final Map<Tuple<String, Integer>, Tuple<Long, Date>> requested;

    public FilePool(FileDownload fileDownload, List<Tuple<String, Integer>> peers) {
        this.peers = peers;
        this.fileDownload = fileDownload;
        this.requested = new HashMap<>();
    }

    public void addRequest(Tuple<String, Integer> peer, long chunkId) {
        this.requested.put(peer, new Tuple<>(chunkId, new Date()));
    }

    public void removeRequest(long chunkId) {
        Tuple<String, Integer> toRemove = null;
        for (Tuple<String, Integer> peer : this.requested.keySet())
            if (this.requested.get(peer).getX() == chunkId)
                toRemove = peer;

        if (toRemove != null) this.requested.remove(toRemove);
    }

    public void removeRequest(Tuple<String, Integer> peer) {
        this.requested.remove(peer);
    }

    public Tuple<Long, Date> getRequest(Tuple<String, Integer> peer) {
        return this.requested.get(peer);
    }

    public boolean hasRequested(Tuple<String, Integer> peer) {
        return this.requested.containsKey(peer);
    }

    public boolean chunkRequested(long chunkId) {
        for (Tuple<Long, Date> val : this.requested.values())
            if (val.getX() == chunkId) return true;
        return false;
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
            if (!this.fileDownload.gotten(chunkId) && !this.chunkRequested(chunkId))
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
