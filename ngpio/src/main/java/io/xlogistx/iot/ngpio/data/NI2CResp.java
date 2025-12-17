package io.xlogistx.iot.ngpio.data;

public class NI2CResp {
    public final int bus;
    public final int address;
    public final byte[] data;
    public final String command;


    private NI2CResp(int bus, int address, byte[] data, String command) {
        this.bus = bus;
        this.address = address;
        this.data = data;
        this.command = command;
    }

    public static NI2CResp build(int bus, int address, byte[] data, String command) {
        return new NI2CResp(bus, address, data, command);
    }


}
