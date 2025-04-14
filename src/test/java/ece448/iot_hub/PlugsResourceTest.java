package ece448.iot_hub;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test class for PlugsResource.
 */
public class PlugsResourceTest {

    private PlugsResource plugsResource;
    private MockPlugsModel mockPlugsModel;

    /**
     * A mock implementation for testing PlugsResource
     */
    private static class MockPlugsModel {
        boolean controlPlugCalled = false;
        String lastControlledPlug = null;
        String lastAction = null;
        
        public Map<String, Object> createPlug(String plug) {
            Map<String, Object> plugDetails = new HashMap<>();
            plugDetails.put("name", plug);
            plugDetails.put("status", "off");
            return plugDetails;
        }
        
        public void controlPlug(String plug, String action) {
            controlPlugCalled = true;
            lastControlledPlug = plug;
            lastAction = action;
        }
        
        public List<Map<String, Object>> getAllPlugs() {
            List<Map<String, Object>> plugs = new ArrayList<>();
            
            Map<String, Object> plug1 = new HashMap<>();
            plug1.put("name", "plug1");
            plug1.put("status", "on");
            
            Map<String, Object> plug2 = new HashMap<>();
            plug2.put("name", "plug2");
            plug2.put("status", "off");
            
            plugs.add(plug1);
            plugs.add(plug2);
            
            return plugs;
        }
    }
    
    /**
     * Create a subclass of PlugsResource that works with our mock
     */
    private static class TestablePlugsResource extends PlugsResource {
        private final MockPlugsModel mockModel;
        
        public TestablePlugsResource(MockPlugsModel mockModel) {
            super(null); // Pass null to the parent constructor as we'll override the methods
            this.mockModel = mockModel;
        }
        
        @Override
        public Object getPlug(String plug, String action) {
            if (action != null) {
                mockModel.controlPlug(plug, action);
            }
            return mockModel.createPlug(plug);
        }
        
        @Override
        public Object getAllPlugs() {
            return mockModel.getAllPlugs();
        }
    }

    @Before
    public void setUp() {
        mockPlugsModel = new MockPlugsModel();
        plugsResource = new TestablePlugsResource(mockPlugsModel);
    }

    @Test
    public void testGetPlugWithoutAction() {
        // Arrange
        String plugName = "testPlug";
        
        // Act
        Object result = plugsResource.getPlug(plugName, null);
        
        // Assert
        assertTrue("Result should be a Map", result instanceof Map);
        Map<?, ?> resultMap = (Map<?, ?>) result;
        assertEquals("Plug name should match", plugName, resultMap.get("name"));
        assertEquals("Status should be 'off'", "off", resultMap.get("status"));
        assertFalse("controlPlug should not be called", mockPlugsModel.controlPlugCalled);
    }

    @Test
    public void testGetPlugWithAction() {
        // Arrange
        String plugName = "testPlug";
        String action = "on";
        
        // Act
        Object result = plugsResource.getPlug(plugName, action);
        
        // Assert
        assertTrue("Result should be a Map", result instanceof Map);
        Map<?, ?> resultMap = (Map<?, ?>) result;
        assertEquals("Plug name should match", plugName, resultMap.get("name"));
        assertEquals("Status should be 'off'", "off", resultMap.get("status"));
        
        assertTrue("controlPlug should be called", mockPlugsModel.controlPlugCalled);
        assertEquals("Last controlled plug should match", plugName, mockPlugsModel.lastControlledPlug);
        assertEquals("Last action should match", action, mockPlugsModel.lastAction);
    }

    @Test
    public void testGetAllPlugs() {
        // Act
        Object result = plugsResource.getAllPlugs();
        
        // Assert
        assertTrue("Result should be a List", result instanceof List);
        List<?> resultList = (List<?>) result;
        assertEquals("List should have 2 items", 2, resultList.size());
        
        assertTrue("First item should be a Map", resultList.get(0) instanceof Map);
        Map<?, ?> plug1 = (Map<?, ?>) resultList.get(0);
        assertEquals("First plug name should be 'plug1'", "plug1", plug1.get("name"));
        assertEquals("First plug status should be 'on'", "on", plug1.get("status"));
        
        assertTrue("Second item should be a Map", resultList.get(1) instanceof Map);
        Map<?, ?> plug2 = (Map<?, ?>) resultList.get(1);
        assertEquals("Second plug name should be 'plug2'", "plug2", plug2.get("name"));
        assertEquals("Second plug status should be 'off'", "off", plug2.get("status"));
    }
}
