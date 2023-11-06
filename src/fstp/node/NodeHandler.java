package fstp.node;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import fstp.models.FileInfo;
import fstp.sockets.TCPConnection;
import fstp.sockets.TCPConnection.Frame;
import fstp.utils.Tuple;

public class NodeHandler {
    private final TCPConnection connection;
    private final ByteArrayOutputStream buffer;
    private final DataOutputStream out;
    
    public NodeHandler(TCPConnection connection) {
        this.connection = connection;
        this.buffer = new ByteArrayOutputStream();
        this.out = new DataOutputStream(this.buffer);
    }

    public String ping(List<FileInfo> files) {
        try {
            StringBuilder sb = new StringBuilder();
            if (files.size() > 0) {
                for (FileInfo file : files)
                    sb.append(file.toString()).append(",");
                sb.deleteCharAt(sb.length() - 1);
            }

            this.out.writeUTF(sb.toString());
            this.connection.send(10, this.buffer);
            FSNode.logger.info("Sent ping to tracker...\nPayload: " + sb.toString() + "\n");

            Frame response = this.connection.receive();
            if (response.tag != 10) return "Error";
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(response.data));
            
            return in.readUTF();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "Error";
    }

    public Map<String, List<String>> list() {
        try {
            this.out.writeUTF("LIST");
            this.connection.send(20, this.buffer);

            Frame packet = this.connection.receive();
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(packet.data));
            String response = in.readUTF();

            if (response.equals("")) return new HashMap<>();
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

    /**
     * GET <path_file1> <path_file2> ...
     * 
     * @param payload <path_file1>,<path_file2>,...
     */
    public Tuple<Integer, List<String>> get(String payload) {
        List<String> peers = new ArrayList<>();
        
        try {
            this.out.writeUTF(payload);
            this.connection.send(11, this.buffer);

            Frame packet = this.connection.receive();
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(packet.data));
            String response = in.readUTF();
            
            if (peers.contains(",")) 
                peers = Arrays.asList(response.split(","));
            else peers.add(response);
            return new Tuple<>(packet.tag, peers);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new Tuple<>(41, peers);
    }

    public void exit() {
        try {
            this.out.writeUTF("Bye world!");
            this.connection.send(40, this.buffer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
