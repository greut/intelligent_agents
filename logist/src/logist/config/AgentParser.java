package logist.config;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import logist.agent.AgentImpl;


class AgentParser {

	private List<Entry> entries;
	
	AgentParser() {
	}

	List<String> parseNames(XMLTag agentsTag) throws ParserException {
		
		parseAgent(agentsTag.getAllChildren("agent"));
		
		List<String> agentNames = new ArrayList<String>(entries.size());
		for (Entry entry : entries)
			agentNames.add(entry.agentName);
		
		return agentNames;
	}
	
	List<AgentImpl> parseAgents(XMLTag agentsTag, String[] names) throws ParserException {
		
		parseAgent(agentsTag.getAllChildren("agent"));
		
		List<AgentImpl> agents = new ArrayList<AgentImpl>(names.length);
		for (String name : names) {
			AgentImpl agent = null;
			for (Entry entry : entries) {
				// Does the name match ?
				if (!name.equals(entry.agentName))
					continue;

				// Duplicate entry ?
				if (agent != null)
					throw ParserException.duplicate("agent", name);

				// Load class, but don't initialize it
				ClassLoader loader = entry.getClassLoader();
				Class<?> clazz;
				try {
					clazz = Class.forName(entry.className, false, loader);
				} catch (ClassNotFoundException cnfEx) {
					throw new ParserException("Cannot find class '"
							+ entry.className + "' in class-path '"
							+ entry.classPath + "'", cnfEx);
				}

				// resolve interface
				agent = AgentImpl.forClass(name, entry.map, clazz);
				if (agent == null)
					throw ParserException.badClass(clazz, "behavior");
				
				agents.add(agent);
			}
			if (agent == null)
				throw ParserException.missing("agent '" + name + "'");
		}
		return agents;
	}

	private void parseAgent(List<XMLTag> agentTags)
			throws ParserException {

		Entry entry;
		this.entries = new ArrayList<Entry>(agentTags.size());

		for (XMLTag agentTag : agentTags) {
			entries.add(entry = new Entry());

			entry.map = Parsers.parseMap(agentTag);

			entry.agentName = agentTag.getAttribute("name", String.class);
			entry.className = XMLTag.convert(entry.map, "class-name", agentTag,
					String.class, null);
			entry.classPath = XMLTag.convert(entry.map, "class-path", agentTag,
					String.class, "");
		}
	}

	private static class Entry {
		String agentName, classPath, className;
		Map<String, String> map;

		ClassLoader getClassLoader() throws ParserException {
			if (classPath.isEmpty())
				return AgentParser.class.getClassLoader();

			String[] paths = classPath.split(";");
			URL[] urls = new URL[paths.length];

			for (int i = 0; i < paths.length; i++) {
				try {
					File file = new File(paths[i]);
					urls[i] = file.toURI().toURL();

					if (!file.exists())
						throw new ParserException("Class-path '" + paths[i]
								+ "' does not exist");

				} catch (MalformedURLException muEx) {
					throw new ParserException("Invalid class-path '" + paths[i]
							+ "'", muEx);
				}
			}
			return new URLClassLoader(urls);
		}
	}
}
