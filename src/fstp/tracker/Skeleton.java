package fstp.tracker;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
                List<FileInfo> fileInfos = Arrays.stream(files)
                    .map(FileInfo::fromString)
                    .collect(Collectors.toList());

                trackerStatus.addFiles(c.getAddress(), fileInfos);
                
                String res = trackerStatus.getFiles().keySet().stream()
                    .collect(Collectors.joining(","));
                out.writeUTF(res);
                c.send(10, bufferOut);
                break;
            case 11:
                String fileToGet = buffer.readUTF();
                List<String> peer = trackerStatus.getFilePeers(fileToGet);
                if (peer == null) {
                    out.writeUTF("Error");
                    c.send(41, bufferOut);
                    break;
                }

                if (peer.size() == 0 || (peer.size() == 1 && peer.get(0).equals(c.getAddress()))) {
                    out.writeUTF("No peers");
                    c.send(41, bufferOut);
                    break;
                }

                String r = peer.stream()
                    .collect(Collectors.joining(","));
                out.writeUTF(r);
                c.send(11, bufferOut);
                break;
            case 20:
                str = buffer.readUTF();
                if (!str.equals("LIST")) break;

                Map<FileInfo, List<String>> updateList = trackerStatus.getUpdateList(c.getAddress());
                String response = updateList.entrySet().stream()
                    .map(entry -> {
                        FileInfo fileInfo = entry.getKey();
                        List<String> peers = entry.getValue();
                        return String.format("%s*%d*%s^%s", fileInfo.getPath(), fileInfo.getChecksum(), fileInfo.getLastModified(), String.join("~", peers));
                    })
                    .collect(Collectors.joining(","));

                out.writeUTF(response);
                c.send(20, bufferOut);
                break;
            case 40:
                str = buffer.readUTF();
                if (!str.equals("Bye world!")) break;

                trackerStatus.removeFiles(c.getAddress());
                break;
        }

        if (out.size() > 0)
            out.flush();
    }
}
