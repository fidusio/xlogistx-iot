package io.xlogistx.iot.gpio64.data;

import com.google.gson.annotations.SerializedName;
import com.pi4j.io.gpio.digital.DigitalState;
import io.xlogistx.iot.gpio64.GPIO64Pin;
import org.zoxweb.shared.util.Const;

public class PWM64Config {


    private GPIO64Pin[] gpios;
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

    public PWM64Config lastStateSetter(DigitalState lastState) {
        setLastState(lastState);
        return this;
    }

    public PWM64Config frequencySetter(float frequency) {
        setFrequency(frequency);
        return this;
    }

    public PWM64Config dutyCycleSetter(float dutyCycle) {
        setDutyCycle(dutyCycle);
        return this;
    }

    public PWM64Config durationSetter(String duration) {
        setDuration(duration);
        return this;
    }

    public PWM64Config gpioPinSetter(GPIO64Pin... pins) {
        setGPIOPins(pins);
        return this;
    }

    public GPIO64Pin[] getGPIOPins() {
        return gpios;
    }

    public void setGPIOPins(GPIO64Pin... gpios) {
        this.gpios = gpios;
    }


}
