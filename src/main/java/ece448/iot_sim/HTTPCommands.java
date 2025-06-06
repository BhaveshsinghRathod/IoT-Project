package ece448.iot_sim;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ece448.iot_sim.http_server.RequestHandler;

public class HTTPCommands implements RequestHandler {

	// Use a map so we can search plugs by name.
	private final TreeMap<String, PlugSim> plugs = new TreeMap<>();

	public HTTPCommands(List<PlugSim> plugs) {
		for (PlugSim plug: plugs)
		{
			this.plugs.put(plug.getName(), plug);
		}
	}

	@Override
	public String handleGet(String path, Map<String, String> params) {
		// list all: /
		// do switch: /plugName?action=on|off|toggle
		// just report: /plugName

		logger.info("HTTPCmd {}: {}", path, params);

		if (path.equals("/"))
		{
			return listPlugs();
		}

		PlugSim plug = plugs.get(path.substring(1));
		if (plug == null)
			return null; // no such plug

		String action = params.get("action");
		if (action == null)
			return report(plug);

		// P2: add your code here, modify the next line if necessary
		if ("on".equals(action)){
			plug.switchOn();
		} else if ("off".equals(action)) {
			plug.switchOff();
		} else if ("toggle".equals(action)) {
			plug.toggle();
		} else {
			return "<html><body><p>Invalid action</p></body></html>";
		}
		return report(plug);
		//return "<html><body></body></html>";
	}

	protected String listPlugs() {
		StringBuilder sb = new StringBuilder();

		sb.append("<html><body>");
		for (String plugName: plugs.keySet())
		{
			sb.append(String.format("<p><a href='/%s'>%s</a></p>",
				plugName, plugName));
		}
		sb.append("</body></html>");

		return sb.toString();
	}

	protected String report(PlugSim plug) {
		String name = plug.getName();
		return String.format("<html><body>"
			+"<p>Plug %s is %s.</p>"
			+"<p>Power reading is %.3f.</p>"
			+"<p><a href='/%s?action=on'>Switch On</a></p>"
			+"<p><a href='/%s?action=off'>Switch Off</a></p>"
			+"<p><a href='/%s?action=toggle'>Toggle</a></p>"
			+"</body></html>",
			name,
			plug.isOn()? "on": "off",
			plug.getPower(), name, name, name);
	}

	private static final Logger logger = LoggerFactory.getLogger(HTTPCommands.class);
}
