package io.xlogistx.iot.mqtt;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;

import java.util.UUID;

public class HivePublisher {
    public static void main(String ...args)
    {
        Mqtt3AsyncClient client = MqttClient.builder()
                .useMqttVersion3()
                .serverHost("api.xlogistx.io")
                .serverPort(8883)
                .sslWithDefaultConfig()
                .identifier(UUID.randomUUID().toString())
                .simpleAuth()
                    .username("my-user")
                    .password("my-password".getBytes())
                    .applySimpleAuth()
                .buildAsync();

    }
}
