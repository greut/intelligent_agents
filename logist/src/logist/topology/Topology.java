package logist.topology;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import logist.Measures;

/**
 * The <tt>Topology</tt> encapsulates the cities and routes in the Pickup and
 * Delivery problem.
 * 
 * <p>
 * Each {@link logist.topology.Topology.City} in the topology has a unique id
 * field in the range <tt>[0, size)</tt>. This allows to use cities as indices
 * in array- or list-based data structures, for example:
 * 
 * <pre>
 * distance[from.id][to.id]
 * </pre>
 * 
 * <p>
 * This class provides the following shortcut to iterate over all cities in the
 * topology:
 * 
 * <pre>
 * 	for (City city : topology) ...
 * </pre>
 * 
 * <p>
 * The lists returned by the <tt>cities</tt> and <tt>neighbors</tt> methods are
 * read-only and must be copied before they can be modified.
 * 
 * @author Robin Steiger
 */
public class Topology implements Iterable<Topology.City> {
    private static final long NO_ROUTE = Long.MAX_VALUE / 2;

    private final int numC;
    private final City[] cities;
    private final long[][] distance;
    private final int[][] parent;

    private final List<City> immutableCities;
    private final List<List<City>> immutableNeighbors;

    private static <T> List<T> readOnly(List<T> list) {
        return Collections.unmodifiableList(list);
    }

    private Topology(List<CityInfo> infos, Set<Route> routes) {
        this.numC = infos.size();
        this.cities = new City[numC];
        this.distance = new long[numC][numC];
        this.parent = new int[numC][numC];

        this.immutableCities = readOnly(Arrays.asList(cities));
        this.immutableNeighbors = new ArrayList<List<City>>(numC);

        // initialize data structures
        List<List<City>> neighborsList = new ArrayList<List<City>>(numC);
        for (int i = 0; i < numC; ++i) {
            cities[i] = new City(i, infos.get(i));

            List<City> neighbors = new ArrayList<City>();
            neighborsList.add(neighbors);
            immutableNeighbors.add(readOnly(neighbors));

            for (int j = 0; j < numC; ++j)
                distance[i][j] = NO_ROUTE;

            distance[i][i] = 0L;
        }

        // create routes
        for (Route route : routes) {
            distance[route.from][route.to] = route.distance;
            distance[route.to][route.from] = route.distance;

            neighborsList.get(route.from).add(cities[route.to]);
            neighborsList.get(route.to).add(cities[route.from]);
        }

        // compute pairwise paths and distances
        computeShortestPaths();

        // check whether topology is connected
        for (int i = 0; i < numC; ++i) {
            for (int j = 0; j < numC; ++j)
                if (distance[i][j] == NO_ROUTE)
                    throw new TopologyException("There is no path from '"
                            + cities[i] + "' to '" + cities[j] + "'");
        }
    }

    // Floyd-Warshall all-pairs shortest-path algorithm
    private void computeShortestPaths() {

        // initialize parent
        for (int i = 0; i < numC; ++i)
            for (int j = 0; j < numC; ++j)
                parent[i][j] = -1;

        // compute shortest paths
        for (int k = 0; k < numC; k++)
            for (int i = 0; i < numC; i++)
                for (int j = 0; j < numC; j++) {
                    long sum = distance[i][k] + distance[k][j];
                    if (distance[i][j] > sum) {
                        distance[i][j] = sum;
                        parent[i][j] = k;
                    }
                }
        
        // test
//		long max = 0;
//		for (int i = 0; i < numC; ++i)
//			for (int j = 0; j < numC; ++j)
//				if (max < distance[i][j]) max = distance[i][j];
//		
//		System.out.println("Max distance " + max);
    }

    private void buildPath(City a, City b, List<City> path) {
        // if (a.id == b.id)
        // throw new AssertionError("a = b");

        int parent_ = parent[a.id][b.id];

        if (parent_ >= 0) {
            City k = cities[parent_];
            buildPath(a, k, path);
            path.add(k);
            buildPath(k, b, path);
        }
    }

    /**
     * An iterator over all cities in the topology. The cities are returned by
     * increasing id fields.
     */
    @Override
    public Iterator<City> iterator() {
        return immutableCities.iterator();
    }

    @Override
    public String toString() {
        return Arrays.toString(cities);
    }

//	@Override
//	public boolean equals() {
//		return Arrays.toString(cities);
//	}
    
    /**
     * Returns the list of all cities in the topology.
     * <p>
     * The list must be copied before it can be modified.
     */
    public List<City> cities() {
        return immutableCities;
    }

    /**
     * the number of cities in the topology
     */
    public int size() {
        return numC;
    }

    /**
     * Returns a random city in the topology
     * 
     * @param rnd
     *            a random number generator
     */
    public City randomCity(Random rnd) {
        return cities[rnd.nextInt(cities.length)];
    }

    /**
     * Checks whether a city is contained by this topology. For system use only.
     * 
     * @param city
     *            the city to check
     */
    public boolean contains(City city) {
        return (city != null && 0 <= city.id && city.id < numC && cities[city.id] == city);
    }

    /**
     * 
     * <p>
     * This class provides the following shortcut to iterate over all neighbors
     * of this city:
     * 
     * <pre>
     * 	for (City neighbor : myCity) ...
     * </pre>
     * 
     * @author Robin Steiger
     */
    public class City implements Iterable<City> {
        public final int id;
        public final int xPos;
        public final int yPos;
        public final String name;

        private City(int id, CityInfo cityInfo) {
            this.id = id;
            this.xPos = cityInfo.xPos;
            this.yPos = cityInfo.yPos;
            this.name = cityInfo.name;
        }

         @Override
         public boolean equals(Object that) {
             if (!(that instanceof City)) 
                 return false;
             City city = (City) that;
             return (this.id == city.id) && contains(city);
         }
        
         @Override
         public int hashCode() {
             int hash = 5581;
             hash = 33*hash + id;
             hash = 33*hash + xPos;
             hash = 33*hash + yPos;
             hash = 33*hash + name.hashCode();
             return hash;
         }

        @Override
        public String toString() {
            return name;
        }

        /**
         * Returns the list of all neighbors.
         */
        public List<City> neighbors() {
            return immutableNeighbors.get(id);
        }

        /**
         * Determines whether another city is a neighbor of this city
         * 
         * @param city
         */
        public boolean hasNeighbor(City city) {
            return neighbors().contains(city);
        }

        /**
         * An iterator over all neighboring cities of this city. No specific
         * order is guaranteed.
         */
        @Override
        public Iterator<City> iterator() {
            return neighbors().iterator();
        }

        /**
         * Returns the distance in 'units' from this city to another city. If
         * the two cities are not directly connected by a route then the length
         * of the shortest path is returned.
         * <p>
         * This is the preferred unit of measurement for the simulation.
         * 
         * @see Measures
         */
        public long distanceUnitsTo(City to) {
            return distance[id][to.id];
        }

        /**
         * Returns the distance in kilometers from this city to another city. If
         * the two cities are not directly connected by a route then the length
         * of the shortest path is returned.
         */
        public double distanceTo(City to) {
            return Measures.unitsToKM(distance[id][to.id]);
        }

        /**
         * Returns the list of cities on the shortest path from this city to
         * another city.
         * <p>
         * The list does not include the first city on the path, but it does
         * include the last city. I.e. if the shortest path from A to D is A ->
         * B -> C -> D then the list [B,C,D] is returned.
         * 
         * @param to
         *            the destination
         */
        public List<City> pathTo(City to) {
            List<City> path = new ArrayList<City>();

            if (id != to.id) {
                buildPath(this, to, path);
                path.add(to);
            }
            return path;
        }

        /**
         * Returns a random neighbor of this city.
         * 
         * @param rnd
         *            a random number generator
         */
        public City randomNeighbor(Random rnd) {
            List<City> neighbors = neighbors();
            return neighbors.get(rnd.nextInt(neighbors.size()));
        }

        // public Topology getTopology() {
        // return Topology.this;
        // }
    }

    /* Builder */
    private static class CityInfo {
        final String name;
        final int xPos, yPos;

        CityInfo(String name, int xPos, int yPos) {
            this.name = name;
            this.xPos = xPos;
            this.yPos = yPos;
        }
    }

    private static class Route {
        final Integer from, to;
        final long distance;

        Route(Integer from, Integer to, long distance) {
            this.from = from;
            this.to = to;
            this.distance = distance;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Route))
                return false;
            Route r = (Route) o;
            return (from == r.from && to == r.to)
                    || (from == r.to && to == r.from);
        }

        @Override
        public int hashCode() {
            return from.hashCode() ^ to.hashCode();
        }
    }

    /**
     * A builder class to facilitate the construction of a topology.
     * 
     * @author Robin Steiger
     */
    public static class Builder {
        private Map<String, Integer> idMap = new HashMap<String, Integer>();
        private List<CityInfo> infos = new ArrayList<CityInfo>();
        private Set<Route> routes = new HashSet<Route>();

        public void addCity(String name, int xPos, int yPos) {
            if (idMap.containsKey(name))
                throw new TopologyException("Duplicate city '" + name + "'");

            CityInfo info = new CityInfo(name, xPos, yPos);
            idMap.put(name, infos.size());
            infos.add(info);
        }

        public void addRoute(String from, String to, long distance) {
            Route route = new Route(idMap.get(from), idMap.get(to), distance);
            if (route.from == null)
                throw new TopologyException("Unknown city '" + from + "'");
            if (route.to == null)
                throw new TopologyException("Unknown city '" + to + "'");
            if (routes.contains(route))
                throw new TopologyException("Duplicate route '" + from
                        + "' <--> '" + to + "'");

            routes.add(route);
        }

        public Topology build() {
            return new Topology(infos, routes);
        }
    }

    /**
     * Find a city by name
     * @param name The name of the city
     * @return The city object
     * @throws IllegalArgumentException if no city with that name exists
     */
    public City parseCity(String name) {
        for (City city : this)
            if (city.name.equals(name))
                return city;
        
        throw new IllegalArgumentException("'" + name + "' is not a city in the current topology");
    }
}
