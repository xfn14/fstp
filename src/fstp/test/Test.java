package fstp.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Date;

import fstp.Constants;
import fstp.sockets.UDPConnection;

public class Test {
    public static void main(String[] args) {
        try {
            Date date = new Date();
            UDPConnection udpConnection = new UDPConnection(new DatagramSocket(4455));
            while (true) {
                try {
                    byte[] data = udpConnection.receive();
                    ByteArrayInputStream buffer = new ByteArrayInputStream(data);
                    DataInputStream in = new DataInputStream(buffer);

                    ByteArrayOutputStream buffer2 = new ByteArrayOutputStream();
                    DataOutputStream out = new DataOutputStream(buffer2);

                    int tag = in.readInt();
                    String l = in.readUTF();
                    String boas = "Boas";
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
