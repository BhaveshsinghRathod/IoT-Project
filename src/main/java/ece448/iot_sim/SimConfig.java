package ece448.iot_sim;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SimConfig { // configuration class for the IOT simulator

	private final int httpPort; // Port number for the HTTP server
	private final List<String> plugNames; // List of the plug names in the simulation
	private final String mqttBroker; // MQTT broker URL
	private final String mqttClientId; // MQTT client identifier
	private final String mqttTopicPrefix; // Prefix for MQTT topics

	@JsonCreator
	public SimConfig(
		@JsonProperty(value = "httpPort", required = true) int httpPort,
		@JsonProperty(value = "plugNames", required = true) List<String> plugNames,
		@JsonProperty(value = "mqttBroker", required = false) String mqttBroker,
		@JsonProperty(value = "mqttClientId", required = false) String mqttClientId,
		@JsonProperty(value = "mqttTopicPrefix", required = false) String mqttTopicPrefix) {
		this.httpPort = httpPort;
		this.plugNames = plugNames;
		this.mqttBroker = mqttBroker;
		this.mqttClientId = mqttClientId;
		this.mqttTopicPrefix = mqttTopicPrefix;
	}

	public int getHttpPort() { // returns the HTTP port number for the simulator
		return httpPort;
	}

	public List<String> getPlugNames() { // returns the list of plug names in the simulation
		return plugNames;
	}

	public String getMqttBroker() { // return the MQTT broker URL or null if not provided
		return mqttBroker;
	}

	public String getMqttClientId() { // return the MQTT client ID or null if not provided
		return mqttClientId;
	}

	public String getMqttTopicPrefix() { // return the MQTT topic prefix or null if not provided
		return mqttTopicPrefix;
	}
}