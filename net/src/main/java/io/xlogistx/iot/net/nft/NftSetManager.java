package io.xlogistx.iot.net.nft;

import org.zoxweb.server.io.IOUtil;

import java.io.IOException;
import java.util.regex.Pattern;

public class NftSetManager {

    private static final Pattern MAC_PATTERN =
            Pattern.compile("^([0-9a-fA-F]{2}:){5}[0-9a-fA-F]{2}$");

    private final String table;
    private final String set;

    /**
     *
     * @param table like "ip firewall"
     * @param set like "auth_macs"
     */
    public NftSetManager(String table, String set) {
        this.table = table;
        this.set = set;
    }

    public boolean addMac(String mac) throws IOException, InterruptedException {
        validateMac(mac);
        return exec("nft add element " + table + " " + set + " { " + mac + " }");
    }

    public boolean removeMac(String mac) throws IOException, InterruptedException {
        validateMac(mac);
        return exec("nft delete element " + table + " " + set + " { " + mac + " }");
    }

    private void validateMac(String mac) {
        if (!MAC_PATTERN.matcher(mac).matches()) {
            throw new IllegalArgumentException("Invalid MAC: " + mac);
        }
    }

    private boolean exec(String command) throws IOException, InterruptedException {
        Process p = new ProcessBuilder("/bin/sh", "-c", command)
                .redirectErrorStream(true)
                .start();
        int exit = p.waitFor();
        if (exit != 0) {
            String err = IOUtil.inputStreamToString(p.getInputStream(), true);
            System.err.println("nft error: " + err);
        }
        return exit == 0;
    }
}