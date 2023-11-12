package fstp.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;

import fstp.sockets.UDPConnection;

public class TestClient {
    public static void main(String[] args) {
        try {
            UDPConnection udpConnection = new UDPConnection(new DatagramSocket(35674));
            System.out.println("FS Transfer Protocol listening using UDP on " + udpConnection.getDevString());
            
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(buffer);

            while (true) {
                try {
                    out.writeInt(1);
                    out.writeUTF("Hello World");
                    udpConnection.send(buffer.toByteArray(), "localhost", 4455);
                    buffer.reset();
                    
                    byte[] data = udpConnection.receive();
                    ByteArrayInputStream buffer2 = new ByteArrayInputStream(data);
                    DataInputStream in = new DataInputStream(buffer2);

                    int tag = in.readInt();
                    String str = in.readUTF();

                    System.out.println(tag + ":" + str);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
}
