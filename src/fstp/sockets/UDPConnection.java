package fstp.sockets;

import java.net.DatagramSocket;

public class UDPConnection {
    private final DatagramSocket socket;

    public UDPConnection(DatagramSocket socket) {
        this.socket = socket;
    }

    public void send(byte[] data) {
        
    }
}
