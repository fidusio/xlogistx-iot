package io.xlogistx.iot.gpio.data;

public class I2CResp {
    public final int bus;
    public final int address;
    public final byte[] data;
    public final String command;


    private I2CResp(int bus, int address, byte[] data, String command)

    {
        this.bus = bus;
        this.address = address;
        this.data = data;
        this.command = command;
    }

    public static I2CResp build(int bus, int address, byte[] data, String command)
    {
        return new I2CResp(bus, address, data, command);
    }


}
