package ece448.iot_hub;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Test class for PlugsModel.
 * Aims to achieve 100% code coverage.
 */
public class PlugsModelTest {

    private PlugsModel plugsModel;
    private MockMqttController mockMqttController;

    @Before
    public void setUp() {
        mockMqttController = new MockMqttController();
        plugsModel = new PlugsModel(mockMqttController);
    }

    @Test
    public void testCreatePlugWithStateAndPower() {
        // Arrange
        String plugName = "plug1";
        mockMqttController.updateState(plugName, "on");
        mockMqttController.updatePower(plugName, "10.5");
        
        // Act
        Map<String, Object> result = plugsModel.createPlug(plugName);
        
        // Assert
        assertEquals("plug1", result.get("name"));
        assertEquals("on", result.get("state"));
        assertEquals(10.5f, ((Number)result.get("power")).floatValue(), 0.001);
    }

    @Test
    public void testCreatePlugWithNullState() {
        // Arrange
        String plugName = "plug2";
        // Don't set state, so it will be null
        mockMqttController.updatePower(plugName, "5.0");
        
        // Act
        Map<String, Object> result = plugsModel.createPlug(plugName);
        
        // Assert
        assertEquals("plug2", result.get("name"));
        assertEquals("off", result.get("state")); // Default when null
        assertEquals(5.0f, ((Number)result.get("power")).floatValue(), 0.001);
    }

    @Test
    public void testCreatePlugWithNullPower() {
        // Arrange
        String plugName = "plug3";
        mockMqttController.updateState(plugName, "off");
        // Don't set power, so it will be null
        
        // Act
        Map<String, Object> result = plugsModel.createPlug(plugName);
        
        // Assert
        assertEquals("plug3", result.get("name"));
        assertEquals("off", result.get("state"));
        assertEquals(0.0f, ((Number)result.get("power")).floatValue(), 0.001); // Default when null
    }

    @Test
    public void testCreatePlugWithNullStateAndPower() {
        // Arrange
        String plugName = "plug4";
        // Don't set state or power, so both will be null
        
        // Act
        Map<String, Object> result = plugsModel.createPlug(plugName);
        
        // Assert
        assertEquals("plug4", result.get("name"));
        assertEquals("off", result.get("state")); // Default when null
        assertEquals(0.0f, ((Number)result.get("power")).floatValue(), 0.001); // Default when null
    }

    @Test
    public void testGetAllPlugsWithBothStateAndPower() {
        // Arrange
        mockMqttController.updateState("plug1", "on");
        mockMqttController.updatePower("plug1", "10.5");
        mockMqttController.updateState("plug2", "off");
        mockMqttController.updatePower("plug2", "0.0");
        
        // Act
        List<Map<String, Object>> results = plugsModel.getAllPlugs();
        
        // Assert
        assertEquals(2, results.size());
        
        // Find and verify each plug
        verifyPlugInList(results, "plug1", "on", 10.5f);
        verifyPlugInList(results, "plug2", "off", 0.0f);
    }

    @Test
    public void testGetAllPlugsWithOnlyStates() {
        // Arrange
        mockMqttController.updateState("plug1", "on");
        mockMqttController.updateState("plug2", "off");
        
        // Act
        List<Map<String, Object>> results = plugsModel.getAllPlugs();
        
        // Assert
        assertEquals(2, results.size());
        
        // Find and verify each plug
        verifyPlugInList(results, "plug1", "on", 0.0f);
        verifyPlugInList(results, "plug2", "off", 0.0f);
    }

    @Test
    public void testGetAllPlugsWithOnlyPowers() {
        // Arrange
        mockMqttController.updatePower("plug1", "10.5");
        mockMqttController.updatePower("plug2", "5.0");
        
        // Act
        List<Map<String, Object>> results = plugsModel.getAllPlugs();
        
        // Assert
        assertEquals(2, results.size());
        
        // Find and verify each plug
        verifyPlugInList(results, "plug1", "off", 10.5f);
        verifyPlugInList(results, "plug2", "off", 5.0f);
    }

    @Test
    public void testGetAllPlugsWithOverlappingPlugs() {
        // Arrange
        mockMqttController.updateState("plug1", "on");
        mockMqttController.updatePower("plug1", "10.5");
        mockMqttController.updateState("plug2", "off");
        mockMqttController.updatePower("plug3", "5.0");
        
        // Act
        List<Map<String, Object>> results = plugsModel.getAllPlugs();
        
        // Assert
        assertEquals(3, results.size());
        
        // Find and verify each plug
        verifyPlugInList(results, "plug1", "on", 10.5f);
        verifyPlugInList(results, "plug2", "off", 0.0f);
        verifyPlugInList(results, "plug3", "off", 5.0f);
    }

    @Test
    public void testGetAllPlugsEmpty() {
        // Act
        List<Map<String, Object>> results = plugsModel.getAllPlugs();
        
        // Assert
        assertEquals(0, results.size());
    }

    @Test
    public void testControlPlug() {
        // Arrange
        String plugName = "plug1";
        String action = "on";
        
        // Act
        plugsModel.controlPlug(plugName, action);
        
        // Assert
        assertEquals(plugName, mockMqttController.lastPlugName);
        assertEquals(action, mockMqttController.lastAction);
        assertTrue("publishAction should be called", mockMqttController.publishActionCalled);
    }

    /**
     * Helper method to verify a plug in the results list
     */
    private void verifyPlugInList(List<Map<String, Object>> plugs, String name, String expectedState, float expectedPower) {
        Map<String, Object> plug = findPlugByName(plugs, name);
        assertNotNull("Plug " + name + " should be present", plug);
        assertEquals("State should match for plug " + name, expectedState, plug.get("state"));
        assertEquals("Power should match for plug " + name, expectedPower, ((Number)plug.get("power")).floatValue(), 0.001);
    }

    /**
     * Helper method to find a plug by name in the results list
     */
    private Map<String, Object> findPlugByName(List<Map<String, Object>> plugs, String name) {
        for (Map<String, Object> plug : plugs) {
            if (name.equals(plug.get("name"))) {
                return plug;
            }
        }
        return null;
    }

    /**
     * Mock implementation of MqttController for testing PlugsModel
     */
    private static class MockMqttController extends MqttController {
        private final HashMap<String, String> states = new HashMap<>();
        private final HashMap<String, String> powers = new HashMap<>();
        boolean publishActionCalled = false;
        String lastPlugName;
        String lastAction;
        
        public MockMqttController() {
            super("tcp://localhost:1883", "test_client", "test_prefix");
        }
        
        @Override
        protected void initializeMqttConnection() throws Exception {
            // Do nothing - avoid actual MQTT connection
        }
        
        @Override
        synchronized public void publishAction(String plugName, String action) {
            publishActionCalled = true;
            lastPlugName = plugName;
            lastAction = action;
        }
        
        @Override
        synchronized public String getState(String plugName) {
            return states.get(plugName);
        }
        
        @Override
        synchronized public String getPower(String plugName) {
            return powers.get(plugName);
        }
        
        @Override
        synchronized public Map<String, String> getStates() {
            return new TreeMap<>(states);
        }
        
        @Override
        synchronized public Map<String, String> getPowers() {
            return new TreeMap<>(powers);
        }
        
        // Helper methods for testing
        public void updateState(String plugName, String state) {
            states.put(plugName, state);
        }
        
        public void updatePower(String plugName, String power) {
            powers.put(plugName, power);
        }
    }
}
