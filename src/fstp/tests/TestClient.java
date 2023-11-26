package fstp.tests;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;

import fstp.Constants;
import fstp.sockets.UDPConnection;
import fstp.utils.Tuple;

public class TestClient {
    public static void main(String[] args) {
        try {
            UDPConnection udpConnection = new UDPConnection(new DatagramSocket(35674));
            System.out.println("FS Transfer Protocol listening using UDP on " + udpConnection.getDevString());
            
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(buffer);

            while (true) {
                try {
                    out.writeByte(1);
                    out.writeLong(523456837455304L);
                    byte[] bytes = new byte[Constants.UDP_BUFFER_SIZE - buffer.size()];
                    for (int i = 0; i < bytes.length; i++) bytes[i] = (byte) i;
                    System.out.println("--------------------");
                    for (int i = 0; i < bytes.length; i++)
                        System.out.print(bytes[i] + " ");
                    System.out.println("--------------------");
                    out.write(bytes);
                    udpConnection.send(buffer.toByteArray(), "localhost", 4455);
                    buffer.reset();
                    
                    Tuple<Tuple<String, Integer>, byte[]> data = udpConnection.receive();
                    ByteArrayInputStream buffer2 = new ByteArrayInputStream(data.getY());
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
