package io.xlogistx.iot.mqtt;

//import org.eclipse.paho.client.mqttv3.MqttClient;
//import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
//import org.eclipse.paho.client.mqttv3.MqttException;
//import org.eclipse.paho.client.mqttv3.MqttMessage;
//import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;


import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.zoxweb.shared.util.Const;
import org.zoxweb.shared.util.SharedStringUtil;
import org.zoxweb.shared.util.SharedUtil;

import java.util.UUID;

public class MQTTClient {


  public static void main(String[] args) {

    String topic        = "testTopic";
    String content      = "Message from MqttPublishSample";
    int qos             = -1;
    String broker       = null;
    String clientId     = UUID.randomUUID().toString();
    MemoryPersistence persistence = new MemoryPersistence();

    try {
      int index = 0;
      broker = args[index++];
      String username = args.length > index ? args[index++] : null;
      String password = args.length > index ? args[index++] : null;
      int repeat = args.length > index ? SharedUtil.parseInt(args[index++]) : 1;
      qos =  args.length > index ? SharedUtil.parseInt(args[index++]) : -1;
      MqttClient sampleClient = new MqttClient(broker, clientId, persistence);
      MqttConnectionOptions connOpts = new MqttConnectionOptions();
      connOpts.setCleanStart(true);
      System.out.println("Connecting to broker: "+broker);
      if(username != null)
        connOpts.setUserName(username);
      if(password != null)
        connOpts.setPassword(SharedStringUtil.getBytes(password));

//      connOpts.setSSLHostnameVerifier(SSLCheckDisabler.SINGLETON.getHostnameVerifier());
      //connOpts.setSocketFactory(SSLCheckDisabler.SINGLETON.getSSLFactory());
      sampleClient.connect(connOpts);
      System.out.println("Connected");
      System.out.println("Publishing message: "+content);
      System.out.println("SSLProperties: " + connOpts.getSSLProperties());
      long ts = System.currentTimeMillis();
      for (int i = 0; i < repeat; i++) {
        String msg = clientId +":["+ (i +1) + ":" + System.currentTimeMillis() + "]: " + content;
        MqttMessage message = new MqttMessage(SharedStringUtil.getBytes(msg ));
        int lqos = (qos < 0 || qos > 2) ? (i%3) : qos;
        message.setQos(lqos);

        sampleClient.publish(topic, message);
        //System.out.println(lqos +": Message published: " + msg);
      }

      ts = System.currentTimeMillis() - ts;

      float rate = ((float)repeat/(float)ts)*1000;
      System.out.println("Disconnected it took: " + Const.TimeInMillis.toString(ts) + " to send " + repeat + " rate " + rate +" msg/s");
      MqttMessage last = new MqttMessage(SharedStringUtil.getBytes("Last message of " +repeat));

      last.setQos(qos);
      sampleClient.publish(topic, last);

      //TaskUtil.sleep(Const.TimeInMillis.SECOND.MILLIS*10);

    } catch(MqttException me) {
      System.out.println("reason "+me.getReasonCode());
      System.out.println("msg "+me.getMessage());
      System.out.println("loc "+me.getLocalizedMessage());
      System.out.println("cause "+me.getCause());
      System.out.println("exception "+me);
      me.printStackTrace();
    }


    System.exit(0);
  }

}
