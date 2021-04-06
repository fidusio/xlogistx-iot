package io.xlogistx.iot.mqtt;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.zoxweb.shared.util.Const;
import org.zoxweb.shared.util.SharedStringUtil;
import org.zoxweb.shared.util.SharedUtil;

import java.util.Date;
import java.util.UUID;

public class MQTTClient {


  public static void main(String[] args) {

    String topic        = "testTopic";
    String content      = "Message from MqttPublishSample";
    int qos             = 2;
    String broker       = null;
    String clientId     = UUID.randomUUID().toString();
    MemoryPersistence persistence = new MemoryPersistence();

    try {
      int index = 0;
      broker = args[index++];
      int repeat = args.length > index ? SharedUtil.parseInt(args[index++]) : 1;
      String username = args.length > index ? args[index++] : null;
      String password = args.length > index ? args[index++] : null;
      MqttClient sampleClient = new MqttClient(broker, clientId, persistence);
      MqttConnectOptions connOpts = new MqttConnectOptions();
      connOpts.setCleanSession(true);
      System.out.println("Connecting to broker: "+broker);
      if(username != null)
        connOpts.setUserName(username);
      if(password != null)
        connOpts.setPassword(password.toCharArray());
      sampleClient.connect(connOpts);
      System.out.println("Connected");
      System.out.println("Publishing message: "+content);
      long ts = System.currentTimeMillis();
      for (int i = 0; i < repeat; i++) {
        String msg = clientId +":["+ (i +1) + ":" + System.currentTimeMillis() + "]: " + content;
        MqttMessage message = new MqttMessage(SharedStringUtil.getBytes(msg));
        message.setQos(qos);
        sampleClient.publish(topic, message);
        System.out.println("Message published: " + msg);
      }
      ts = System.currentTimeMillis() - ts;

      //sampleClient.disconnect();
      System.out.println("Disconnected it took: " + Const.TimeInMillis.toString(ts));
      System.exit(0);
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
