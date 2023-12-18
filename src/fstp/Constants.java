package fstp;

import java.util.HashMap;
import java.util.Map;

public class Constants {
    public static boolean DEDUG = false;
    public static boolean DEBUG_TRAFFIC = true;
    public static boolean DEBUG_UPDATE_LIST = true;
    public static boolean DNS_SYSTEM = true;

    public static final int DEFAULT_PORT = 9090;
    public static final int UDP_BUFFER_SIZE = 4096;
    public static final long CHUNK_TIMEOUT = 10 * 1000; // Time in milliseconds

    public static Map<String, String> dns = new HashMap<>();

    public static void initDns() {
        dns.put("Portatil1", "10.1.1.1");
        dns.put("Portatil2", "10.1.1.2");
        dns.put("PC1", "10.2.2.1");
        dns.put("PC2", "10.2.2.2");
        dns.put("Roma", "10.3.3.1");
        dns.put("Paris", "10.3.3.2");
        dns.put("Servidor1", "10.4.4.1");
        dns.put("Servidor2", "10.4.4.1");
    }

    public static String getDns(String service) {
        if (!DNS_SYSTEM) return service;
        return dns.get(service);
    }

    public static String getNameByIp(String ip) {
        if (!DNS_SYSTEM) return ip;
        for (String name : Constants.dns.keySet())
            if (Constants.dns.get(name).equals(ip)) return name;
        return null;
    }
}
