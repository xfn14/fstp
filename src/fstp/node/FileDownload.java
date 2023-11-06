package fstp.node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fstp.sockets.UDPConnection;

public class FileDownload {
    private final String path;
    private final Map<String, UDPConnection> peers = new HashMap<>();

    public FileDownload(String path, List<String> peers) {
        this.path = path;

        for (String peer : peers) {
            try (UDPConnection connection = new UDPConnection(peer)) {
                this.peers.put(peer, connection);
            } catch (Exception e) {
                FSNode.logger.warning("Error connecting to peer " + peer);
                e.printStackTrace();
            }
        }
    }

    public void download() {
        for (Map.Entry<String, UDPConnection> peer : peers.entrySet()) {
            try (UDPConnection connection = peer.getValue()) {
                connection.send(this.path.getBytes());
                byte[] data = connection.receive();
                System.out.println(new String(data));
            } catch (Exception e) {
                FSNode.logger.warning("Error connecting to peer " + peer.getKey());
                e.printStackTrace();
            }
        }
    }
}
