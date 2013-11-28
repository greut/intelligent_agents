package logist.config;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import logist.agent.AgentImpl;
import logist.agent.AgentStatistics;

import logist.LogistSettings;
import logist.simulation.Manager;
import logist.topology.Topology;

/**
 * Parsers for the various XML configuration files.
 * 
 * @author Robin Steiger
 */
public class Parsers {

    /* configuration parser */

    public static Manager parseConfiguration(String configFile, String agentsFile, String[] names)
            throws ParserException {
        return parseConfiguration(XMLTag.loadXMLFromFile(configFile), agentsFile, names);
    }

    static Manager parseConfiguration(XMLTag configTag, String agentsFile, String[] names)
            throws ParserException {
        return new ConfigParser().parse(configTag, agentsFile, names);
    }

    /* topology parser */

    public static Topology parseTopology(String topologyFile)
            throws ParserException {
        return parseTopology(XMLTag.loadXMLFromFile(topologyFile));
    }

    static Topology parseTopology(XMLTag topologyTag)
            throws ParserException {
        return new TopologyParser().parseTopology(topologyTag);
    }

    /* agent parser */

    public static List<AgentImpl> parseAgents(String configFile, String[] names)
            throws ParserException {
        return parseAgents(XMLTag.loadXMLFromFile(configFile), names);
    }

    static List<AgentImpl> parseAgents(XMLTag configTag, String[] names)
            throws ParserException {
        return new AgentParser().parseAgents(configTag, names);
    }

    /* agent names parser */
    
    public static List<String> parseAgents(File agentsFile)
            throws ParserException {
        return parseAgents(XMLTag.loadXMLFromFile(agentsFile));
    }

    static List<String> parseAgents(XMLTag configTag)
            throws ParserException {
        return new AgentParser().parseNames(configTag);
    }

    /* settings parser */

    public static LogistSettings parseSettings(String configFile)
            throws ParserException {
        return parseSettings(XMLTag.loadXMLFromFile(configFile));
    }

    static LogistSettings parseSettings(XMLTag configTag)
            throws ParserException {
        return new SettingsParser().parse(configTag);
    }

    /* history parser */

    public static List<AgentStatistics> parseHistory(File file)
            throws ParserException {
        return parseHistory(XMLTag.loadXMLFromFile(file));
    }

    static List<AgentStatistics> parseHistory(XMLTag historyTag)
            throws ParserException {
        return new HistoryParser().parse(historyTag);
    }

    /* helper */

    static Map<String, String> parseMap(XMLTag settings) throws ParserException {

        Map<String, String> map = new HashMap<String, String>();
        for (XMLTag set : settings.getAllChildren("set"))
            set.getAttributes(map);

        return map;
    }
}
