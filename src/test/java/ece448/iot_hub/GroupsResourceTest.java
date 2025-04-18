package ece448.iot_hub;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.Before;
import org.junit.Test;

public class GroupsResourceTest {

    private SimulatedGroupManager testManager;
    private GroupsResource restApi;

    class SimulatedGroupManager extends GroupsModel {
        private final Map<String, List<String>> dummyGroups = new HashMap<>();
        private final Map<String, String> controlLog = new HashMap<>();

        public SimulatedGroupManager() {
            super(null);
        }

        @Override
        public void createOrUpdateGroup(String id, List<String> plugs) {
            dummyGroups.put(id, new ArrayList<>(plugs));
        }

        @Override
        public void removeGroup(String id) {
            dummyGroups.remove(id);
        }

        @Override
        public Map<String, Object> getGroup(String id) {
            List<String> plugs = dummyGroups.getOrDefault(id, List.of());
            List<Map<String, Object>> plugInfo = new ArrayList<>();
            for (String p : plugs) {
                plugInfo.add(Map.of("name", p, "state", "on", "power", 10.0f));
            }
            return Map.of("name", id, "members", plugInfo);
        }

        @Override
        public List<Map<String, Object>> getAllGroups() {
            List<Map<String, Object>> all = new ArrayList<>();
            dummyGroups.keySet().forEach(id -> all.add(getGroup(id)));
            return all;
        }

        @Override
        public void controlGroup(String id, String act) {
            controlLog.put(id, act);
        }

        public String lastControl(String id) {
            return controlLog.get(id);
        }

        public List<String> plugsFor(String id) {
            return dummyGroups.get(id);
        }

        public boolean exists(String id) {
            return dummyGroups.containsKey(id);
        }
    }

    @Before
    public void setup() {
        testManager = new SimulatedGroupManager();
        restApi = new GroupsResource(testManager);
    }

    @Test
    public void testPostCreatesGroup() {
        List<String> plugs = List.of("plug1", "plug2");
        Object result = restApi.createGroup("grp", plugs);

        assertTrue(testManager.exists("grp"));
        assertEquals(plugs, testManager.plugsFor("grp"));
    }

    @Test
    public void testGetReturnsGroup() {
        restApi.createGroup("grp", List.of("plug1"));
        Map<?, ?> result = (Map<?, ?>) restApi.getGroup("grp", null);

        assertEquals("grp", result.get("name"));
    }

    @Test
    public void testDeleteGroup() {
        restApi.createGroup("tbd", List.of("plug1"));
        restApi.removeGroup("tbd");

        assertFalse(testManager.exists("tbd"));
    }

    @Test
    public void testGroupControlThroughGet() {
        restApi.createGroup("controlTest", List.of("plug1"));
        restApi.getGroup("controlTest", "toggle");

        assertEquals("toggle", testManager.lastControl("controlTest"));
    }

    @Test
    public void testGetAllMultipleGroups() {
        restApi.createGroup("g1", List.of("plug1"));
        restApi.createGroup("g2", List.of("plug2"));

        List<?> all = (List<?>) restApi.getAllGroups();
        assertEquals(2, all.size());
    }
}