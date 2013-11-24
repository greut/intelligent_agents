package logist.config;

import logist.topology.Topology;
import logist.topology.Topology.Builder;
import static logist.Measures.DISTANCE_UNITS_PER_KM;

class TopologyParser {

	private Builder builder = new Builder();

	TopologyParser() {}
	
	/**
	 * 
	 * @param topologyTag
	 * @return
	 * @throws ParserException
	 */
	Topology parseTopology(XMLTag topologyTag) throws ParserException {

		// parses cities tag
		parseCities(topologyTag.getUniqueChild("cities"));

		// parse routes tag
		parseRoutes(topologyTag.getUniqueChild("routes"));

		// create the topology
		return builder.build();
	}

	/**
	 * 
	 * @param citiesTag
	 * @throws ParserException
	 */
	private void parseCities(XMLTag citiesTag) throws ParserException {

		// parse all city tags
		for (XMLTag cityTag : citiesTag.getAllChildren("city")) {

			// parse attributes
			String name = cityTag.getAttribute("name", String.class);
			int x = cityTag.getAttribute("x", Integer.class);
			int y = cityTag.getAttribute("y", Integer.class);

			// create city
			builder.addCity(name, x, y);
		}
	}

	/**
	 * 
	 * @param routesTag
	 * @throws ParserException
	 */
	private void parseRoutes(XMLTag routesTag) throws ParserException {

		// parse all route tags
		for (XMLTag routeTag : routesTag.getAllChildren("route")) {

			// parse attributes
			String from = routeTag.getAttribute("from", String.class);
			String to = routeTag.getAttribute("to", String.class);
			double kmDistance = routeTag.getAttribute("distance", Double.class);
			long distance = (long)(kmDistance * DISTANCE_UNITS_PER_KM);
			
			// create route
			builder.addRoute(from, to, distance);
		}
	}
}
