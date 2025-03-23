package ece448.iot_sim;

import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MQTTUpdates {
    private final String prefix;

    public MQTTUpdates(String prefix) {
        this.prefix = prefix;
    }

    public String getTopic(String plugName, String updateType) {
        return prefix + "/update/" + plugName + "/" + updateType;
    }

    public MqttMessage getMessage(String value) {
        MqttMessage message = new MqttMessage(value.getBytes());
        message.setRetained(true);
        return message;
    }
}
