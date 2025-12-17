package io.xlogistx.iot.ngpio.data;

import org.zoxweb.shared.util.BytesValue;
import org.zoxweb.shared.util.SharedStringUtil;

public class NCommandToBytes {
    private final byte data[];
    private final byte sep;
    private String command;

    private int size;


    public NCommandToBytes(int size, char sep) {
        this(size, (byte) sep);
    }

    public NCommandToBytes(int size, byte sep) {
        data = new byte[size];
        this.sep = (byte) sep;
        size = 0;
    }


    public NCommandToBytes() {
        this(16, ':');
    }


    public byte[] data() {
        return data;
    }

    public int size() {
        return size;
    }


    public synchronized NCommandToBytes command(String command) {
        this.command = command;
        return toBytes(command);
    }

    public String command() {
        return command;
    }

    public NCommandToBytes toBytes(String str) {
        byte[] ret = BytesValue.STRING.toBytes(str);
        addBytes(ret, 0, ret.length);
        return this;
    }

    public NCommandToBytes toBytes(byte v) {
        byte[] ret = new byte[1];
        ret[0] = v;
        addBytes(ret, 0, ret.length);
        return this;
    }

    public NCommandToBytes toBytes(short v) {
        byte[] ret = BytesValue.SHORT.toBytes(v);
        addBytes(ret, 0, ret.length);
        return this;
    }

    public NCommandToBytes toBytes(int v) {
        byte[] ret = BytesValue.INT.toBytes(v);
        addBytes(ret, 0, ret.length);
        return this;
    }

    public NCommandToBytes toBytes(long v) {
        byte[] ret = BytesValue.LONG.toBytes(v);
        addBytes(ret, 0, ret.length);
        return this;
    }

    public NCommandToBytes toBytes(float v) {
        byte[] ret = BytesValue.FLOAT.toBytes(v);
        addBytes(ret, 0, ret.length);
        return this;
    }

    public NCommandToBytes toBytes(double v) {
        byte[] ret = BytesValue.DOUBLE.toBytes(v);
        addBytes(ret, 0, ret.length);
        return this;
    }

    public String toString() {
        return SharedStringUtil.bytesToHex(data, 0, size());
    }

    public synchronized NCommandToBytes reset() {
        size = 0;
        return this;
    }

    public synchronized int addBytes(byte buf[], int offset, int len) {
        int oldSize = size;
        if (sep != 0 && size > 0)
            data[size++] = sep;

        for (int i = 0; i < len; i++) {
            data[size + i] = buf[offset + i];
        }
        size += len;

        return size - oldSize;
    }

    public synchronized int addByte(byte b) {
        int oldSize = size;
        if (sep != 0 && size > 0)
            data[size++] = sep;

        data[size++] = b;

        return size - oldSize;
    }

}
