package io.xlogistx.iot.net.iptables;

import org.zoxweb.shared.net.IPAddress;
import org.zoxweb.shared.util.BytesArray;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class NetFilterByAddress
        implements NetFilter<byte[]> {


    private final Map<BytesArray, Set<Integer>> inAddrMap = new LinkedHashMap<>();
    private final Map<BytesArray, Set<Integer>> outAddrMap = new LinkedHashMap<>();


    private final boolean incomingStatus;
    private final boolean outgoingStatus;
    private long incomingValidationCounter = 0;
    private long outgoingValidationCounter = 0;

    public NetFilterByAddress(boolean incomingStatus, boolean outgoingStatus) {
        this.incomingStatus = incomingStatus;
        this.outgoingStatus = outgoingStatus;
    }

    @Override
    public synchronized boolean validateIncoming(byte[] host, int port) {
        incomingValidationCounter++;
        return validate(inAddrMap, incomingStatus, host, port);
    }

    @Override
    public synchronized boolean validateOutgoing(byte[] host, int port) {
        outgoingValidationCounter++;
        return validate(outAddrMap, outgoingStatus, host, port);
    }

    private static boolean validate(Map<BytesArray, Set<Integer>> map, boolean status, byte[] host, int port) {
        Set<Integer> portValues = map.get(new BytesArray(null, host));
        if (portValues != null && (portValues.isEmpty() || portValues.contains(port)))
            return status;
        return !status;
    }

    public synchronized void addIncomingHost(String host) throws UnknownHostException {
        add(inAddrMap, host);
    }

    public synchronized void addOutgoingHost(String host) throws UnknownHostException {
        add(outAddrMap, host);
    }

    private static void add(Map<BytesArray, Set<Integer>> map, String host) throws UnknownHostException {

        IPAddress ipa = new IPAddress(host);
        InetAddress addr = InetAddress.getByName(ipa.getInetAddress());
        BytesArray ba = new BytesArray(null, addr.getAddress());

        Set<Integer> ports = map.get(ba);
        if (ports == null) {
            ports = new HashSet<>();
            if (ipa.getPort() != -1) {
                ports.add(ipa.getPort());
            }
            map.put(new BytesArray(null, addr.getAddress()), ports);
        } else if (ipa.getPort() != -1) {
            ports.add(ipa.getPort());
        }
    }

    public long incomingValidationsCount() {
        return incomingValidationCounter;
    }

    public long outgoingValidationsCount() {
        return outgoingValidationCounter;
    }
}
