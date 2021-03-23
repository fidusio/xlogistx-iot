package io.xlogistx.iot.mqtt;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.zoxweb.server.task.TaskUtil;
import org.zoxweb.shared.util.SharedUtil;

import java.util.Date;
import java.util.UUID;

public class MQTTConsumer {

  static int counter = 0;

  public static void main(String[] args) {

    String topic        = "testTopic";
    String content      = "Message from MqttPublishSample " + new Date();
    int qos             = 2;
    String broker       = null;
    String clientId     = UUID.randomUUID().toString();;
    MemoryPersistence persistence = new MemoryPersistence();


    try {
      int index = 0;
      broker = args[index++];
      MqttClient sampleClient = new MqttClient(broker, clientId, persistence);
      MqttConnectOptions connOpts = new MqttConnectOptions();
      connOpts.setCleanSession(true);
      connOpts.setAutomaticReconnect(true);
      connOpts.setCleanSession(true);
      connOpts.setConnectionTimeout(10);
      sampleClient.connect(connOpts);
      System.out.println("Connecting to broker: "+broker + " topic: " + topic);



      sampleClient.setCallback(new MqttCallback() {
        @Override
        public void connectionLost(Throwable throwable) {
            throwable.printStackTrace();
        }

        @Override
        public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
          counter++;
          System.out.println(SharedUtil.toCanonicalID(':', counter, mqttMessage));
          //System.out.println(mqttMessage);
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
          System.out.println(iMqttDeliveryToken);
        }
      });

      sampleClient.subscribe(topic);


//      MqttMessage message = new MqttMessage(content.getBytes());
//      message.setQos(qos);
//      sampleClient.publish(topic, message);

      System.out.println("Connected");
      TaskUtil.getDefaultTaskScheduler();

    } catch(MqttException me) {
      System.out.println("reason "+me.getReasonCode());
      System.out.println("msg "+me.getMessage());
      System.out.println("loc "+me.getLocalizedMessage());
      System.out.println("cause "+me.getCause());
      System.out.println("excep "+me);
      me.printStackTrace();
    }
  }

}
