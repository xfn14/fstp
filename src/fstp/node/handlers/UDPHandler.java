package fstp.node.handlers;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import fstp.sockets.UDPConnection;

public class UDPHandler {
    private final UDPConnection connection;
    private final ByteArrayOutputStream buffer;
    private final DataOutputStream out;

    public UDPHandler(UDPConnection connection) {
        this.connection = connection;
        this.buffer = new ByteArrayOutputStream();
        this.out = new DataOutputStream(this.buffer);
    }

    public UDPConnection getConnection() {
        return this.connection;
    }

    public void close() throws Exception {
        this.connection.close();
    }

    public byte[] receive() throws IOException {
        return this.connection.receive();
    }
}
