package fstp.node;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import fstp.sockets.TCPConnection;
import fstp.sockets.TCPConnection.Frame;

public class NodeHandler {
    private final TCPConnection connection;
    private final ByteArrayOutputStream buffer;
    private final DataOutputStream out;
    
    public NodeHandler(TCPConnection connection) {
        this.connection = connection;
        this.buffer = new ByteArrayOutputStream();
        this.out = new DataOutputStream(this.buffer);
    }

    public String hello(String str) {
        try {
            this.out.writeUTF(str);

            this.out.flush();
            this.connection.send(10, this.buffer);

            Frame response = this.connection.receive();
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(response.data));
            return in.readUTF();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "Hello world!";
    }
}
