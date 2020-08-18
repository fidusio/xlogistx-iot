package io.xlogistx.iot.gpio.i2c.modules;



import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.util.Console;
import io.xlogistx.iot.gpio.i2c.I2CBaseDevice;
import org.zoxweb.server.task.TaskUtil;
import org.zoxweb.shared.util.Const;
import org.zoxweb.shared.util.ParamUtil;



import java.io.IOException;

/**
 * This module is to access the ADS1115 I2C 16 bits 4 channel Analog to Digital Converter
 *
 * @see <a href="https://www.ti.com/product/ADS1115">ADS1115 TI Specs</a>
 */
public class ADS1115
   extends I2CBaseDevice
{

    public static  final String DEFAULT_NAME = "ADS1115";
    /**
     * Programmable Gain Amplifier values
     */
    public enum PGA
    {
        // 0.256 v reference
        FSR_0_256((byte)0b11111011, (float)0.256),
        // 0.512 v reference
        FSR_0_512((byte)0b11111001, (float)0.512),
        // 1.024 v reference
        FSR_1_024((byte)0b11110111, (float)1.024),
        // 2.048 v reference
        FSR_2_048((byte)0b11110101, (float)2.048),
        // 4.096 v reference
        FSR_4_096((byte)0b11110011, (float)4.096),
        // 6.144 v reference
        FSR_6_144((byte)0b11110001, (float)6.144),
        ;

        private final byte mask;
        private final float fsr;

        PGA(byte mask, float fsr)
        {
            this.mask = mask;
            this.fsr = fsr;
        }

        /**
         * Get the mask value to be ANDed
         * @return mask value
         */
        public byte getMask()
        {
            return mask;
        }

        /**
         *
         * @return fsr refer to specs
         */
        public float getFSR()
        {
            return fsr;
        }

        public static PGA match(float f)
        {
            f = Math.abs(f);
            for(PGA pga : PGA.values())
            {
                if (pga.getFSR() - f >= 0)
                    return pga;
            }

            return FSR_6_144;
        }
    }

    public enum Port
    {
        A0((byte)0b11001111),
        A1((byte)0b11011111),
        A2((byte)0b11101111),
        A3((byte)0b11111111),
        ;

        private final byte mask;
        Port(byte mask)
        {
            this.mask = mask;
        }

        public byte getMask()
        {
            return mask;
        }
    }

    /**
     *
     * @param bus i2c bus id for RPI is 1
     * @param address i2c address for ADS1115 hex values 0x48, 0x49, 0x5A, 0x5B
     * @throws IOException in case of communication failure
     * @throws I2CFactory.UnsupportedBusNumberException is I2C bus not found
     */
    public ADS1115(int bus, int address)
            throws IOException,
                   I2CFactory.UnsupportedBusNumberException
    {
        this(DEFAULT_NAME, bus, address);
    }


    public ADS1115(String name, int bus, int address)
            throws IOException,
            I2CFactory.UnsupportedBusNumberException
    {
        super(name, bus, address);
    }



    /**
     * Read the specified port value in volts
     * @param p port A0-A3
     * @param pga Programmable gain amplifier see specs
     * @param delay in millis between IC2 command sequencing
     * @return The value in volts read by the specified port
     * @throws IOException in case of communication failure
     */
    public float readPortInVolts(Port p, PGA pga, long delay) throws IOException
    {

        // Write the MSB + LSB of Config Register
        // MSB: Bits 15:8
        // Bit  15		0=No effect, 1=Begin Single Conversion (in power down mode)
        // Bits 14:12 	How to configure A0 to A3 (comparator or single ended)
        // Bits	11:9 	Programmable Gain 000=6.144v 001=4.096v 010=2.048v .... 111=0.256v
        // Bits	8 		0=Continuous conversion mode, 1=Power down single shot

        // LSB:	Bits 7:0
        // Bits 7:5	Data Rate (Samples per second) 000=8, 001=16, 010=32, 011=64,
        //			100=128, 101=250, 110=475, 111=860
        // Bit 	4 	Comparator Mode 0=Traditional, 1=Window
        // Bit	3 	Comparator Polarity 0=low, 1=high
        // Bit	2 	Latching 0=No, 1=Yes
        // Bits	1:0	Comparator # before Alert pin goes high
        //			00=1, 01=2, 10=4, 11=Disable this feature

//        if(delay < 100)
//        {
//            // based on testing 100 millis is the minimum value noticed for
//            // proper sampling
//            delay = 100;
//        }

        int msb = (0b11111110 & p.getMask() & pga.getMask());

        byte[] config = {/*MSB*/(byte)msb, /*LSB*/(byte)0x83};

        getI2CDevice().write(0x01, config, 0, 2);
        TaskUtil.sleep(delay);

        byte[] data = new byte[2];
        getI2CDevice().read(0x00, data, 0, 2);

        // Convert the data
        return convert(data, pga.getFSR());

    }

    @Override
    public void close()  {
        // no op
    }

    public static float convert(byte[] data, float maxValue)
    {
        int raw_adc = ((data[0] & 0xFF) * 256) + (data[1] & 0xFF);
        if (raw_adc > 32767)
        {
            raw_adc -= 65535;
        }
        return (((float)raw_adc)*maxValue)/32767;
    }


    public static void main(String[] args) {

        try {
            // create Pi4J console wrapper/helper
            // (This is a utility class to abstract some of the boilerplate code)
            final Console console = new Console();

            // print program title/header
            console.title("<-- The Pi4J Project -->", "I2C ADS1115 Example");


            // allow for user to exit program using CTRL-C
            console.promptForExit();


            ParamUtil.ParamMap params = ParamUtil.parse("-", args);



            int bus = params.intValue("-b");
            int devAddress = params.hexValue("-a");
            float maxVoltage = params.floatValue("-v");
            ADS1115.Port p = params.parameterExists("-p") ? params.enumValue("-p", ADS1115.Port.values()) : null ;
            long delay = 100;
            if(params.parameterExists("-d"))
            {
                delay = Const.TimeInMillis.toMillis(params.stringValue("-d"));
            }
            console.println("device address:" + Integer.toHexString(devAddress));

            ADS1115.PGA pga = ADS1115.PGA.match(maxVoltage);




            ADS1115 ads1115 = new ADS1115(bus, devAddress);
            console.println("Bus: " + ads1115.getI2CBus().getBusNumber() + " Address: " + Integer.toHexString(ads1115.getI2CDevice().getAddress()) +
                    " PGA: " + pga.name() + " Port: " + p + " delay: " + Const.TimeInMillis.toString(delay));

            console.println("... reading ID register from ADS1115");

            if (p == null) {
                for (ADS1115.Port p1 : ADS1115.Port.values()) {
                    System.out.println("Port:" + p1.name() + " volts:" + ads1115.readPortInVolts(p1, pga, delay));
                }
            } else {
                System.out.println("Port:" + p.name() + " volts:" + ads1115.readPortInVolts(p, pga, delay));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();

            System.err.println("Usage: ADS1115 -b [i2c bus id or number] -a [i2c address in hex] -v [max voltage 3.3,5 ] [-p  port a0-a3] [-d delay] ");
        }
    }
}
