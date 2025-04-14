package ece448.iot_hub;

import java.util.Map;
import java.util.TreeMap;
import java.util.HashMap;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MqttController {
    private static final Logger LOGGER = LoggerFactory.getLogger(MqttController.class);
    
    private final String broker;
    private final String clientId;
    protected final String topicPrefix;
    
    protected MqttClient client;
    private final HashMap<String, String> states = new HashMap<>();
    private final HashMap<String, String> powers = new HashMap<>();

    public MqttController(
        @Value("${mqtt.broker:tcp://localhost:1883}") String broker,
        @Value("${mqtt.clientId:iot_hub}") String clientId,
        @Value("${mqtt.topicPrefix:iot_ece448}") String topicPrefix) {
        
        this.broker = broker;
        this.clientId = clientId;
        this.topicPrefix = topicPrefix;
        
        LOGGER.info("Initializing MQTT Controller - Broker: {}, Client ID: {}, Topic Prefix: {}",
            broker, clientId, topicPrefix);
    }

    @PostConstruct
    public void start() {
        try {
            initializeMqttConnection();
            LOGGER.info("Successfully connected to MQTT broker: {}", broker);
        } catch (Exception e) {
            LOGGER.error("Connection failure to {}: {}", broker, e.getMessage(), e);
        }
    }

    protected void initializeMqttConnection() throws Exception {
        client = new MqttClient(broker, clientId, new MemoryPersistence());
        
        MqttConnectOptions connectOptions = new MqttConnectOptions();
        connectOptions.setCleanSession(true);
        client.connect(connectOptions);
        
        client.subscribe(topicPrefix + "/update/#", this::processIncomingMessage);
    }

    @PreDestroy
    public void close() {
        try {
            if (client != null && client.isConnected()) {
                client.disconnect();
                LOGGER.info("Disconnected from MQTT broker");
            }
        } catch (Exception e) {
            LOGGER.error("Disconnection error: {}", e.getMessage(), e);
        }
    }

    synchronized public void publishAction(String plugName, String action) {
        final String topic = String.format("%s/action/%s/%s", topicPrefix, plugName, action);
        
        try {
            if (client == null || !client.isConnected()) {
                LOGGER.warn("Publish failed - MQTT client not connected");
                return;
            }
            
            transmitMqttMessage(topic);
            LOGGER.debug("Action published - Device: {}, Action: {}", plugName, action);
        } catch (Exception e) {
            LOGGER.error("Publish error - Device: {}, Action: {}", plugName, action, e);
        }
    }

    protected void transmitMqttMessage(String topic) throws Exception {
        client.publish(topic, new MqttMessage());
    }

    // State management methods
    synchronized public String getState(String plugName) {
        return states.get(plugName);
    }

    synchronized public String getPower(String plugName) {
        return powers.get(plugName);
    }

    synchronized public Map<String, String> getStates() {
        return new TreeMap<>(states);
    }

    synchronized public Map<String, String> getPowers() {
        return new TreeMap<>(powers);
    }

    synchronized protected void processIncomingMessage(String topic, MqttMessage message) {
        LOGGER.debug("Received message - Topic: {}, Payload: {}", topic, message);
        
        // Expected topic format: {prefix}/update/{device}/{type}
        String[] components = topic.substring(topicPrefix.length() + 1).split("/");
        if (components.length != 3 || !"update".equals(components[0])) {
            LOGGER.debug("Ignoring malformed topic: {}", topic);
            return;
        }

        String device = components[1];
        String updateType = components[2];
        String value = new String(message.getPayload());

        switch (updateType) {
            case "state":
                states.put(device, value);
                LOGGER.debug("State updated - Device: {}, Value: {}", device, value);
                break;
            case "power":
                powers.put(device, value);
                LOGGER.debug("Power updated - Device: {}, Value: {}", device, value);
                break;
            default:
                LOGGER.debug("Unhandled update type: {}", updateType);
        }
    }
}
