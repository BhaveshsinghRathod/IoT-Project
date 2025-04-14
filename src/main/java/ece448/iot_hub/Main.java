package ece448.iot_hub;

import java.io.File;
import java.util.HashMap;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

public class Main implements AutoCloseable {

    public static void main(String[] args) throws Exception {
        // Load configuration file
        String configFile = (args.length > 0) ? args[0] : "hubConfig.json";
        HubConfig config = MAPPER.readValue(new File(configFile), HubConfig.class);
        LOGGER.info("Loaded configuration from {}: {}", configFile, MAPPER.writeValueAsString(config));

        try (Main mainInstance = new Main(config, args)) {
            // Run indefinitely
            while (true) {
                Thread.sleep(60000); // Sleep for 1 minute
            }
        }
    }

    public Main(HubConfig config, String[] args) throws Exception {
        // Initialize Spring application with custom properties
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("server.port", config.getHttpPort());
        properties.put("mqtt.broker", config.getMqttBroker());
        properties.put("mqtt.clientId", config.getMqttClientId());
        properties.put("mqtt.topicPrefix", config.getMqttTopicPrefix());

        SpringApplication springApp = new SpringApplication(App.class);
        springApp.setDefaultProperties(properties);
        this.applicationContext = springApp.run(args);
    }

    @Override
    public void close() throws Exception {
        applicationContext.close();
    }

    private final ConfigurableApplicationContext applicationContext;

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
}
