package io.xlogistx.iot.gpio;

import com.google.gson.annotations.SerializedName;
import com.pi4j.io.gpio.PinState;

public class PWMConfig {



  private GPIOPin[] gpios;
  private float frequency;
  @SerializedName("duty_cycle")
  private float dutyCycle;
  private long count;
  @SerializedName("last_state")
  private PinState lastState;


  public float getFrequency() {
    return frequency;
  }

  public void setFrequency(float frequency) {
    if(frequency <= 0)
    {
      throw new IllegalArgumentException("Invalid frequency " + frequency);
    }
    this.frequency = frequency;
  }

  public float getDutyCycle() {
    return dutyCycle;
  }

  public void setDutyCycle(float dutyCycle) {
    if(dutyCycle <=0 || dutyCycle >=100)
    {
      throw new IllegalArgumentException("Invalid duty cycle " + dutyCycle);
    }
    this.dutyCycle = dutyCycle;
  }

  public long getCount() {
    return count;
  }

  public void setCount(long count) {
    if (count < 1)
    {
      throw new IllegalArgumentException("Invalid count " + count);
    }
    this.count = count;
  }

  public PinState getLastState() {
    return lastState;
  }

  public void setLastState(PinState lastState) {
    this.lastState = lastState;
  }

  public PWMConfig lastStateSetter(PinState lastState)
  {
    setLastState(lastState);
    return this;
  }
  public PWMConfig frequencySetter(float frequency)
  {
    setFrequency(frequency);
    return this;
  }

  public PWMConfig dutyCycleSetter(float dutyCycle)
  {
    setDutyCycle(dutyCycle);
    return this;
  }
  public PWMConfig countSetter(long count)
  {
    setCount(count);
    return this;
  }

  public PWMConfig gpioPinSetter(GPIOPin ...pins)
  {
    setGIOPins(pins);
    return this;
  }

  public GPIOPin[] getGPIOPins() {
    return gpios;
  }

  public void setGIOPins(GPIOPin ...gpios) {
    this.gpios = gpios;
  }



}
