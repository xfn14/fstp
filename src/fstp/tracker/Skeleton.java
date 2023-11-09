package fstp.tracker;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import fstp.models.FileInfo;
import fstp.models.Frame;
import fstp.sockets.TCPConnection;

public class Skeleton {
    private final TrackerStatus trackerStatus;

    public Skeleton(TrackerStatus trackerStatus) {
        this.trackerStatus = trackerStatus;
    }

    public void handle(TCPConnection c) throws IOException {
        Frame frame = c.receive();
        DataInputStream buffer = new DataInputStream(new ByteArrayInputStream(frame.getData()));

        ByteArrayOutputStream bufferOut = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bufferOut);

        switch (frame.getTag()) {
            case 1:
                String str = buffer.readUTF();
                FileInfo fileInfo = FileInfo.fromString(str);
                int nblocks = buffer.readInt();

                if (nblocks == 0) {
                    trackerStatus.addFile(c.getDevString(), fileInfo);
                    c.send((byte) 10, bufferOut);
                    break;
                }

                List<Long> blocks = Arrays.stream(new long[nblocks])
                    .mapToObj(i -> {
                        try {
                            return buffer.readLong();
                        } catch (IOException e) {
                            e.printStackTrace();
                            return -1L;
                        }
                    })
                    .collect(Collectors.toList());

                fileInfo.setChunks(blocks);

                if (!blocks.contains(-1L)) {
                    trackerStatus.addFile(c.getDevString(), fileInfo);
                }
                    
                FSTracker.logger.info("Received file from " + c.getDevString() + "\nPayload: " + str + "\n");

                c.send((byte) (!blocks.contains(-1L) ? 10 : 40), bufferOut);
                break;
            case 2:
                Map<FileInfo, List<String>> toUpdate = trackerStatus.getUpdateList(c.getDevString());
                System.out.println(toUpdate.size() + " files to update");
                for (Entry<FileInfo, List<String>> test : toUpdate.entrySet()) {
                    System.out.println(test.getKey().toString() + " " + test.getValue().size());
                }
                if (toUpdate.size() == 0) {
                    c.send((byte) 21, bufferOut);
                    break;
                }
                
                out.writeInt(toUpdate.size());
                System.out.println(toUpdate.size() + " files to update");
                for (Entry<FileInfo, List<String>> f : toUpdate.entrySet()) {
                    out.writeUTF(f.getKey().toString());
                    System.out.println(f.getKey().toString() + " " + f.getValue().size());
                    out.writeInt(f.getValue().size());
                    for (String addr : f.getValue()) {
                        out.writeUTF(addr);
                        System.out.println(addr);
                    }
                }
                
                c.send((byte) 20, bufferOut);
                break;
        }

        if (out.size() > 0)
            out.flush();
    }
}
