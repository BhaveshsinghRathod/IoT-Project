package ece448.iot_hub;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PlugsResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlugsResource.class);
    private final PlugsModel plugsModel;

    public PlugsResource(PlugsModel plugsModel) {
        this.plugsModel = plugsModel;
    }

    /**
     * Handles requests to retrieve a single plug's details.
     * Optionally performs an action (e.g., "on", "off") on the plug.
     *
     * @param plug   The name of the plug.
     * @param action The optional action to perform on the plug.
     * @return A map representing the plug's details.
     */
    @GetMapping("/api/plugs/{plug:.+}")
    public Object getPlug(
        @PathVariable("plug") String plug,
        @RequestParam(value = "action", required = false) String action) {

        if (action != null) {
            // Perform the specified action on the plug
            plugsModel.controlPlug(plug, action);
            LOGGER.info("Performed action '{}' on plug '{}'", action, plug);
        }

        Map<String, Object> plugDetails = plugsModel.createPlug(plug);
        LOGGER.info("Retrieved details for plug '{}': {}", plug, plugDetails);
        return plugDetails;
    }

    /**
     * Handles requests to retrieve details of all plugs.
     *
     * @return A list of maps, each representing a plug's details.
     */
    @GetMapping("/api/plugs")
    public Object getAllPlugs() {
        List<Map<String, Object>> allPlugs = plugsModel.getAllPlugs();
        LOGGER.info("Retrieved details for all plugs: {}", allPlugs);
        return allPlugs;
    }
}
