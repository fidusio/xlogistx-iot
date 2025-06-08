package io.xlogistx.iot.net.util;

import org.pcap4j.core.PcapNetworkInterface;

import java.net.NetworkInterface;

public class NIPMap {
    public final NetworkInterface NI;
    public final PcapNetworkInterface PNI;

    public NIPMap(NetworkInterface ni, PcapNetworkInterface pni) {
        NI = ni;
        PNI = pni;
    }
}
