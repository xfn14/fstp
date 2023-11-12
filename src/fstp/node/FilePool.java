package fstp.node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fstp.models.FileDownload;
import fstp.sockets.UDPConnection;

public class FilePool {
    private final String path;
    private final FileDownload fileDownload;
    private final Map<String, UDPConnection> peers = new HashMap<>();

    public FilePool(String path, List<String> peers) {
        this.path = path;
        this.fileDownload = null;

        for (String peer : peers) {
            String[] parts = peer.split(":");
            try (UDPConnection connection = new UDPConnection(parts[0], Integer.parseInt(parts[1]))) {
                this.peers.put(peer, connection);
            } catch (Exception e) {
                FSNode.logger.warning("Error connecting to peer " + peer);
                e.printStackTrace();
            }
        }
    }

    public void init() {
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

    public String getPath() {
        return this.path;
    }

    public Map<String, UDPConnection> getPeers() {
        return this.peers;
    }
}
