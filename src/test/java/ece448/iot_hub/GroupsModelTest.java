package ece448.iot_hub;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.Before;
import org.junit.Test;

public class GroupsModelTest {

    private MockPlugRegistry testPlugs;
    private GroupsModel modelUnderTest;

    class MockPlugRegistry extends PlugsModel {

        private final Map<String, Map<String, Object>> stubPlugs = new HashMap<>();
        private final Map<String, String> sentActions = new HashMap<>();

        public MockPlugRegistry() {
            super(null);
            initialize("plug1", "on", 10.5f);
            initialize("plug2", "off", 0.0f);
            initialize("plug3", "on", 15.2f);
        }

        private void initialize(String id, String state, float usage) {
            Map<String, Object> plug = new HashMap<>();
            plug.put("name", id);
            plug.put("state", state);
            plug.put("power", usage);
            stubPlugs.put(id, plug);
        }

        @Override
        public Map<String, Object> createPlug(String id) {
            return stubPlugs.containsKey(id)
                ? new HashMap<>(stubPlugs.get(id))
                : Map.of("name", id, "state", "off", "power", 0.0f);
        }

        @Override
        public void controlPlug(String id, String cmd) {
            sentActions.put(id, cmd);
        }

        public String lastAction(String id) {
            return sentActions.get(id);
        }

        public void clearHistory() {
            sentActions.clear();
        }
    }

    @Before
    public void init() {
        testPlugs = new MockPlugRegistry();
        modelUnderTest = new GroupsModel(testPlugs);
    }

    @Test
    public void canCreateGroup() {
        modelUnderTest.createOrUpdateGroup("alpha", Arrays.asList("plug1", "plug2"));
        Map<String, Object> group = modelUnderTest.getGroup("alpha");

        assertEquals("alpha", group.get("name"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> members = (List<Map<String, Object>>) group.get("members");
        assertEquals(2, members.size());

        boolean hasP1 = members.stream().anyMatch(p -> "plug1".equals(p.get("name")));
        boolean hasP2 = members.stream().anyMatch(p -> "plug2".equals(p.get("name")));

        assertTrue(hasP1 && hasP2);
    }

    @Test
    public void handlesEmptyGroup() {
        modelUnderTest.createOrUpdateGroup("empty", new ArrayList<>());
        Map<String, Object> group = modelUnderTest.getGroup("empty");

        assertEquals("empty", group.get("name"));
        assertTrue(((List<?>) group.get("members")).isEmpty());
    }

    @Test
    public void supportsGroupUpdates() {
        modelUnderTest.createOrUpdateGroup("g", Arrays.asList("plug1"));
        modelUnderTest.createOrUpdateGroup("g", Arrays.asList("plug3"));
        List<?> members = (List<?>) modelUnderTest.getGroup("g").get("members");

        assertEquals(1, members.size());
        assertEquals("plug3", ((Map<?, ?>) members.get(0)).get("name"));
    }

    @Test
    public void canDeleteGroup() {
        modelUnderTest.createOrUpdateGroup("g", Arrays.asList("plug1"));
        modelUnderTest.removeGroup("g");

        List<?> members = (List<?>) modelUnderTest.getGroup("g").get("members");
        assertEquals(0, members.size());
    }

    @Test
    public void handlesMissingGroups() {
        List<?> members = (List<?>) modelUnderTest.getGroup("missing").get("members");
        assertTrue(members.isEmpty());
    }

    @Test
    public void enumeratesAllGroups() {
        modelUnderTest.createOrUpdateGroup("one", Arrays.asList("plug1"));
        modelUnderTest.createOrUpdateGroup("two", Arrays.asList("plug2"));

        List<?> all = modelUnderTest.getAllGroups();
        assertEquals(2, all.size());
    }

    @Test
    public void groupActionsAreSentToPlugs() {
        modelUnderTest.createOrUpdateGroup("target", Arrays.asList("plug1", "plug2"));

        testPlugs.clearHistory();
        modelUnderTest.controlGroup("target", "off");

        assertEquals("off", testPlugs.lastAction("plug1"));
        assertEquals("off", testPlugs.lastAction("plug2"));
    }

    @Test
    public void ignoresMissingControlGroups() {
        modelUnderTest.controlGroup("nope", "on");

        assertNull(testPlugs.lastAction("plug1"));
        assertNull(testPlugs.lastAction("plug2"));
    }

    @Test
    public void preventsDuplicates() {
        modelUnderTest.createOrUpdateGroup("dups", Arrays.asList("plug1", "plug1", "plug2"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> members = (List<Map<String, Object>>) modelUnderTest.getGroup("dups").get("members");

        assertEquals(2, members.size());
    }
}