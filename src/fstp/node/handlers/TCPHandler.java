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

import fstp.models.FileDownload;
import fstp.models.FileInfo;
import fstp.models.Frame;
import fstp.node.FSNode;
import fstp.node.FilePool;
import fstp.sockets.TCPConnection;

public class TCPHandler {
    private final TCPConnection connection;
    private final ByteArrayOutputStream buffer;
    private final DataOutputStream out;

    public TCPHandler(TCPConnection connection) {
        this.connection = connection;
        this.buffer = new ByteArrayOutputStream();
        this.out = new DataOutputStream(this.buffer);
    }

    public List<String> ping() {
        List<String> peers = new ArrayList<>();

        try {
            this.connection.send(0, this.buffer);

            Frame response = this.connection.receive();
            if (response.getTag() == 11) return peers;

            DataInputStream in = new DataInputStream(new ByteArrayInputStream(response.getData()));
            int nPeers = in.readInt();
            for (int i = 0; i < nPeers; i++) 
                peers.add(in.readUTF());
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

    public Map<FileInfo, List<String>> getUpdateList() {
        Map<FileInfo, List<String>> res = new HashMap<>();
        
        try {
            this.connection.send(2, this.buffer);

            Frame response = this.connection.receive();
            if (response.getTag() == 21) return res;

            DataInputStream in = new DataInputStream(new ByteArrayInputStream(response.getData()));
            int len = in.readInt();

            for (int i = 0; i < len; i++) {
                String path = in.readUTF();
                long lastModified = in.readLong();
                int npeers = in.readInt();
                if (npeers == 0) continue;

                List<String> peers = new ArrayList<>();
                for (int j = 0; j < npeers; j++) 
                    peers.add(in.readUTF());

                res.put(new FileInfo(path, new Date(lastModified)), peers);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return res;
    }

    public FileDownload get(FileInfo updateFile, List<String> peers) {
        // FilePool filePool = new FilePool(updateFile.getPath(), peers);
        // if (filePool.getPeers().size() == 0) return null;

        // try {
        //     this.out.writeUTF(updateFile.getPath());
        //     this.out.writeLong(updateFile.getLastModified().getTime());
        //     this.out.writeInt(peers.size());
            
        //     for (String peer : peers)
        //         this.out.writeUTF(peer);
            
        //     this.connection.send(3, this.buffer);

        //     Frame response = this.connection.receive();
        //     if (response.getTag() == 41) return null;

        //     DataInputStream in = new DataInputStream(new ByteArrayInputStream(response.getData()));
        //     int len = in.readInt();

        //     List<Long> chucks = new ArrayList<>();
        //     for (int i = 0; i < len; i++)
        //         chucks.add(in.readLong());

        //     updateFile.setChunks(chucks);

            
        // } catch (IOException e) {
        //     e.printStackTrace();
        //     return null;
        // }
        

        // FileDownload fileDownload = new FileDownload(updateFile.getPath(), updateFile.getLastModified());
        return null;
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