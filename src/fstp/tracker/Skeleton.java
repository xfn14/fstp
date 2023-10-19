package fstp.tracker;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import fstp.models.FileInfo;
import fstp.sockets.TCPConnection;

public class Skeleton {
    private final TrackerStatus trackerStatus;

    public Skeleton(TrackerStatus trackerStatus) {
        this.trackerStatus = trackerStatus;
    }

    public void handle(TCPConnection c) throws IOException {
        TCPConnection.Frame frame = c.receive();
        DataInputStream buffer = new DataInputStream(new ByteArrayInputStream(frame.data));

        ByteArrayOutputStream bufferOut = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bufferOut);

        switch (frame.tag) {
            case 10:
                String str = buffer.readUTF();
                String[] files = str.split(",");
                List<FileInfo> fileInfos = new ArrayList<>();

                for (String file : files)
                    fileInfos.add(FileInfo.fromString(file));

                trackerStatus.addFiles(c.getInetAddress().getHostAddress(), fileInfos);
                
                out.writeUTF("Updated files.");
                c.send(20, bufferOut);
                break;
        }

        if (out.size() > 0)
            out.flush();
    }
}
