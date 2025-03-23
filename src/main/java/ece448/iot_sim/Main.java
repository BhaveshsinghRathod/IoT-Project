package ece448.iot_sim;

import java.io.File;
import java.util.ArrayList;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ece448.iot_sim.http_server.JHTTP;

public class Main implements AutoCloseable {
    public static void main(String[] args) throws Exception {
        String configFile = args.length > 0 ? args[0] : "simConfig.json";
        SimConfig config = mapper.readValue(new File(configFile), SimConfig.class);
        logger.info("Loaded config: {}", mapper.writeValueAsString(config));

        try (Main m = new Main(config)) {
            while (true) Thread.sleep(60000);
        }
    }

    public Main(SimConfig config) throws Exception {
        // Initialize plugs
        ArrayList<PlugSim> plugs = new ArrayList<>();
        for (String name : config.getPlugNames()) {
            plugs.add(new PlugSim(name));
        }

        // Start power monitoring
        new MeasurePower(plugs).start();

        // HTTP Server
        this.http = new JHTTP(config.getHttpPort(), new HTTPCommands(plugs));
        this.http.start();

        // MQTT Client Setup
        this.mqttClient = new MqttClient(
            config.getMqttBroker(),
            config.getMqttClientId(),
            new org.eclipse.paho.client.mqttv3.persist.MemoryPersistence()
        );
        
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        mqttClient.connect(options);

        // MQTT Command Handling
        MQTTCommands commands = new MQTTCommands(plugs, config.getMqttTopicPrefix());
        mqttClient.subscribe(commands.getTopic(), (topic, msg) -> {
            try {
                commands.handleMessage(topic, msg);
            } catch (Exception e) {
                logger.error("MQTT command error: {}", e.getMessage());
            }
        });

        // MQTT Update Publishing
        MQTTUpdates updates = new MQTTUpdates(config.getMqttTopicPrefix());
        for (PlugSim plug : plugs) {
            // State updates
            plug.addStateChangeListener(state -> {
                try {
                    String topic = updates.getTopic(plug.getName(), "state");
                    MqttMessage message = updates.getMessage(state ? "on" : "off");
                    mqttClient.publish(topic, message);
                } catch (Exception e) {
                    logger.error("State update failed: {}", e.getMessage());
                }
            });

            // Power updates (always format to 3 decimal places)
            plug.addPowerChangeListener(power -> {
                try {
                    String topic = updates.getTopic(plug.getName(), "power");
                    String value = String.format("%.3f", power);
                    MqttMessage message = updates.getMessage(value);
                    mqttClient.publish(topic, message);
                } catch (Exception e) {
                    logger.error("Power update failed: {}", e.getMessage());
                }
            });
        }
    }

    @Override
    public void close() throws Exception {
        http.close();
        if (mqttClient != null && mqttClient.isConnected()) {
            mqttClient.disconnect();
            mqttClient.close();
        }
    }

    private final JHTTP http;
    private final MqttClient mqttClient;
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
}
