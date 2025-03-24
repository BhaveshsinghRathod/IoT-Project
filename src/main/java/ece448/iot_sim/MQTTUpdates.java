package ece448.iot_sim;

import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MQTTUpdates { // Manages MQTT update messages by formatting topics and creating messages
    private final String topicPrefix; // construct an MQTTupdates instance with the specified topic prefix

    public MQTTUpdates(String topicPrefix) {
        this.topicPrefix = topicPrefix != null ? topicPrefix : "";
    }

    public String getTopic(String name, String key) {
        return String.format("%s/update/%s/%s", topicPrefix, name, key);
    }

    public MqttMessage getMessage(String value) { // creates an MQTT message with the given value
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }
        MqttMessage msg = new MqttMessage(value.getBytes());
        msg.setRetained(true);
        return msg;
    }
}
