package ece448.iot_hub;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class GroupsModel {
    private static final Logger LOGGER = LoggerFactory.getLogger(GroupsModel.class);

    private final PlugsModel plugManager;
    private final Map<String, Set<String>> groupStore = new HashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();
    private final Path persistencePath;

    /** 
     * Constructor used by Spring: autowires both PlugsModel and the JSON filename.
     */
    @Autowired
    public GroupsModel(PlugsModel plugManager,
                       @Value("${persistence.file:group.json}") String fileName) {
        this.plugManager    = plugManager;
        this.persistencePath = Paths.get(fileName);
        LOGGER.info("Using persistence file: {}", persistencePath);
    }

    /**
     * Legacy constructor for existing tests—they’ll still work and default to "group.json".
     */
    public GroupsModel(PlugsModel plugManager) {
        this(plugManager, "group.json");
    }

    @PostConstruct
    private void loadFromDisk() {
        File file = persistencePath.toFile();
        if (!file.exists()) {
            LOGGER.info("No existing persistence file {}, starting fresh", persistencePath);
            return;
        }
        try {
            Map<String, Set<String>> loaded = mapper.readValue(
                file, new TypeReference<Map<String, Set<String>>>() {}
            );
            groupStore.clear();
            groupStore.putAll(loaded);
            LOGGER.info("Loaded {} groups from {}", groupStore.size(), persistencePath);
        } catch (IOException e) {
            LOGGER.error("Failed to load groups from {}", persistencePath, e);
        }
    }

    public synchronized void createOrUpdateGroup(String groupKey, List<String> plugIds) {
        groupStore.put(groupKey, new HashSet<>(plugIds));
        LOGGER.debug("Group '{}' assigned {} plug(s)", groupKey, plugIds.size());
        saveToDisk();
    }

    public synchronized void removeGroup(String groupKey) {
        groupStore.remove(groupKey);
        LOGGER.debug("Group '{}' removed", groupKey);
        saveToDisk();
    }

    public synchronized Map<String, Object> getGroup(String groupKey) {
        Map<String, Object> output = new HashMap<>();
        output.put("name", groupKey);

        Set<String> plugs = groupStore.get(groupKey);
        if (plugs == null) {
            output.put("members", Collections.emptyList());
            return output;
        }

        List<Map<String, Object>> plugDetails = new ArrayList<>();
        for (String plug : plugs) {
            plugDetails.add(plugManager.createPlug(plug));
        }
        output.put("members", plugDetails);
        return output;
    }

    public synchronized List<Map<String, Object>> getAllGroups() {
        return groupStore.keySet().stream()
                         .sorted()
                         .map(this::getGroup)
                         .collect(Collectors.toList());
    }

    public synchronized void controlGroup(String groupKey, String command) {
        Set<String> plugs = groupStore.get(groupKey);
        if (plugs != null) {
            for (String plug : plugs) {
                plugManager.controlPlug(plug, command);
                LOGGER.debug("Command '{}' sent to plug '{}' in group '{}'",
                             command, plug, groupKey);
                try {
                    Thread.sleep(50); // throttle to avoid MQTT overload
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private void saveToDisk() {
        try {
            mapper.writerWithDefaultPrettyPrinter()
                  .writeValue(persistencePath.toFile(), groupStore);
            LOGGER.info("Persisted {} groups to {}", groupStore.size(), persistencePath);
        } catch (IOException e) {
            LOGGER.error("Failed to write groups to {}", persistencePath, e);
        }
    }
}
