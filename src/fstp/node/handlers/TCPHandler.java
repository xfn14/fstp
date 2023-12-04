package fstp.node.handlers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fstp.models.FileInfo;
import fstp.models.Frame;
import fstp.models.sockets.TCPConnection;
import fstp.node.FSNode;
import fstp.utils.Tuple;

public class TCPHandler {
    private final TCPConnection connection;
    private final ByteArrayOutputStream buffer;
    private final DataOutputStream out;

    public TCPHandler(TCPConnection connection) {
        this.connection = connection;
        this.buffer = new ByteArrayOutputStream();
        this.out = new DataOutputStream(this.buffer);
    }

    public List<Tuple<String, Integer>> ping(int port) {
        List<Tuple<String, Integer>> peers = new ArrayList<>();

        try {
            this.out.writeInt(port);
            this.connection.send(0, this.buffer);

            Frame response = this.connection.receive();
            if (response.getTag() == 11) return peers;

            DataInputStream in = new DataInputStream(new ByteArrayInputStream(response.getData()));
            int nPeers = in.readInt();
            for (int i = 0; i < nPeers; i++) {
                String addr = in.readUTF();
                int peerPort = in.readInt();
                peers.add(new Tuple<>(addr, peerPort));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return peers;
    }

    public boolean registerFiles(List<FileInfo> fileInfos) {
        boolean finalRes = true;
        for (FileInfo fileInfo : fileInfos) {
            int res = this.registerFile(fileInfo);
            
            if (res != 11) {
                finalRes = false;
                FSNode.logger.warning("Error registering file " + fileInfo.getPath());
            } else FSNode.logger.info("File " + fileInfo.getPath() + " registered successfully.");
        }
        return finalRes;
    }

    public int registerFile(FileInfo fileInfo) {
        try {
            List<Long> chunks = fileInfo.getChunks();
            this.out.writeUTF(fileInfo.getPath());
            this.out.writeLong(fileInfo.getLastModified().getTime());
            this.out.writeShort(fileInfo.getLastChunkSize());
            this.out.writeInt(chunks.size());

            for (Long chunk : chunks)
                this.out.writeLong(chunk);

            this.connection.send(1, this.buffer);

            Frame response = this.connection.receive();
            return response.getTag();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 40;
    }

    public Map<FileInfo, List<Tuple<String, Integer>>> getUpdateList() {
        Map<FileInfo, List<Tuple<String, Integer>>> res = new HashMap<>();
        
        try {
            this.connection.send(2, this.buffer);

            Frame response = this.connection.receive();
            if (response.getTag() == 41) return res;

            DataInputStream in = new DataInputStream(new ByteArrayInputStream(response.getData()));
            int len = in.readInt();

            for (int i = 0; i < len; i++) {
                String path = in.readUTF();
                long lastModified = in.readLong();
                int npeers = in.readInt();
                if (npeers == 0) continue;

                List<Tuple<String, Integer>> peers = new ArrayList<>();
                for (int j = 0; j < npeers; j++) {
                    String addr = in.readUTF();
                    int port = in.readInt();
                    peers.add(new Tuple<>(addr, port));
                }

                res.put(new FileInfo(path, new Date(lastModified)), peers);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return res;
    }

    public Tuple<Short, List<Long>> getFileChunks(String path) {
        List<Long> res = new ArrayList<>();

        try {
            this.out.writeUTF(path);
            this.connection.send(3, this.buffer);

            Frame response = this.connection.receive();
            if (response.getTag() == 42) return new Tuple<>((short) -1, res);

            DataInputStream in = new DataInputStream(new ByteArrayInputStream(response.getData()));
            short lastChunkSize = in.readShort();
            int len = in.readInt();

            for (int i = 0; i < len; i++)
                res.add(in.readLong());
            
            return new Tuple<>(lastChunkSize, res);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<Tuple<String, Integer>> ackChunk(String path, long chunkId) {
        List<Tuple<String, Integer>> res = new ArrayList<>();

        try {
            this.out.writeUTF(path);
            this.out.writeLong(chunkId);
            this.connection.send(5, this.buffer);

            Frame response = this.connection.receive();
            if (response.getTag() == 51) return res;

            DataInputStream in = new DataInputStream(new ByteArrayInputStream(response.getData()));
            int len = in.readInt();

            for (int i = 0; i < len; i++) {
                String addr = in.readUTF();
                int port = in.readInt();
                res.add(new Tuple<>(addr, port));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return res;
    }

    public Map<Tuple<String, Integer>, List<Long>> listPeersDownloadingFile(String file) {
        Map<Tuple<String, Integer>, List<Long>> res = new HashMap<>();

        try {
            this.out.writeUTF(file);
            this.connection.send(4, this.buffer);

            Frame response = this.connection.receive();
            if (response.getTag() == 43) return res;

            DataInputStream in = new DataInputStream(new ByteArrayInputStream(response.getData()));
            int len = in.readInt();

            for (int i = 0; i < len; i++) {
                String peer = in.readUTF();
                int port = in.readInt();
                int nChunks = in.readInt();

                List<Long> chunks = new ArrayList<>();
                for (int j = 0; j < nChunks; j++)
                    chunks.add(in.readLong());

                res.put(new Tuple<>(peer, port), chunks);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return res;
    }

    public void exit() {
        // TODO: send exit message
    }

    public String getDevString() {
        return this.connection.getDevString();
    }

    public void close() throws IOException {
        this.connection.close();
    }
}
