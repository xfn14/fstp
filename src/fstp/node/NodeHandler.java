package fstp.node;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fstp.models.FileInfo;
import fstp.models.Frame;
import fstp.sockets.TCPConnection;

public class NodeHandler {
    private final TCPConnection connection;
    private final ByteArrayOutputStream buffer;
    private final DataOutputStream out;
    
    public NodeHandler(TCPConnection connection) {
        this.connection = connection;
        this.buffer = new ByteArrayOutputStream();
        this.out = new DataOutputStream(this.buffer);
    }

    public boolean registerFiles(List<FileInfo> fileInfos) {
        boolean finalRes = true;
        for (FileInfo fileInfo : fileInfos) {
            int res = this.registerFile(fileInfo);
            
            if (res != 10) {
                finalRes = false;
                FSNode.logger.warning("Error registering file " + fileInfo.getPath());
            } else FSNode.logger.info("File " + fileInfo.getPath() + " registered successfully.");
        }
        return finalRes;
    }

    public int registerFile(FileInfo fileInfo) {
        try {
            List<Long> chunks = fileInfo.getChunks();
            this.out.writeUTF(fileInfo.toString());
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
                String str = in.readUTF();
                int npeers = in.readInt();
                if (npeers == 0) continue;

                List<String> peers = new ArrayList<>();
                for (int j = 0; j < npeers; j++) 
                    peers.add(in.readUTF());

                res.put(FileInfo.fromString(str), peers);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return res;
    }

    public void exit() {
        // TODO: Send exit message to tracker
    }
}
