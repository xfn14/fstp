package fstp.tests;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;

import fstp.models.sockets.UDPConnection;
import fstp.utils.Tuple;

public class TestServer {
    public static void main(String[] args) {
        try {
            UDPConnection udpConnection = new UDPConnection(new DatagramSocket(4455));
            while (true) {
                try {
                    Tuple<Tuple<String, Integer>, byte[]> data = udpConnection.receive();
                    ByteArrayInputStream buffer = new ByteArrayInputStream(data.getY());
                    DataInputStream in = new DataInputStream(buffer);

                    ByteArrayOutputStream buffer2 = new ByteArrayOutputStream();
                    DataOutputStream out = new DataOutputStream(buffer2);

                    int tag = in.readInt();
                    String l = in.readUTF();
                    byte[] bytes = new byte[buffer.available()];
                    in.read(bytes);

                    System.out.println("--------------------");
                    System.out.println(tag + ":" + l + ":" + bytes.length + ":" + new String(bytes));
                    for (int i = 0; i < bytes.length; i++)
                        System.out.print(bytes[i] + " ");
                    System.out.println("--------------------");
                    
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
