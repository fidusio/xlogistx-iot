package io.xlogistx.iot.ngpio.data;

import com.google.gson.annotations.SerializedName;
import com.pi4j.io.gpio.digital.DigitalState;
import io.xlogistx.iot.ngpio.NGPIOPin;
import org.zoxweb.shared.util.Const;

public class NPWMConfig {


    private NGPIOPin[] gpios;
    private float frequency;
    @SerializedName("duty_cycle")
    private float dutyCycle;
    private String duration;
    @SerializedName("last_state")
    private DigitalState lastState;


    public float getFrequency() {
        return frequency;
    }

    public void setFrequency(float frequency) {
        if (frequency <= 0) {
            throw new IllegalArgumentException("Invalid frequency " + frequency);
        }
        this.frequency = frequency;
    }

    public float getDutyCycle() {
        return dutyCycle;
    }

    public void setDutyCycle(float dutyCycle) {
        if (dutyCycle < 0 || dutyCycle > 100) {
            throw new IllegalArgumentException("Invalid duty cycle " + dutyCycle);
        }
        this.dutyCycle = dutyCycle;
    }

    public long getDuration() {

        return Const.TimeInMillis.toMillis(duration);
    }

    public void setDuration(long duration) {

        this.duration = "" + duration;
    }

    public void setDuration(String duration) {

        this.duration = duration;
    }

    public DigitalState getLastState() {
        return lastState;
    }

    public void setLastState(DigitalState lastState) {
        this.lastState = lastState;
    }

    public NPWMConfig lastStateSetter(DigitalState lastState) {
        setLastState(lastState);
        return this;
    }

    public NPWMConfig frequencySetter(float frequency) {
        setFrequency(frequency);
        return this;
    }

    public NPWMConfig dutyCycleSetter(float dutyCycle) {
        setDutyCycle(dutyCycle);
        return this;
    }

    public NPWMConfig durationSetter(String duration) {
        setDuration(duration);
        return this;
    }

    public NPWMConfig gpioPinSetter(NGPIOPin... pins) {
        setGPIOPins(pins);
        return this;
    }

    public NGPIOPin[] getGPIOPins() {
        return gpios;
    }

    public void setGPIOPins(NGPIOPin... gpios) {
        this.gpios = gpios;
    }


}
