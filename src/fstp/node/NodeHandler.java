package fstp.node;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import fstp.sockets.TCPConnection;
import fstp.sockets.TCPConnection.Frame;
import fstp.utils.FileUtils;

public class NodeHandler {
    private final String path;
    private final TCPConnection connection;
    private final ByteArrayOutputStream buffer;
    private final DataOutputStream out;
    
    public NodeHandler(String path, TCPConnection connection) {
        this.path = path;
        this.connection = connection;
        this.buffer = new ByteArrayOutputStream();
        this.out = new DataOutputStream(this.buffer);
    }

    public String ping(List<File> files) {
        try {
            this.out.writeUTF(FileUtils.filesToString(path, files));

            this.connection.send(10, this.buffer);

            Frame response = this.connection.receive();
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(response.data));
            
            return in.readUTF();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "Hello world!";
    }

    public Map<String, List<String>> list() {
        try {
            this.out.writeUTF("LIST");
            this.connection.send(20, this.buffer);

            Frame packet = this.connection.receive();
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(packet.data));
            String response = in.readUTF();

            Map<String, List<String>> res = Arrays.stream(response.split(","))
                .map(file -> file.split("\\^"))
                .collect(Collectors.toMap(
                    parts -> parts[0],
                    parts -> Arrays.asList(parts[1].split("~"))
                ));

            return res;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new HashMap<>();
    }
}
