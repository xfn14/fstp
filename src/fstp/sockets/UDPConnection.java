package fstp.sockets;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import fstp.Constants;

public class UDPConnection implements AutoCloseable {
    private final DatagramSocket socket;

    public UDPConnection(DatagramSocket socket) {
        this.socket = socket;
    }

    public UDPConnection(String host) throws SocketException, UnknownHostException {
        this.socket = new DatagramSocket(Constants.DEFAULT_PORT, InetAddress.getByName(host));
    }

    public UDPConnection(String host, int port) throws SocketException, UnknownHostException {
        this.socket = new DatagramSocket(port, InetAddress.getByName(host));
    }

    public void send(byte[] data) throws IOException {
        DatagramPacket packet = new DatagramPacket(data, data.length);
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
}
