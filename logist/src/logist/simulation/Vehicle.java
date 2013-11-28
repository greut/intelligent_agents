package logist.simulation;

import java.awt.Color;

import logist.task.TaskSet;
import logist.topology.Topology.City;

/**
 * Collection of useful information about a vehicle.
 * <p>
 * A <tt>get</tt>-prefix indicates that the return value of the method may
 * change over time. All other methods return constant values.
 * 
 * @author Robin Steiger
 */
public interface Vehicle {
    /**
     * A unique id for each vehicle of the same agent/company, in the range
     * <tt>[0,numVehiclesOfAgent)</tt>.
     */
    int id();

    /** The name of the vehicle */
    String name();

    /** The capacity of the vehicle */
    int capacity();

    /** The starting location of the vehicle */
    City homeCity();

    /** The speed of the vehicle */
    double speed();

    /** The cost/km of the vehicle */
    int costPerKm();

    /* Statistics / current state */

    /** The current city of the vehicle */
    City getCurrentCity();

    /**
     * The tasks that are currently being transported by the vehicle.
     * These tasks have been picked up but have not yet been delivered.
     */
    TaskSet getCurrentTasks();

    /** The sum of rewards for all delivered tasks */
    long getReward();

    /**
     * The total distance (in units) traveled by the vehicle.
     * 
     * @see logist.Measures
     */
    long getDistanceUnits();

    /** The total distance (in km) traveled by the vehicle */
    double getDistance();

    /** The color of the vehicle */
    Color color();

}
