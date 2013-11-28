package logist.agent;

import java.util.Iterator;
import java.util.concurrent.Callable;

import logist.behavior.ReactiveBehavior;

import logist.LogistException; //import logist.agent.Agent;
//import logist.agent.Simulation;
//import logist.agent.Vehicle;
//import logist.agent.VehicleController;
//import logist.agent.Vehicle;
import logist.LogistSettings.TimeoutKey;
import logist.simulation.Company;
import logist.simulation.Context;
import logist.simulation.VehicleImpl;
import logist.simulation.Vehicle;
import logist.plan.Action;
import logist.plan.ActionHandler;
import logist.plan.Plan;
import logist.plan.PlanVerifier;
import logist.task.DefaultTaskDistribution;
import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology.City;

class ReactiveAgent extends AgentImpl implements ActionHandler<Plan> {

    private final Class<? extends ReactiveBehavior> behaviorClass;

    private ReactiveBehavior behavior;
    private VehicleImpl vehicle;
    private Vehicle vehicleInfo;
    private Iterator<Action> plan = new EmptyIterator<Action>();

    public ReactiveAgent(String name,
            Class<? extends ReactiveBehavior> behaviorClass) {
        super(name);
        this.behaviorClass = behaviorClass;
    }

    /* Agent */

    @Override
    public void setup(int id, final Context sim, Company company) {
        super.setup(id, sim, company);

        for (VehicleImpl myvehicle : vehicles)
            myvehicle.setController(this);
        vehicle = vehicles.get(0);
        vehicleInfo = vehicle.getInfo();

        TimeoutGuard.schedule(name, TimeoutKey.SETUP, new Runnable() {
            @Override
            public void run() {
                try {
                    behavior = behaviorClass.newInstance();
                } catch (InstantiationException iEx) {
                    throw new LogistException("Could not create '"
                            + behaviorClass + "'", iEx);
                } catch (IllegalAccessException iaEx) {
                    throw new LogistException("Could not create '"
                            + behaviorClass + "'", iaEx);
                }
                behavior.setup(sim.getTopology(), sim.getTaskDistribution(),
                        agentInfo);
            }
        });
    }

    /* VehicleController */

    @Override
    public Action nextAction(int vid) {
        
        if (vid != 0)
            return null;
        
        if (!plan.hasNext()) {
            final Task task = ((DefaultTaskDistribution) sim
                    .getTaskDistribution())
                    .createTask(vehicleInfo.getCurrentCity());
            
            TaskSet tasks = TaskSet.create((task == null) ? new Task[] {}
                    : new Task[] { task });
            vehicle.setTasks(tasks);

            Plan plan = TimeoutGuard.schedule(name, TimeoutKey.PLAN,
                    new Callable<Plan>() {
                        @Override
                        public Plan call() throws Exception {

                            return behavior.act(vehicleInfo, task).accept(
                                    ReactiveAgent.this);
                        }
                    });

            PlanVerifier verifier = new PlanVerifier(sim.getTopology(), tasks);
            verifier.verifyPlan(vehicleInfo, plan);
            verifier.verifyDelivery();

            // We must NOT verify pickup in this mode !
            // verifier.verifyPickup();

            this.plan = plan.iterator();
        }
        return plan.next();
    }

    @Override
    public void stuckAction(int vid, Action action) {
        // should not happen
        System.out.println("Stuck action " + action);
    }

    @Override
    public Long askBid(Task task) {
        return null;
    }

    /* ActionHandler<Plan> */

    @Override
    public Plan moveTo(City target) {
        Plan plan = new Plan(vehicleInfo.getCurrentCity());
        plan.appendMove(target);
        return plan.seal();
    }

    @Override
    public Plan pickup(Task task) {
        return deliver(task);
    }

    @Override
    public Plan deliver(Task task) {
        Plan plan = new Plan(vehicleInfo.getCurrentCity());
        plan.appendPickup(task);
        for (City city : task.path())
            plan.appendMove(city);
        plan.appendDelivery(task);
        return plan.seal();
    }

    @Override
    public Type type() {
        return Type.REACTIVE;
    }
    
    @Override
    public void notifyDelivery(Task task) {
        // Do NOT notify manager
        //sim.notifyDelivery(task);
    }

    @Override
    public void notifyPickup(Task task) {
        // Do NOT notify manager
        //sim.notifyPickup(task);
    }
}
