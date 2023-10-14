package fstp.node;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Logger;

import fstp.Constants;
import fstp.handlers.LoggerHandler;
import fstp.sockets.TCPConnection;

public class FSNode {
    public static Logger logger = Logger.getLogger("FS-Tracker");
    private static final BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
    
    public static void main(String[] args) {
        LoggerHandler.loadLoggerSettings(logger, true);

        if (args.length < 2 || args.length > 3) {
            logger.severe("Invalid number of arguments. Usage: java FSNode <folder_path> <tracker_ip> [tracker_port]");
            return;
        }

        String path = args[0];
        String ip = args[1];
        int port = args.length == 3 ? Integer.parseInt(args[2]) : Constants.DEFAULT_PORT;

        Runnable tcpRunnable = () -> {
            try (Socket socket = new Socket(ip, port)) {
                TCPConnection tcpConnection = new TCPConnection(socket);
                NodeHandler nodeHandler = new NodeHandler(tcpConnection);
                logger.info("Conexão FS Track Protocol com servidor " + ip + " porta " + port);

                String message = stdin.readLine();
                while (!message.equals("exit")) {
                    String response = nodeHandler.hello(message);
                    logger.info("Resposta do servidor: " + response);
                    message = stdin.readLine();
                }

                tcpConnection.close();
            } catch (UnknownHostException e) {
                logger.severe(ip + " is not a valid IP address.");
                e.printStackTrace();
            } catch (IOException e) {
                logger.severe("Error connecting to " + ip + " on port " + port);
                e.printStackTrace();
            }
        };
        tcpRunnable.run();

        Runnable udpRunnable = () -> {
            // logger.info("FS Transfer Protocol à escuta na porta UDP " + port);
        };
        udpRunnable.run();
    }
}
