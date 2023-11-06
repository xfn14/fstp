package fstp.sockets;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class TCPConnection implements AutoCloseable {
    private final Socket socket;

    private final DataOutputStream out;
    private final DataInputStream in;

    public TCPConnection(Socket socket) throws IOException {
        this.socket = socket;
        this.out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        this.in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
    }

    public void send(Frame frame) throws IOException {
        this.send(frame.tag, frame.data);
    }

    public void send(int tag, ByteArrayOutputStream byteArray) throws IOException {
        this.send(tag, byteArray.toByteArray());
        byteArray.reset();
    }

    public void send(int tag, byte[] data) throws IOException {
        this.rawSend(tag, data);
    }

    public void rawSend(int tag, byte[] data) throws IOException {
        this.out.writeInt(4 + data.length);
        this.out.writeInt(tag);
        this.out.write(data);
        this.out.flush();
    }

    public Frame receive() throws IOException {
        int size = this.in.readInt();
        byte[] data = new byte[size - 4];
        int tag = this.in.readInt();
        this.in.readFully(data);

        return new Frame(tag, data);
    }

    @Override
    public void close() throws IOException {
        this.socket.close();
    }

    public InetAddress getInetAddress() {
        return this.socket.getInetAddress();
    }

    public String getAddress() {
        return this.getInetAddress().getHostAddress();
    }

    public static class Frame {
        public final int tag;
        public final byte[] data;

        public Frame(int tag, byte[] data) {
            this.tag = tag;
            this.data = data;
        }
    }
}
