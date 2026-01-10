package io.xlogistx.iot.gpio32;



import io.xlogistx.iot.gpio32.data.PWMConfig;
import org.junit.jupiter.api.Test;
import org.zoxweb.server.util.GSONUtil;

public class PWMConfigTest {
  @Test
  public void jsonTest()
  {
    PWMConfig pwmConfig = new PWMConfig().frequencySetter(2).dutyCycleSetter(50).durationSetter("5sec").gpioPinSetter(GPIOPin.GPIO_02);
    System.out.println(GSONUtil.toJSONDefault(pwmConfig));

  }
}
