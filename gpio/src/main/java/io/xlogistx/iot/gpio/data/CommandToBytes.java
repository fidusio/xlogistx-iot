package io.xlogistx.iot.gpio.data;

import org.zoxweb.shared.util.BytesValue;

public class CommandToBytes
{
    private final byte data[];
    private final byte sep;
    private String command;

    private int size;


    public CommandToBytes(int size, char sep)
    {
       this(size, (byte)sep);
    }

    public CommandToBytes(int size, byte sep)
    {
        data = new byte[size];
        this.sep = (byte)sep;
        size = 0;
    }


    public CommandToBytes()
    {
        this(16, ':');
    }


    public byte[] data()
    {
        return data;
    }

    public int size()
    {
        return size;
    }


    public synchronized  CommandToBytes command(String command)
    {
        this.command = command;
        return toBytes(command);
    }

    public String command()
    {
        return command;
    }

    public CommandToBytes toBytes(String str)
    {
        byte[] ret = BytesValue.STRING.toBytes(str);
        addBytes(ret, 0, ret.length);
        return this;
    }

    public CommandToBytes toBytes(byte v)
    {
        byte[] ret = new byte[1];
        addBytes(ret, 0, ret.length);
        return this;
    }

    public CommandToBytes toBytes(short v)
    {
        byte[] ret = BytesValue.SHORT.toBytes(v);
        addBytes(ret, 0, ret.length);
        return this;
    }

    public CommandToBytes toBytes(int v)
    {
        byte[] ret = BytesValue.INT.toBytes(v);
        addBytes(ret, 0, ret.length);
        return this;
    }

    public CommandToBytes toBytes(long v)
    {
        byte[] ret = BytesValue.LONG.toBytes(v);
        addBytes(ret, 0, ret.length);
        return this;
    }

    public CommandToBytes toBytes(float v)
    {
        byte[] ret = BytesValue.FLOAT.toBytes(v);
        addBytes(ret, 0, ret.length);
        return this;
    }

    public CommandToBytes toBytes(double v)
    {
        byte[] ret = BytesValue.DOUBLE.toBytes(v);
        addBytes(ret, 0, ret.length);
        return this;
    }

    public synchronized CommandToBytes reset()
    {
        size = 0;
        return this;
    }

    public synchronized int addBytes(byte buf[], int offset, int len)
    {
        int oldSize = size;
        if(sep != 0 && size > 0)
            data[size++] = sep;

        for(int i = 0; i < len; i++)
        {
            data[size + i] = buf[offset + i];
        }
        size += len;

        return size - oldSize;
    }

    public synchronized int addByte(byte b)
    {
        int oldSize = size;
        if(sep != 0 && size > 0)
            data[size++] = sep;

        data[size++] = b;

        return size - oldSize;
    }

}
