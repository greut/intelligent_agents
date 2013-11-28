package logist.simulation;

import java.util.List;

/**
 * A company that was defined in the XML configuration.
 * 
 * @author Robin Steiger
 */
public class Company {

    public final String name;
    public final List<VehicleImpl> vehicles;
    
    public Company(String name, List<VehicleImpl> vehicles) {
        this.name = name;
        this.vehicles = vehicles;
    }
}
