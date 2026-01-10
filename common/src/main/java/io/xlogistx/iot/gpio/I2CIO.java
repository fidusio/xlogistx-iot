package io.xlogistx.iot.gpio;

import org.zoxweb.server.io.InputStreamInt;
import org.zoxweb.server.io.OutputStreamInt;
import org.zoxweb.server.util.LockHolder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

;

public class I2CIO {

    class ProxyIS
            extends InputStream {

        private InputStream is;
        private InputStreamInt isi;

        ProxyIS(InputStream is) {
            this.is = is;
            isi = null;
        }

        ProxyIS(InputStreamInt is) {
            this.is = null;
            isi = is;
        }

        /**
         * Reads the next byte of data from the input stream. The value byte is
         * returned as an {@code int} in the range {@code 0} to
         * {@code 255}. If no byte is available because the end of the stream
         * has been reached, the value {@code -1} is returned. This method
         * blocks until input data is available, the end of the stream is detected,
         * or an exception is thrown.
         *
         * @return the next byte of data, or {@code -1} if the end of the
         * stream is reached.
         * @throws IOException if an I/O error occurs.
         */
        @Override
        public int read() throws IOException {
            try {
                lockHolder.lock(true);
                if (isi != null) {
                    return isi.read();
                }
                return is.read();
            } finally {
                lockHolder.unlock(true);
            }
        }

        public int read(byte[] b) throws IOException {
            try {
                lockHolder.lock(true);
                if (isi != null) {
                    return isi.read(b);
                }
                return is.read(b);
            } finally {
                lockHolder.unlock(true);
            }
        }


        public int read(byte[] b, int off, int len) throws IOException {
            try {
                lockHolder.lock(true);
                if (isi != null) {
                    return isi.read(b, off, len);
                }
                return is.read(b, off, len);
            } finally {
                lockHolder.unlock(true);
            }
        }


    }

    class ProxyOS
            extends OutputStream {

        private OutputStream os;
        private OutputStreamInt osi;

        ProxyOS(OutputStream os) {
            this.os = os;
            osi = null;
        }

        ProxyOS(OutputStreamInt osi) {
            this.os = null;
            this.osi = osi;
        }

        @Override
        public void write(int b) throws IOException {
            try {
                lockHolder.lock(true);
                if (osi != null)
                    osi.write(b);
                else
                    os.write(b);
            } finally {
                lockHolder.unlock(true);
            }
        }

        public void write(byte[] b) throws IOException {
            try {
                lockHolder.lock(true);
                if (osi != null)
                    osi.write(b);
                else
                    os.write(b);
            } finally {
                lockHolder.unlock(true);
            }
        }


        public void write(byte[] b, int off, int len) throws IOException {
            try {
                lockHolder.lock(true);
                if (osi != null)
                    osi.write(b, off, len);
                else
                    os.write(b, off, len);
            } finally {
                lockHolder.unlock(true);
            }
        }

    }

    private final int i2cAddress;
    private final LockHolder lockHolder;
    private final ProxyIS proxyIS;
    private final ProxyOS proxyOS;

    public I2CIO(int i2cAddress, LockHolder lockHolder, InputStream is, OutputStream os) throws IOException {
        this.i2cAddress = i2cAddress;
        this.lockHolder = lockHolder;
        this.proxyIS = new ProxyIS(is);
        this.proxyOS = new ProxyOS(os);
    }
    public I2CIO(int i2cAddress, LockHolder lockHolder, InputStreamInt is, OutputStreamInt os) throws IOException {
        this.i2cAddress = i2cAddress;
        this.lockHolder = lockHolder;
        this.proxyIS = new ProxyIS(is);
        this.proxyOS = new ProxyOS(os);
    }

    public int getI2CAddress() {
        return i2cAddress;
    }

    public InputStream getInputStream() {
        return proxyIS;
    }

    public OutputStream getOutputStream() {
        return proxyOS;
    }


}
