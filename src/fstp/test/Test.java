package fstp.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;

import fstp.Constants;
import fstp.sockets.UDPConnection;

public class Test {
    public static void main(String[] args) {
        try {
            UDPConnection udpConnection = new UDPConnection(new DatagramSocket(4455));
            while (true) {
                try {
                    byte[] data = udpConnection.receive();
                    ByteArrayInputStream buffer = new ByteArrayInputStream(data);
                    DataInputStream in = new DataInputStream(buffer);

                    ByteArrayOutputStream buffer2 = new ByteArrayOutputStream();
                    DataOutputStream out = new DataOutputStream(buffer2);


                    int tag = in.readInt();
                    String str = in.readUTF();

                    System.out.println(tag + ":" + str);
                    out.writeInt(2);
                    out.writeUTF("Goodbye World");
                    udpConnection.send(buffer2.toByteArray(), "localhost", 35674);
                    buffer2.reset();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
}
