package fstp.models.sockets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import fstp.Constants;
import fstp.utils.Tuple;

public class UDPConnection {
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
        byte[] buffer = new byte[Constants.UDP_BUFFER_SIZE];
        System.arraycopy(data, 0, buffer, 0, data.length);
        DatagramPacket packet = new DatagramPacket(buffer, Constants.UDP_BUFFER_SIZE, InetAddress.getByName(addr), port);
        this.socket.send(packet);
    }

    public Tuple<Tuple<String, Integer>, byte[]> receive() throws IOException {
        byte[] buffer = new byte[Constants.UDP_BUFFER_SIZE];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        this.socket.receive(packet);
        return new Tuple<>(
            new Tuple<>(
                packet.getAddress().getHostAddress(),
                packet.getPort()
            ),
            packet.getData()
        );
    }

    public void close() throws Exception {
        this.socket.close();
    }

    public String getDevString() {
        return Constants.getNameByIp(this.socket.getLocalAddress().getHostAddress());
        // return this.socket.getLocalAddress().getHostAddress() + ":" + this.socket.getLocalPort();
    }

    public int getPort() {
        return this.port;
    }
}
