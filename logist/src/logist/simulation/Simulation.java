package logist.simulation;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import logist.agent.AgentImpl;

import uchicago.src.sim.engine.SimInit;

import logist.LogistSettings;
import logist.gui.SimulationModel;
import logist.history.Event;
import logist.history.History;
import logist.plan.Action;
import logist.topology.Topology;

/**
 * Simulates the actions of all agents in one round. In GUI mode the simulation
 * is drawn on the screen.
 * 
 * @author Robin Steiger
 */
public class Simulation {

    private final Context sim;
    private final SimulationModel model;
    private final History history;

    private final PriorityQueue<VehicleImpl> queue;
    private final List<VehicleImpl> allVehicles;
    private final List<VehicleImpl> activeVehicles;

    private long simulationTime;
    private int eventCounter;

    public Simulation(Context sim, List<AgentImpl> agents, History history) {
        this.history = history;
        this.sim = sim;

        this.simulationTime = 0;
        this.eventCounter = 0;
        this.model = new SimulationModel(this);
        this.queue = new PriorityQueue<VehicleImpl>();
        this.allVehicles = new ArrayList<VehicleImpl>();
        for (AgentImpl agent : agents)
            allVehicles.addAll(agent.getVehicles());
        this.activeVehicles = new ArrayList<VehicleImpl>(allVehicles.size());
    }

    private void reset() {

        simulationTime = 0;
        eventCounter = 0;
        queue.clear();
        activeVehicles.clear();
        activeVehicles.addAll(allVehicles);
    }

    public synchronized void run(boolean gui) {
        reset();
        
        if (gui) {
            // initializes repast
            SimInit init = new SimInit();

            // loads the repast model
            init.loadModel(model, null, false);
            
            //TODO termination ?
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            while (!isRoundFinished())
                step(1000000000L);
            
            close();
        }

    }
    
    public synchronized void close() {
        sim.close();
        notifyAll();
    }

    public boolean isRoundFinished() {
        return activeVehicles.isEmpty();
    }

    public void step(long nanos) {
//		System.out.println("step " +nanos);
        
        // move vehicles
        queue.clear();
        for (VehicleImpl vehicle : activeVehicles) {
            vehicle.step(nanos);
            if (vehicle.hasNextAction())
                queue.add(vehicle);
        }
        simulationTime += nanos;

        // execute actions
        while (!queue.isEmpty()) {
            VehicleImpl vehicle = queue.poll();
            Action action = vehicle.executeNextAction();

            if (action == null) {
                // a null action terminates the round for this vehicle
                activeVehicles.remove(vehicle);
            } else {
                // time-stamp action and add it to history
                long time = simulationTime - vehicle.getUnusedNanos();
                Event event = Event.fromAction(time, vehicle.getInfo(), action);

                // pastEvents.add(event);
                history.addEvent(eventCounter++, event);

                vehicle.step(0);
                if (vehicle.hasNextAction())
                    queue.add(vehicle);
            }
        }
    }

    public List<VehicleImpl> getVehicles() {
        return allVehicles;
    }

    //
    // public TaskDistribution getTaskDistribution() {
    // return taskDistribution;
    // }
    //
    // public Task createTask(City from) {
    // return taskDistribution.createTask(from);
    // }
    //
    // public TaskSet createTaskSet(int size) {
    // return taskDistribution.createTaskSet(size);
    // }

    public LogistSettings getSettings() {
        return sim.getSettings();
    }

    public Topology getTopology() {
        return sim.getTopology();
    }

    public Context getContext() {
        return sim;
    }
}
