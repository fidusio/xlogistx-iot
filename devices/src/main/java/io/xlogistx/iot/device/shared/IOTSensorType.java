package io.xlogistx.iot.device.shared;

public enum IOTSensorType {
    I2C,
    ;



    public enum Input{
        DIGITAL,
        ANALOG,
        ADC,
        DAC,
    }

    public enum Output{
        DAC,
        PWM
    }

}
