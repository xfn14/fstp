package fstp.tracker;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import fstp.sockets.TCPConnection;

public class Skeleton {
    public void handle(TCPConnection c) throws IOException {
        TCPConnection.Frame frame = c.receive();
        DataInputStream buffer = new DataInputStream(new ByteArrayInputStream(frame.data));

        ByteArrayOutputStream bufferOut = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bufferOut);

        switch (frame.tag) {
            case 10:
                String str = buffer.readUTF();

                if (str.equalsIgnoreCase("Hello world!")) {
                    out.writeUTF("Hello client, " + c.getInetAddress().getHostAddress() + "!");
                    c.send(20, bufferOut);
                } else {
                    out.writeUTF("Invalid message.");
                    c.send(30, bufferOut);
                }
                break;
        }

        if (out.size() > 0)
            out.flush();
    }
}
