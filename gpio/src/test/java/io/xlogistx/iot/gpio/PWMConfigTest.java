package io.xlogistx.iot.gpio;


import org.junit.Test;
import org.zoxweb.server.util.GSONUtil;

public class PWMConfigTest {
  @Test
  public void jsonTest()
  {
    PWMConfig pwmConfig = new PWMConfig().frequencySetter(2).dutyCycleSetter(50).countSetter(5).gpioPinSetter(GPIOPin.GPIO_02);
    System.out.println(GSONUtil.DEFAULT_GSON.toJson(pwmConfig));

  }
}
