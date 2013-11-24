package logist.config;


import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import logist.agent.AgentImpl;

import logist.LogistSettings;
import logist.Measures;
import logist.LogistSettings.SizeKey;
import logist.simulation.Company;
import logist.simulation.Manager;
import logist.simulation.VehicleImpl;
import logist.task.DefaultTaskDistribution;
import logist.task.Policy;
import logist.task.Policy.LongDistances;
import logist.task.Policy.MediumDistances;
import logist.task.Policy.Uniform;
import logist.task.Policy.ShortDistances;
import logist.topology.Topology;
import logist.topology.Topology.City;

class ConfigParser {

	private LogistSettings settings;
	private Topology topology;
	private List<AgentImpl> agents;
	private List<Company> companies;
	private DefaultTaskDistribution taskDistribution;
	private Random random;

	ConfigParser() {}
	
	Manager parse(XMLTag configTag, String agentsFile, String[] agentNames) throws ParserException {
		
//		String name = configTag.getAttribute("name", String.class);
//		System.out.println("Reading configuration " + name);
		
		parseSettings( configTag.getUniqueChild("settings") );
		parseTopology( configTag.getUniqueChild("topology") );	
		parseTasks( configTag.getUniqueChild("tasks") );
		parseCompanies( configTag.getUniqueChild("companies") );
		
		if (agentsFile == null)
			parseAgents( configTag.getUniqueChild("agents"), agentNames );
		else
			parseAgents(XMLTag.loadXMLFromFile(agentsFile), agentNames);
		
		return new Manager(settings, topology, agents, companies, taskDistribution);
	}
	
	private void parseAgents(XMLTag agentsTag, String[] names) throws ParserException {
		
		if (agentsTag.hasAttribute("import")) {
			// load from external file
			String agentsFile = agentsTag.getAttribute("import", String.class);
			agents = Parsers.parseAgents(agentsFile, names);
		} else {
			// parse directly
			agents = Parsers.parseAgents(agentsTag, names);
		}
		settings.set(SizeKey.NUMBER_OF_AGENTS, agents.size());		
	}

	private void parseSettings(XMLTag settingsTag) throws ParserException {
		
		if (settingsTag.hasAttribute("import")) {
			// load from external file
			String settingsFile = settingsTag.getAttribute("import", String.class);
			settings = Parsers.parseSettings(settingsFile);
		} else {
			// parse directly
			settings = Parsers.parseSettings(settingsTag);
		}
	}

	private void parseTopology(XMLTag topologyTag) throws ParserException {
	
		if (topologyTag.hasAttribute("import")) {
			// load from external file
			String topologyFile = topologyTag.getAttribute("import", String.class);
			topology = Parsers.parseTopology(topologyFile);
		} else {
			// parse directly
			topology = Parsers.parseTopology(topologyTag);
		}
	}
	
	private void parseCompanies(XMLTag agentsTag) throws ParserException {
		companies = new ArrayList<Company>();
		
		for (XMLTag companyTag : agentsTag.getAllChildren("company")) {
			String name = companyTag.getAttribute("name", String.class);
			List<VehicleImpl> vehicles = parseVehicles(companyTag);
			
			companies.add(new Company(name, vehicles));
		}
	}
	
	private List<VehicleImpl> parseVehicles(XMLTag companyTag) throws ParserException {
		List<VehicleImpl> vehicles = new ArrayList<VehicleImpl>();
		
		for (XMLTag vehicleTag : companyTag.getAllChildren("vehicle")) {
			String name = vehicleTag.getAttribute("name", String.class);
			
			Map<String, String> map = Parsers.parseMap(vehicleTag);
			
			// Get the capacity
//			if (!map.containsKey("capacity"))
//				throw ParserException.missingAttribute("capacity", "set (in vehicle)");
//			String className = map.get("capacity");
			
			int capacity = XMLTag.convert(map, "capacity", vehicleTag, Integer.class, null);
			long speed =  Measures.kmToUnits(XMLTag.convert(map, "speed", vehicleTag, Double.class, 120.0));
			Color color = XMLTag.convert(map, "color", vehicleTag, Color.class, null);
			String home = XMLTag.convert(map, "home", vehicleTag, String.class, null);
			int costPerKm = XMLTag.convert(map, "cost-per-km", vehicleTag, Integer.class, null);
			
			City homeCity;
			try {
				homeCity = topology.parseCity(home);
			} catch (IllegalArgumentException iaEx) {
				throw new ParserException("'" + home + "' is not a city in the current topology", iaEx);
			}
			int id = vehicles.size();
			vehicles.add(new VehicleImpl(id, name, capacity, costPerKm, homeCity, speed, color));
		}
		return vehicles;
	}

	private void parseTasks(XMLTag tasksTag) throws ParserException {
		long seed = tasksTag.getAttribute("rngSeed", Long.class);
		int number = tasksTag.getAttribute("number", Integer.class);
		
		random = new Random(seed);
		settings.set(SizeKey.NUMBER_OF_TASKS, number);
		
		double[][] p = parsePolicy(tasksTag.getUniqueChild("probability"));
		double[][] r = parsePolicy(tasksTag.getUniqueChild("reward"));
		double[][] w = parsePolicy(tasksTag.getUniqueChild("weight"));
		
		double[] n;
		if (tasksTag.hasChild("no-task")) {
			n = parseDistribution(tasksTag.getUniqueChild("no-task"));
		} else {
			n = new double[topology.size()];
		}
		
		taskDistribution = new DefaultTaskDistribution(topology, random, p, r, w, n);
	}

	private double[] parseDistribution(XMLTag policyTag) throws ParserException {

		String distributionName = policyTag.getAttribute("distribution", String.class);

		double min, max;
		if (policyTag.hasAttribute("value")) {
			min = max = policyTag.getAttribute("value", Double.class);
		} else if (policyTag.hasAttribute("min") && policyTag.hasAttribute("max")) {
			min = policyTag.getAttribute("min", Double.class);
			max = policyTag.getAttribute("max", Double.class);
		} else {
			throw ParserException.missingAttribute("value or min/max", policyTag);
		}
		
		int size = topology.size();
		if (distributionName.equals("constant")) {
			return Policy.constant(size, (min + max) / 2.0);
		} else if (distributionName.equals("uniform")) {
			return Policy.uniform(size, min, max, random);
		} else {
			throw ParserException.badFormat("distribution", policyTag, "distribution");
		}
	}

	private double[][] parsePolicy(XMLTag policyTag) throws ParserException {
		
		String distributionName = policyTag.getAttribute("distribution", String.class);
		String policyName = policyTag.hasAttribute("policy") ? policyTag.getAttribute("policy", String.class) : "none";		
		Policy policy;
		if (policyName.equals("long-distances")) {
			policy = new LongDistances(topology);
		} else if (policyName.equals("medium-distances")) {
			policy = new MediumDistances(topology);
		} else if (policyName.equals("short-distances")) {
			policy = new ShortDistances(topology);
		} else if (policyName.equals("none")) {
			policy = new Uniform(topology);
		} else {
			throw ParserException.badFormat("policy", policyTag, "policy");
		}
		
		double min, max;
		if (policyTag.hasAttribute("value")) {
			min = max = policyTag.getAttribute("value", Double.class);
		} else if (policyTag.hasAttribute("min") && policyTag.hasAttribute("max")) {
			min = policyTag.getAttribute("min", Double.class);
			max = policyTag.getAttribute("max", Double.class);
		} else {
			throw ParserException.missingAttribute("value or min/max", policyTag);
		}
		
		if (distributionName.equals("constant")) {
			return policy.constant(min, max);
		} else if (distributionName.equals("uniform")) {
			return policy.uniform(min, max, random);
		} else {
			throw ParserException.badFormat("distribution", policyTag, "distribution");
		}
		
	}
	
}
