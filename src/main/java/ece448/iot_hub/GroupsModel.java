package ece448.iot_hub;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class GroupsModel {

    private final PlugsModel plugManager;
    private final Map<String, Set<String>> groupStore = new HashMap<>();

    public GroupsModel(PlugsModel plugManager) {
        this.plugManager = plugManager;
        LOGGER.info("Group management module activated");
    }

    synchronized public void createOrUpdateGroup(String groupKey, List<String> plugIds) {
        groupStore.put(groupKey, new HashSet<>(plugIds));
        LOGGER.debug("Group '{}' assigned {} plug(s)", groupKey, plugIds.size());
    }

    synchronized public void removeGroup(String groupKey) {
        groupStore.remove(groupKey);
        LOGGER.debug("Group '{}' removed", groupKey);
    }

    synchronized public Map<String, Object> getGroup(String groupKey) {
        Map<String, Object> output = new HashMap<>();
        output.put("name", groupKey);

        Set<String> plugs = groupStore.get(groupKey);
        if (plugs == null) {
            output.put("members", new ArrayList<>());
            return output;
        }

        List<Map<String, Object>> plugDetails = new ArrayList<>();
        for (String plug : plugs) {
            plugDetails.add(plugManager.createPlug(plug));
        }

        output.put("members", plugDetails);
        return output;
    }

    synchronized public List<Map<String, Object>> getAllGroups() {
        List<Map<String, Object>> allGroups = new ArrayList<>();
        Map<String, Set<String>> sortedGroups = new TreeMap<>(groupStore);
        for (String groupKey : sortedGroups.keySet()) {
            allGroups.add(getGroup(groupKey));
        }
        return allGroups;
    }

    synchronized public void controlGroup(String groupKey, String command) {
        Set<String> plugs = groupStore.get(groupKey);
        if (plugs != null) {
            for (String plug : plugs) {
                plugManager.controlPlug(plug, command);
                LOGGER.debug("Command '{}' sent to plug '{}' in group '{}'", command, plug, groupKey);
                try {
                    Thread.sleep(50); // Added delay to prevent MQTT publish overload
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(GroupsModel.class);
}
