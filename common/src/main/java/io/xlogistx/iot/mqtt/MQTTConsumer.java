package io.xlogistx.iot.mqtt;


import io.xlogistx.common.crypto.DigestAppender;
import org.eclipse.paho.mqttv5.client.*;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.zoxweb.server.task.TaskUtil;
import org.zoxweb.shared.util.Const;
import org.zoxweb.shared.util.RateCounter;
import org.zoxweb.shared.util.SharedStringUtil;
import org.zoxweb.shared.util.SharedUtil;

import java.util.Date;
import java.util.UUID;

public class MQTTConsumer {

  static RateCounter counter = new RateCounter("Received");

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
      String username = args.length > index ? args[index++] : null;
      String password = args.length > index ? args[index++] : null;
      MqttClient sampleClient = new MqttClient(broker, clientId, persistence);
      MqttConnectionOptions connOpts = new MqttConnectionOptions();
//      connOpts.setSSLHostnameVerifier(SSLCheckDisabler.SINGLETON.getHostnameVerifier());
//      connOpts.setSocketFactory(SSLCheckDisabler.SINGLETON.getSSLFactory());

      connOpts.setAutomaticReconnect(true);
      connOpts.setKeepAliveInterval(60);

      //connOpts.setCleanSession(true);
      //connOpts.setConnectionTimeout(10);
      if(username != null)
        connOpts.setUserName(username);
      if(password != null)
        connOpts.setPassword(SharedStringUtil.getBytes(password));
      System.out.println("Connecting to broker: " + broker + " topic: " + topic + ", " + username + ", " + password);
      sampleClient.connect(connOpts);




      sampleClient.setCallback(new MqttCallback() {

        DigestAppender da;
        MqttCallback init(DigestAppender da){
          this.da = da;
          return this;
        }



        @Override
        public void disconnected(MqttDisconnectResponse mqttDisconnectResponse) {

        }

        @Override
        public void mqttErrorOccurred(MqttException e) {

        }

        @Override
        public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
          counter.inc();
          //if(counter.getCounts()%200 == 0)
          {
            //String hash = da.appendToString(mqttMessage.getPayload());
            System.out.println(SharedUtil.toCanonicalID(':', Thread.currentThread(), counter, SharedStringUtil.toString(mqttMessage.getPayload())));
          }
          //System.out.println(mqttMessage);
        }

        @Override
        public void deliveryComplete(IMqttToken iMqttToken) {

        }

        @Override
        public void connectComplete(boolean b, String s) {

        }

        @Override
        public void authPacketArrived(int i, MqttProperties mqttProperties) {

        }


      }.init(new DigestAppender("sha-256")));

      sampleClient.subscribe(topic, qos);


//      MqttMessage message = new MqttMessage(content.getBytes());
//      message.setQos(qos);
//      sampleClient.publish(topic, message);

      System.out.println("Connected");
      TaskUtil.getDefaultTaskScheduler().queue(Const.TimeInMillis.SECOND.MILLIS * 5, new Runnable() {
        @Override
        public void run() {
          System.out.println(counter);
          TaskUtil.getDefaultTaskScheduler().queue(Const.TimeInMillis.SECOND.MILLIS*5, this);
        }
      });

    } catch(Exception me) {
      if(me instanceof MqttException)
        System.out.println("reason "+((MqttException)me).getReasonCode());
      System.out.println("msg "+me.getMessage());
      System.out.println("loc "+me.getLocalizedMessage());
      System.out.println("cause "+me.getCause());
      System.out.println("excep "+me);
      me.printStackTrace();
    }
  }

}
