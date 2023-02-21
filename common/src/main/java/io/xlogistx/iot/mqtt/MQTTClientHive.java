package io.xlogistx.iot.mqtt;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import org.zoxweb.server.task.TaskUtil;
import org.zoxweb.shared.util.Const;
import org.zoxweb.shared.util.RateCounter;
import org.zoxweb.shared.util.SharedStringUtil;

import java.util.Date;

public class MQTTClientHive {
    public static void main (String ...args)
    {
        try {
//            Mqtt5Client client = Mqtt5Client.builder()
//                    .identifier(UUID.randomUUID().toString())
//                    .serverHost("iot.xlogistx.io")
//                    .serverPort(8883)
//                    .useSslWithDefaultConfig()
//                    .build();
//
//
//            client.connectWith()
//                    .simpleAuth()
//                    .username("my-user")
//                    .password("my-password".getBytes())
//                    .applySimpleAuth()
//                    .send()
//                    .whenComplete((connAck, throwable) -> {
//                        if (throwable != null) {
//                            // handle failure
//                            throwable.printStackTrace();
//                        } else {
//                            // setup subscribes or start publishing
//                            System.out.println("Connected");
//                        }
//                    });
//
//
//            client.publishWith()
//                    .topic("testTopic")
//                    .payload("hello world".getBytes())
//                    .qos(MqttQos.AT_MOST_ONCE)
//                    .send()
//                    .whenComplete((publish, throwable) -> {
//                        if (throwable != null) {
//                            // handle failure to publish
//                            throwable.printStackTrace();
//                        } else {
//                            // handle successful publish, e.g. logging or incrementing a metric
//                            System.out.println("Sent");
//                        }
//                    });
//
//            //client.disconnect();


//            final Mqtt5AsyncClient client = Mqtt5Client.builder().serverHost("iot.xlogistx.io").serverPort(8883).useSslWithDefaultConfig().buildAsync();
//            AtomicLong counter = new AtomicLong();
//            for (int i=0; i < 100; i++) {
//                client.connect()
//                        .thenAccept(connAck -> System.out.println("connected " + connAck))
//                        .thenCompose(v -> client.publishWith().topic("testTopic").qos(MqttQos.AT_MOST_ONCE).payload((new Date().toString() + " " + counter.incrementAndGet()).toString().getBytes()).send())
//                        .thenAccept(publishResult -> System.out.println("published " + publishResult))
//                        //.thenCompose(v -> client.disconnect())
//                        .thenAccept(v -> System.out.println("disconnected"));
//                //client.connect();
//            }
//
//            System.out.println("see that everything above is async");
//            for (int i = 0; i < 5; i++) {
//                TimeUnit.MILLISECONDS.sleep(50);
//                System.out.println("...");
//            }

            RateCounter rc = new RateCounter("hive-client");
            String topic = "testTopic";
            String brokerHost = "iot.xlogistx.io";
            int brokerPort = 8883;

            Mqtt5BlockingClient client =  Mqtt5Client.builder().serverHost(brokerHost).serverPort(brokerPort).useSslWithDefaultConfig().buildBlocking();
            client.connect();
            for(int i = 0; i < 100000; i++) {
                long ts = System.currentTimeMillis();
                client.publishWith().topic(topic).qos(MqttQos.AT_MOST_ONCE).payload(SharedStringUtil.getBytes("[" + (i + 1) + "]:" + new Date())).send();
                rc.register(System.currentTimeMillis() - ts);
            }
            TaskUtil.sleep(Const.TimeInMillis.SECOND.MILLIS*10);
            client.disconnect();
            System.out.println(rc + " " + rc.rate(Const.TimeInMillis.SECOND.MILLIS));
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

    }
}
