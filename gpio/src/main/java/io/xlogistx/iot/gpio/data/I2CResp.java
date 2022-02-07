package io.xlogistx.iot.gpio.data;

public class I2CResp {
    public final int bus;
    public final int address;
    public final byte[] data;


    private I2CResp(int bus, int address, byte[] data)
    {
        this.bus = bus;
        this.address = address;
        this.data = data;
    }

    public static I2CResp build(int bus, int address, byte[] data)
    {
        return new I2CResp(bus, address, data);
    }


}
