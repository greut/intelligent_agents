package logist;

/**
 * Defines unit conversions between external values from XML files (e.g.
 * kilometers) and internal representations (e.g. distance units). The platform
 * avoids the use of floating point number to increase precision and avoid
 * non-determinism.
 * 
 * @author Robin Steiger
 */
public class Measures {

    /**
     * The number of distance units in a kilometer. A distance unit is the
     * smallest distance that a vehicle can travel.
     */
    public static final long DISTANCE_UNITS_PER_KM = 1000L;

    /**
     * The time (in nanos) that it takes to animate one hour of simulation (in
     * GUI mode).
     */
    public static final long NANOS_PER_SIM_HOUR = 1000000000L;

    /**
     * Convert kilometers to distance units.
     * 
     * @param kilometers
     *            the distance in km
     */
    public static long kmToUnits(double kilometers) {
        return (long) (kilometers * DISTANCE_UNITS_PER_KM);
    }

    /**
     * Convert distance units to kilometers.
     * 
     * @param units
     *            the distance in units
     */
    public static double unitsToKM(long units) {
        return units / (double) DISTANCE_UNITS_PER_KM;
    }

}
