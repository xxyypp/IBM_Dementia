package helpers;
import android.content.Context;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;

public class MqttHelper {
        public MqttAndroidClient mqttAndroidClient;


        final String serverUri = "tcp://192.168.43.185:1883";

        final String clientId = "ExampleAndroidClient";
        final String subscriptionTopic = "Test";

        final String username = "admin";
        final String password = "password";

        final String topic = "Test";
        final String payload = "Hello from Android";
        byte[] encodedPayload = new byte[0];

        public MqttHelper(Context context, String pub_topic, String location){
            mqttAndroidClient = new MqttAndroidClient(context, serverUri, clientId);
            mqttAndroidClient.setCallback(new MqttCallbackExtended() {

                @Override
                public void connectComplete(boolean b, String s) {
                    Log.w("Test", s);
                }

                //losing the MQTT connection
                @Override
                public void connectionLost(Throwable throwable) {

                }

                //message is received on a subscribed topic.
                @Override
                public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                    Log.w("Test", mqttMessage.toString());
                }

                //message published by this client is successfully received by the broker.
                @Override
                public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                }
            });
            connect(pub_topic,location);
        }

        public void setCallback(MqttCallbackExtended callback) {
            mqttAndroidClient.setCallback(callback);
        }

        private void connect(final String pub_topic,final String location){
            MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
            mqttConnectOptions.setAutomaticReconnect(true);
            mqttConnectOptions.setCleanSession(false);
            //mqttConnectOptions.setUserName(username);
            //mqttConnectOptions.setPassword(password.toCharArray());

            try {
                mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {

                        DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                        disconnectedBufferOptions.setBufferEnabled(true);
                        disconnectedBufferOptions.setBufferSize(100);
                        disconnectedBufferOptions.setPersistBuffer(false);
                        disconnectedBufferOptions.setDeleteOldestMessages(false);
                        mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                        subscribeToTopic();
                        publishToTopic(pub_topic,location);
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        Log.w("Test", "Failed to connect to: " + serverUri + exception.toString());
                    }
                });
            } catch (MqttException ex){
                ex.printStackTrace();
            }
        }

        private void publishToTopic(String pub_topic, String location){
            try {
                encodedPayload = location.getBytes("UTF-8");
                MqttMessage message = new MqttMessage(encodedPayload);
                int qos=2;
                message.setQos(qos);
                //message.setQos(qos);
                //mqttAndroidClient.publish(topic, message);
                mqttAndroidClient.publish(pub_topic, message);
            } catch (UnsupportedEncodingException | MqttException e) {
                e.printStackTrace();
            }
        }

        private void subscribeToTopic() {
            try {
                mqttAndroidClient.subscribe(subscriptionTopic, 2, null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Log.w("Test","Subscribed!");
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        Log.w("Test", "Subscribed fail!");
                    }
                });

            } catch (MqttException ex) {
                System.err.println("Exception whilst subscribing");
                ex.printStackTrace();
            }
        }

}
