package fstp.node;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Interperter {
    private static final BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
    
    private NodeHandler nodeHandler;
    private NodeStatus nodeStatus;
    private boolean running = true;

    public Interperter(NodeHandler nodeHandler, NodeStatus nodeStatus) {
        this.nodeHandler = nodeHandler;
        this.nodeStatus = nodeStatus;
    }

    public void run() {
        while (running) {
            try {
                System.out.print(">> ");
                String command = stdin.readLine();
                String[] args = command.split(" ");
                switch (args[0]) {
                    case "LIST":
                        FSNode.logger.info("\n" + list());
                        break;
                    case "GET":
                        if (args.length < 2) {
                            FSNode.logger.warning("Invalid usage. Usage: GET <file_path>");
                            break;
                        }

                        get(args[1]);
                        break;
                    case "q":
                        exit();
                        break;
                    default:
                        FSNode.logger.warning("Invalid command.");
                }
            } catch (Exception e) {
                FSNode.logger.warning("Error reading command.");
            }
        }
    }

    private String list() {
        StringBuilder sb = new StringBuilder("TODO: List files");
        return sb.toString();
    }

    private void get(String file) {
    }

    private void exit() {
        nodeHandler.exit();
        running = false;
    }
}
