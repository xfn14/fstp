package fstp;

import java.util.HashMap;
import java.util.Map;

public class Constants {

    public static final boolean DEDUG = false;
    public static final boolean DEBUG_TRAFFIC = true;
    public static final boolean DEBUG_UPDATE_LIST = true;

    public static final int DEFAULT_PORT = 9090;
    public static final int UDP_BUFFER_SIZE = 4096;
    public static final long CHUNK_TIMEOUT = 10 * 1000; // Time in milliseconds

    public static Map<String, String> getDns() {
        Map<String, String> dns = new HashMap<>();
        dns.put("Portatil1", "10.1.1.1");
        dns.put("Portatil2", "10.1.1.2");
        dns.put("PC1", "10.2.2.1");
        dns.put("PC2", "10.2.2.2");
        dns.put("Roma", "10.3.3.1");
        dns.put("Paris", "10.3.3.2");
        dns.put("Servidor1", "10.4.4.1");
        dns.put("Servidor2", "10.4.4.1");
        return dns;
    }

    public static String getNameByIp(String ip) {
        for (String name : Constants.getDns().keySet())
            if (Constants.getDns().get(name).equals(ip)) return name;
        return null;
    }
}
