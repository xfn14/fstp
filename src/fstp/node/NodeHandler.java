package fstp.node;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.SocketException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import fstp.models.FileInfo;
import fstp.node.handlers.TCPHandler;
import fstp.node.handlers.UDPHandler;
import fstp.utils.FileUtils;
import fstp.utils.Tuple;

public class NodeHandler {
    private final NodeStatus nodeStatus;
    private final TCPHandler tcpHandler;
    private final UDPHandler udpHandler;

    public NodeHandler(NodeStatus nodeStatus, TCPHandler tcpHandler, UDPHandler udpHandler) {
        this.nodeStatus = nodeStatus;
        this.tcpHandler = tcpHandler;
        this.udpHandler = udpHandler;
    }

    public Runnable initTCPListener() {
        return () -> {
            FSNode.logger.info("FS Track Protocol connected to Tracker on " + tcpHandler.getDevString());
            boolean res = this.tcpHandler.registerFiles(this.nodeStatus.getFileInfos().values().stream().collect(Collectors.toList()));
            if (!res) {
                FSNode.logger.severe("Error registering some files to Tracker.");
                return;
            }
        
            Map<FileInfo, List<String>> response = this.tcpHandler.getUpdateList();
            this.nodeStatus.setUpdateMap(response);
        
            Interperter interperter = new Interperter(this.tcpHandler, this.nodeStatus);
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

    public Runnable initUDPListener() {
        return () -> {
            FSNode.logger.info("FS Transfer Protocol listening using UDP on " + udpHandler.getConnection().getDevString());

            while (this.nodeStatus.isRunning()) {
                try {
                    Tuple<Tuple<String, Integer>, byte[]> data = this.udpHandler.receive();
                    if (data == null || data.getY() == null) {
                        FSNode.logger.severe("Error receiving data from UDP connection.");
                        continue;
                    }

                    DataInputStream in = new DataInputStream(new ByteArrayInputStream(data.getY()));
                    Tuple<String, Integer> addr = data.getX();
                    byte tag = in.readByte();
                    switch (tag) {
                        case 1:
                            String path = in.readUTF();
                            long chunk = in.readLong();

                            FileInfo fileInfo = this.nodeStatus.getFileInfo(path);
                            if (fileInfo == null || !fileInfo.getChunks().contains(chunk)) {
                                this.udpHandler.invalidChunk(chunk, addr.getX(), addr.getY());
                                break;
                            }
                            
                            int chunkPos = fileInfo.getChunkIndex(chunk);
                            try {
                                byte[] chunkData = this.nodeStatus.getChunkData(path, chunkPos);
                                this.udpHandler.sendChunk(chunk, chunkData, addr.getX(), addr.getY());
                                FSNode.logger.info("Sending chunk " + chunk + " to " + addr.getX() + ":" + addr.getY());
                            } catch (Exception e) {
                                this.udpHandler.invalidChunk(chunk, addr.getX(), addr.getY());
                                FSNode.logger.warning("Error in local chunk in file system on " + path + " chunk " + chunk + ".");
                            }
                            break;
                        case 2:
                            if (this.nodeStatus.getDownloading() == null) break;

                            long chunkId = in.readLong();
                            byte[] chunkData = in.readAllBytes();

                            long checksum = FileUtils.checksumByteArr(chunkData);
                            if (chunkId != checksum) {
                                FSNode.logger.warning("Invalid checksum for chunk " + chunkId + ".\nExpected " + chunkId + " but got " + checksum + ".");
                                break;
                            }

                            this.nodeStatus.addChunkToDownload(chunkId, chunkData);
                            FSNode.logger.info("Received chunk " + chunkId + " from " + addr.getX() + ":" + addr.getY());
                            break;
                        default:
                            break;
                    }
                } catch (SocketException e) {
                    FSNode.logger.info("FS Transfer Protocol stopped listenning.");
                    continue;
                } catch (Exception e) {
                    FSNode.logger.severe("Error handling UDP connection.");
                    e.printStackTrace();
                }
            }

            try {
                this.udpHandler.close();
            } catch (Exception e) {
                FSNode.logger.severe("Error closing UDP connection.");
            }
        };
    }

    public Runnable initDownloadHandler() {
        return () -> {
            while (this.nodeStatus.isRunning()) {
                if (this.nodeStatus.getDownloading() == null) continue;

                FilePool filePool = this.nodeStatus.getDownloading();
                for (String peer : filePool.getPeers()) {
                    String[] addr = peer.split(":");
                    long chunkToRequest = filePool.getNextChunkToRequest();
                    if (chunkToRequest == -1L) {
                        FileInfo newFileInfo = this.nodeStatus.saveFile(filePool.getFileDownload());
                        if (newFileInfo == null) {
                            FSNode.logger.warning("Error saving file " + filePool.getPath());
                            continue;
                        }

                        this.tcpHandler.registerFile(newFileInfo);
                        this.nodeStatus.setDownloading(null);
                        continue;
                    }

                    try {
                        this.udpHandler.requestChunk(filePool.getPath(), chunkToRequest, addr[0], 9092); // Integer.parseInt(addr[1])
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                filePool.nextIteration();
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
