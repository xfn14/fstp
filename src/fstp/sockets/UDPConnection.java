package fstp.sockets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import fstp.Constants;

public class UDPConnection implements AutoCloseable {
    private final DatagramSocket socket;
    private final int port;

    public UDPConnection(DatagramSocket socket) {
        this.port = socket.getPort();
        this.socket = socket;
    }

    public UDPConnection(String host) throws SocketException, UnknownHostException {
        this.port = Constants.DEFAULT_PORT;
        this.socket = new DatagramSocket(Constants.DEFAULT_PORT, InetAddress.getByName(host));
    }

    public UDPConnection(int port) throws SocketException, UnknownHostException {
        this.port = port;
        this.socket = new DatagramSocket(port);
    }

    public void send(ByteArrayOutputStream byteArray, String addr, int port) throws IOException {
        this.send(byteArray.toByteArray(), addr, port);
        byteArray.reset();
    }
    
    public void send(byte[] data, String addr, int port) throws IOException {
        DatagramPacket packet = new DatagramPacket(data, Constants.UDP_BUFFER_SIZE, InetAddress.getByName(addr), port);
        this.socket.send(packet);
    }

    public void send(ByteArrayOutputStream byteArray, String addr) throws IOException {
        this.send(byteArray.toByteArray(), addr, Constants.DEFAULT_PORT);
        byteArray.reset();
    }
    
    public void send(byte[] data, String addr) throws IOException {
        DatagramPacket packet = new DatagramPacket(data, Constants.UDP_BUFFER_SIZE, InetAddress.getByName(addr), Constants.DEFAULT_PORT);
        this.socket.send(packet);
    }

    public byte[] receive() throws IOException {
        byte[] buffer = new byte[Constants.UDP_BUFFER_SIZE];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        this.socket.receive(packet);
        return packet.getData();
    }

    @Override
    public void close() throws Exception {
        this.socket.close();
    }

    public String getDevString() {
        return this.socket.getLocalAddress().getHostAddress() + ":" + this.socket.getLocalPort();
    }

    public int getPort() {
        return this.port;
    }
}
