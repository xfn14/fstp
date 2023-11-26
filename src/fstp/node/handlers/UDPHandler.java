package fstp.node.handlers;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;

import fstp.sockets.UDPConnection;
import fstp.utils.Tuple;

public class UDPHandler {
    private final UDPConnection connection;
    private final ByteArrayOutputStream buffer;
    private final DataOutputStream out;

    public UDPHandler(UDPConnection connection) {
        this.connection = connection;
        this.buffer = new ByteArrayOutputStream();
        this.out = new DataOutputStream(this.buffer);
    }

    public void requestChunk(String path, long chunkId, String addr, int port) throws IOException {
        this.out.writeByte(0x01);
        this.out.writeUTF(path);
        this.out.writeLong(chunkId);
        this.connection.send(this.buffer, addr, port);
    }

    public void sendChunk(long chunkId, byte[] chunkData, String addr, int port) throws IOException {
        this.out.writeByte(0x02);
        this.out.writeLong(chunkId);
        this.out.write(chunkData);
        this.connection.send(this.buffer, addr, port);
    }

    public void invalidChunk(long chunkId, String addr, int port) throws IOException {
        this.out.writeByte(0x02);
        this.out.writeLong(chunkId);
        this.connection.send(this.buffer, addr, port);
    }

    public UDPConnection getConnection() {
        return this.connection;
    }

    public void close() throws Exception {
        this.connection.close();
    }

    public Tuple<Tuple<String, Integer>, byte[]> receive() throws IOException {
        return this.connection.receive();
    }
}
