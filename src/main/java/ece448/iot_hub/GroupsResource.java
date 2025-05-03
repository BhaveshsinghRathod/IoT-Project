package ece448.iot_hub;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
public class GroupsResource {

    private final GroupsModel modelRef;

    public GroupsResource(GroupsModel modelRef) {
        this.modelRef = modelRef;
        LOGGER.info("GroupsResource has been initialized");
    }

    @PostMapping("/api/groups/{group:.+}")
    public Object createGroup(
            @PathVariable("group") String group,
            @RequestBody List<String> plugs) {

        modelRef.createOrUpdateGroup(group, plugs);
        LOGGER.info("Group '{}' created or updated with plugs {}", group, plugs);
        return modelRef.getGroup(group);
    }

    @DeleteMapping("/api/groups/{group:.+}")
    public Object removeGroup(@PathVariable("group") String group) {
        modelRef.removeGroup(group);
        LOGGER.info("Group '{}' has been removed", group);
        return "{}";
    }

    @GetMapping("/api/groups/{group:.+}")
    public Object getGroup(
            @PathVariable("group") String group,
            @RequestParam(value = "action", required = false) String action) {

        if (action != null) {
            modelRef.controlGroup(group, action);
            LOGGER.info("Action '{}' triggered on group '{}'", action, group);
        }

        Map<String, Object> response = modelRef.getGroup(group);
        LOGGER.info("Group '{}' fetched with response {}", group, response);
        return response;
    }

    @GetMapping("/api/groups")
    public Object getAllGroups() {
        List<Map<String, Object>> all = modelRef.getAllGroups();
        LOGGER.info("Fetched all groups: {}", all);
        return all;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(GroupsResource.class);
}
