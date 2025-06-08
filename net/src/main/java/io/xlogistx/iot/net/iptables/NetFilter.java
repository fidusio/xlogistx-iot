package io.xlogistx.iot.net.iptables;

public interface NetFilter<V> {
    boolean validateIncoming(V host, int port);

    boolean validateOutgoing(V host, int port);
}
