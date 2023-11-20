package fstp.sockets;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import fstp.Constants;
import fstp.models.Frame;

public class TCPConnection {
    private final Socket socket;

    private final DataOutputStream out;
    private final DataInputStream in;

    public TCPConnection(Socket socket) throws IOException {
        this.socket = socket;
        this.out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        this.in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
    }

    public void send(Frame frame) throws IOException {
        this.send(frame.getTag(), frame.getData());
    }

    public void send(int tag, ByteArrayOutputStream byteArray) throws IOException {
        this.send(tag, byteArray.toByteArray());
        byteArray.reset();
    }

    public void send(int tag, byte[] data) throws IOException {
        this.rawSend(tag, data);
    }

    public void rawSend(int tag, byte[] data) throws IOException {
        if (Constants.DEDUG) {
            System.out.println("Sent " + data.length + " bytes to " + this.getDevString());
            System.out.println("Tag: " + tag);
            System.out.println("Data: " + new String(data));
        }

        this.out.writeInt(4 + data.length);
        this.out.writeByte((byte) tag);
        this.out.write(data);
        this.out.flush();
    }

    public Frame receive() throws IOException {
        int size = this.in.readInt();
        byte[] data = new byte[size - 4];
        byte tag = this.in.readByte();
        this.in.readFully(data);

        if (Constants.DEDUG) {
            System.out.println("Received " + size + " bytes from " + this.getDevString());
            System.out.println("Tag: " + tag);
            System.out.println("Data: " + new String(data));
        }

        return new Frame(tag, data);
    }

    public void close() throws IOException {
        this.socket.close();
    }

    public InetAddress getInetAddress() {
        return this.socket.getInetAddress();
    }

    public String getAddress() {
        return this.getInetAddress().getHostAddress();
    }

    public int getPort() {
        return this.socket.getPort();
    }

    public String getDevString() {
        return this.getAddress() + ":" + this.getPort();
    }
}
