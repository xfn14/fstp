package fstp.tracker;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import fstp.models.FileInfo;
import fstp.models.Frame;
import fstp.models.sockets.TCPConnection;
import fstp.utils.Tuple;

public class Skeleton {
    private final TrackerStatus trackerStatus;

    public Skeleton(TrackerStatus trackerStatus) {
        this.trackerStatus = trackerStatus;
    }

    public void handle(TCPConnection c) throws IOException {
        Frame frame = c.receive();
        DataInputStream buffer = new DataInputStream(new ByteArrayInputStream(frame.getData()));

        ByteArrayOutputStream bufferOut = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bufferOut);

        switch (frame.getTag()) {
            case 0:
                int port = buffer.readInt();
                this.trackerStatus.addPeerPort(c.getDevString(), port);

                List<String> peers = this.trackerStatus.getFiles().keySet().stream()
                    .filter(peer -> !peer.equals(c.getDevString()))
                    .collect(Collectors.toList());

                out.writeInt(peers.size());
                for (String peer : peers) {
                    out.writeUTF(peer);
                    out.writeInt(this.trackerStatus.getPeerPort(peer));
                }
                c.send(10, bufferOut);

                FSTracker.logger.info("Sending ping response to " + c.getDevString() + " with " + peers.size() + " peers (" + port + ")");
                break;
            case 1:
                String path = buffer.readUTF();
                long lastModified = buffer.readLong();
                short lastChunkSize = buffer.readShort();
                FileInfo fileInfo = new FileInfo(path, new Date(lastModified), lastChunkSize);
                int nblocks = buffer.readInt();

                if (nblocks == 0) {
                    trackerStatus.addFile(c.getDevString(), fileInfo);
                    c.send((byte) 11, bufferOut);
                    break;
                }

                List<Long> blocks = Arrays.stream(new long[nblocks])
                    .mapToObj(i -> {
                        try {
                            return buffer.readLong();
                        } catch (IOException e) {
                            e.printStackTrace();
                            return -1L;
                        }
                    })
                    .collect(Collectors.toList());

                fileInfo.setChunks(blocks);

                if (!blocks.contains(-1L)) {
                    trackerStatus.addFile(c.getDevString(), fileInfo);
                }

                FSTracker.logger.info("Received file " + path + " - " + new Date(lastModified) + " from " + c.getDevString());
                if (this.trackerStatus.isNodeDownloadingFile(path, c.getDevString())) {
                    this.trackerStatus.removeDownloadProgress(path, c.getDevString());
                    FSTracker.logger.info("File has been updated, removing download progress for " + c.getDevString());
                }
                
                c.send((byte) (!blocks.contains(-1L) ? 11 : 40), bufferOut);
                break;
            case 2:
                Map<FileInfo, List<String>> toUpdate = trackerStatus.getUpdateList(c.getDevString());
                if (toUpdate == null || toUpdate.size() == 0) {
                    c.send((byte) 41, bufferOut);
                    break;
                }
                
                out.writeInt(toUpdate.size());
                for (Entry<FileInfo, List<String>> f : toUpdate.entrySet()) {
                    out.writeUTF(f.getKey().getPath());
                    out.writeLong(f.getKey().getLastModified().getTime());
                    out.writeInt(f.getValue().size());
                    for (String addr : f.getValue()) {
                        out.writeUTF(addr);
                        out.writeInt(this.trackerStatus.getPeerPort(addr));
                    }
                }

                FSTracker.logger.info("Sending update list to " + c.getDevString() + " with " + toUpdate.size() + " files");
                c.send((byte) 20, bufferOut);
                break;
            case 3:
                String fileDownload = buffer.readUTF();
                FileInfo fileDownloadInfo = this.trackerStatus.getMostRecentFile(fileDownload);
                
                this.trackerStatus.initDownloadProgress(fileDownload, c.getDevString());

                if (fileDownloadInfo == null || fileDownloadInfo.getChunks().size() == 0) {
                    c.send((byte) 42, bufferOut);
                    break;
                }

                List<Long> chunks = fileDownloadInfo.getChunks();
                out.writeShort(fileDownloadInfo.getLastChunkSize());
                out.writeInt(chunks.size());
                for (Long chunk : chunks)
                    out.writeLong(chunk);

                FSTracker.logger.info("Sending file chunks of " + fileDownload + " to " + c.getDevString());
                c.send((byte) 21, bufferOut);
                break;
            case 4:
                String file = buffer.readUTF();
                Map<String, List<Long>> progress = this.trackerStatus.getDownloadProgress(file);
                if (progress == null || progress.size() == 0) {
                    c.send((byte) 43, bufferOut);
                    break;
                }

                out.writeInt(progress.size());
                for (Entry<String, List<Long>> entry : progress.entrySet()) {
                    String addr = entry.getKey();
                    out.writeUTF(addr);
                    out.writeInt(this.trackerStatus.getPeerPort(entry.getKey()));
                    out.writeInt(entry.getValue().size());
                    for (Long block : entry.getValue())
                        out.writeLong(block);
                }

                FSTracker.logger.info("Sending download progress to " + c.getDevString() + " for file " + file);
                c.send((byte) 22, bufferOut);
                break;
            case 5:
                String pathFile = buffer.readUTF();
                long chunkId = buffer.readLong();
                this.trackerStatus.addDownloadProgress(pathFile, c.getDevString(), chunkId);
                List<Tuple<String, Integer>> peersNeedChunk = this.trackerStatus.getPeersNeedFile(pathFile, chunkId);

                if (peersNeedChunk == null || peersNeedChunk.size() == 0) {
                    c.send((byte) 44, bufferOut);
                    break;
                }

                out.writeInt(peersNeedChunk.size());
                for (Tuple<String, Integer> peer : peersNeedChunk) {
                    out.writeUTF(peer.getX());
                    out.writeInt(peer.getY());
                }

                FSTracker.logger.info("Adding download progress for " + c.getDevString() + " for file " + pathFile + " chunk " + chunkId);
                c.send((byte) 23, bufferOut);
                break;
        }

        if (out.size() > 0)
            out.flush();
    }
}
