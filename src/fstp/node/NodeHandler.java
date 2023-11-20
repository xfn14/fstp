package fstp.node;

import java.net.SocketException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import fstp.models.FileInfo;
import fstp.node.handlers.TCPHandler;
import fstp.node.handlers.UDPHandler;

public class NodeHandler {
    private final NodeStatus nodeStatus;
    private final TCPHandler tcpHandler;
    private final UDPHandler udpHandler;

    public NodeHandler(NodeStatus nodeStatus, TCPHandler tcpHandler, UDPHandler udpHandler) {
        this.nodeStatus = nodeStatus;
        this.tcpHandler = tcpHandler;
        this.udpHandler = udpHandler;
    }

    public Runnable initTCP() {
        return () -> {
            FSNode.logger.info("FS Track Protocol connected to Tracker on " + tcpHandler.getDevString());
            boolean res = this.tcpHandler.registerFiles(nodeStatus.getFileInfos().values().stream().collect(Collectors.toList()));
            if (!res) {
                FSNode.logger.severe("Error registering some files to Tracker.");
                return;
            }
        
            Map<FileInfo, List<String>> response = this.tcpHandler.getUpdateList();
            nodeStatus.setUpdateMap(response);
        
            Interperter interperter = new Interperter(this.tcpHandler, nodeStatus);
            interperter.run();

            try {
                this.tcpHandler.close();
                this.udpHandler.close();
                FSNode.logger.info("FS Track Protocol disconnected from Tracker.");
            } catch (Exception e) {
                FSNode.logger.severe("Error closing TCP connection.");
            }
        };
    }

    public Runnable initUDP() {
        return () -> {
            FSNode.logger.info("FS Transfer Protocol listening using UDP on " + udpHandler.getConnection().getDevString());

            while (this.nodeStatus.isRunning()) {
                try {
                    System.out.println("Waiting for data...");
                    byte[] data = this.udpHandler.receive();
                    System.out.println("Received data.");
                    if (data == null) {
                        FSNode.logger.severe("Error receiving data from UDP connection.");
                        continue;
                    }
                } catch (SocketException e) {
                    FSNode.logger.info("FS Transfer Protocol stopped listenning.");
                    continue;
                } catch (Exception e) {
                    FSNode.logger.severe("Error handling UDP connection.");
                }
            }

            try {
                this.udpHandler.close();
            } catch (Exception e) {
                FSNode.logger.severe("Error closing UDP connection.");
            }
        };
    }

    public NodeStatus getNodeStatus() {
        return this.nodeStatus;
    }

    public TCPHandler getTCPHandler() {
        return this.tcpHandler;
    }

    public UDPHandler getUDPHandler() {
        return this.udpHandler;
    }

    public void closeConnections() throws Exception {
        this.tcpHandler.close();
        this.udpHandler.close();
    }
}
