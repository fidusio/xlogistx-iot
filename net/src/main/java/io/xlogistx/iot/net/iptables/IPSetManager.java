package io.xlogistx.iot.net.iptables;

import org.zoxweb.server.util.RuntimeUtil;
import org.zoxweb.shared.util.GetName;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

/**
 * IPSetManager
 * Manages ipsets via the ipset CLI. No native bindings required.
 */
public final class IPSetManager
        implements GetName {

    public static final String IP_SET = "ipset";

    private final String name;
    private final String type;

    public IPSetManager(String setName, String setType)
            throws IOException {
        this.name = setName;
        this.type = setType;
        createSet(name, type);
    }

    public boolean add(String deviceID) throws IOException {
        if (!setContains(name, deviceID))
            return addToSet(name, deviceID);
        return false;
    }

    public boolean remove(String deviceID) throws IOException {
        return removeFromSet(name, deviceID);

    }

    public Set<String> listDeviceIDS()
            throws IOException {
        return listSet(name);
    }

    public void flush()
            throws IOException {
        flushSet(name);
    }

    public boolean contains(String deviceID)
            throws IOException {
        return setContains(name, deviceID);
    }

    public void destroy()
            throws IOException {
        destroySet(name);
    }

    @Override
    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }


    /**
     * Create a new set. Example type: "hash:mac", "hash:ip", etc.
     */
    public static boolean createSet(String setName, String setType)
            throws IOException {
        if (!doSetExist(setName))
            return RuntimeUtil.exec(IP_SET, "create", setName, setType);

        return true;
    }

    /**
     * Destroy an existing set.
     */
    public static boolean destroySet(String setName)
            throws IOException {
        return RuntimeUtil.exec(IP_SET, "destroy", setName);
    }


    public static boolean flushSet(String setName)
            throws IOException {
        return RuntimeUtil.exec(IP_SET, "flush", setName);
    }

    /**
     * Return true if the set exists (ipset list <setName> exit code 0).
     */
    public static boolean doSetExist(String setName)
            throws IOException {
        ProcessBuilder pb = new ProcessBuilder("ipset", "list", setName);
        pb.redirectErrorStream(true);
        try {
            Process p = pb.start();
            int rc = p.waitFor();
            return rc == 0;
        } catch (InterruptedException e) {
            throw new IOException("Failed to check set existence", e);
        }
    }

    /**
     * Add an element (e.g. MAC or IP) to the set.
     */
    public static boolean addToSet(String setName, String element)
            throws IOException {
        return RuntimeUtil.exec(IP_SET, "add", setName, element);
    }


    /**
     * Remove an element from the set.
     */
    public static boolean removeFromSet(String setName, String element)
            throws IOException {
        return RuntimeUtil.exec(IP_SET, "del", setName, element);
    }

    /**
     * Return true if element is in set (ipset test <set> <element>).
     * Exit code 0 = present, 1 = absent, >1 = error.
     */
    public static boolean setContains(String setName, String element)
            throws IOException {
        ProcessBuilder pb = new ProcessBuilder("ipset", "test", setName, element);
        pb.redirectErrorStream(true);
        try {
            Process p = pb.start();
            int rc = p.waitFor();
            if (rc == 0) return true;    // element present
            if (rc == 1) return false;   // element absent
            throw new IOException("ipset test error, rc=" + rc);
        } catch (InterruptedException e) {
            throw new IOException("Failed to test membership", e);
        }
    }

    /**
     * List all members of the set by parsing `ipset list <setName>` output.
     * Returns an empty set if the set does not exist or has no members.
     */
    public static Set<String> listSet(String setName)
            throws IOException {
        Set<String> members = new HashSet<>();
        if (!doSetExist(setName)) {
            return members;
        }
        ProcessBuilder pb = new ProcessBuilder("ipset", "list", setName);
        pb.redirectErrorStream(true);
        try {
            Process p = pb.start();
            try (BufferedReader rdr = new BufferedReader(
                    new InputStreamReader(p.getInputStream()))) {
                String line;
                boolean inMembers = false;
                while ((line = rdr.readLine()) != null) {
                    line = line.trim();
                    if (!inMembers) {
                        if (line.equalsIgnoreCase("Members:")) {
                            inMembers = true;
                        }
                    } else {
                        if (line.isEmpty()) {
                            break;
                        }
                        members.add(line);
                    }
                }
            }
            p.waitFor();
            return members;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to list set members", e);
        }
    }

//    /** Helper: run `ipset <args...>` and throw on non-zero exit. */
//    private static boolean exec(String... args)
//        throws IOException
//    {
//        String[] cmd = new String[args.length + 1];
//        cmd[0] = "ipset";
//        System.arraycopy(args, 0, cmd, 1, args.length);
//        ProcessBuilder pb = new ProcessBuilder(cmd);
//        pb.redirectErrorStream(true);
//        try {
//            Process p = pb.start();
//            // consume any output to avoid blocking
//            try (BufferedReader rdr = new BufferedReader(
//                    new InputStreamReader(p.getInputStream()))) {
//                while (rdr.readLine() != null) { /* discard */ }
//            }
//            int rc = p.waitFor();
//            return (rc == 0);
////            if (rc != 0) {
////                throw new IOException(
////                        "ipset " + String.join(" ", args) + " failed, rc=" + rc);
////            }
//        } catch (InterruptedException e) {
//            throw new IOException("Failed to run ipset command", e);
//        }
//
//    }


//    public static void main(String[] args) {
//
//        try {
//            RateCounter rc = new RateCounter("xec");
//            rc.start();
//
//            IPSetManager mgr = new IPSetManager("test_auth_macs", "hash:mac");
//
//            System.out.println("Created set " + mgr.getName());
//
//
//            // add a MAC
//            String mac = "18:65:90:D2:CA:65";
//            //String ip = "10.0.1.75";
//            if (!mgr.contains(mac))
//                mgr.add(mac);
//            //SINGLETON.add(SET, ip);
//
//
//            // check membership
//            System.out.println(mac + " in set? " + mgr.contains(mac));
//            mgr.add("28:65:90:D2:CA:65");
//            mgr.add("38:65:90:D2:CA:65");
//            mgr.add("48:65:90:D2:CA:65");
//
//            // list members
//            System.out.println("Members: " + mgr.listDeviceIDS());
//
//            // remove it
//            mgr.flush();
//            //SINGLETON.remove(SET, ip);
//            //System.out.println("Removed: " + mac);
//
//            // final list
//            System.out.println("Members now after flush: " + mgr.listDeviceIDS());
//
//            // destroy the set
//            mgr.destroy();
//            rc.stop();
//            System.out.println("Destroyed set " + mgr.getName());
//
//            System.out.println(Const.TimeInMillis.toString(rc.getDeltas()));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
