package logist.agent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

import logist.behavior.AuctionBehavior;

import logist.LogistException;
import logist.LogistSettings.TimeoutKey;
import logist.plan.Action;
import logist.plan.Plan;
import logist.plan.PlanVerifier;
import logist.simulation.Company;
import logist.simulation.Context;
import logist.simulation.VehicleImpl;
//import logist.simulation.VehicleController;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;

class AuctionAgent extends AgentImpl {

    // interface with legacy API
    // private final Class<? extends Behavior> legacyBehaviorClass;

    private final Class<? extends AuctionBehavior> behaviorClass;
    private AuctionBehavior behavior;

    private Task lastTask;
    private int lastWinner;
    private Long[] lastOffers;
    private List<Iterator<Action>> plans;

    // interface with legacy API
    // AuctionAgent(Class<? extends Behavior> behaviorClass, String name) {
    // super(name);
    // this.legacyBehaviorClass = behaviorClass;
    // }

    AuctionAgent(String name, Class<? extends AuctionBehavior> behaviorClass) {
        super(name);
        this.behaviorClass = behaviorClass;
    }

    @Override
    public void setup(int id, final Context sim, Company company) {
        super.setup(id, sim, company);

        for (VehicleImpl vehicle : vehicles)
            vehicle.setController(this);

        TimeoutGuard.schedule(name, TimeoutKey.SETUP, new Runnable() {
            @Override
            public void run() {
                try {
                    // if (behaviorClass != null)
                    behavior = behaviorClass.newInstance();
                    // interface with legacy API
                    // else
                    // behavior = new BehaviorAdapter(legacyBehaviorClass
                    // .newInstance());

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

    @Override
    public void beginRound(TaskSet empty) {
        super.beginRound(empty);

        lastTask = null;
        lastWinner = -1;
        lastOffers = null;
        plans = null;
    }

    @Override
    public Long askBid(final Task task) {
        return TimeoutGuard.schedule(name, TimeoutKey.BID,
                new Callable<Long>() {
                    @Override
                    public Long call() throws Exception {
                        if (lastTask != null) {
                            Long[] offers = (lastOffers == null) ? null
                                    : lastOffers.clone();
                            behavior
                                    .auctionResult(lastTask, lastWinner, offers);
                            lastTask = null;
                        }
                        return behavior.askPrice(task);
                    }
                });
    }

    @Override
    public void notifyResult(Task previous, int winner, Long[] offers) {
        super.notifyResult(previous, winner, offers);

        lastTask = previous;
        lastWinner = winner;
        lastOffers = offers;
    }

    @Override
    public Action nextAction(int vid) {
        if (plans == null) {
            final List<Vehicle> vehicleInfos = agentInfo.vehicles();

            List<Plan> planList = TimeoutGuard.schedule(name, TimeoutKey.PLAN,
                    new Callable<List<Plan>>() {
                        @Override
                        public List<Plan> call() throws Exception {
                            if (lastTask != null) {
                                Long[] offers = (lastOffers == null) ? null
                                        : lastOffers.clone();
                                behavior.auctionResult(lastTask, lastWinner,
                                        offers);
                                lastTask = null;
                            }

                            List<Plan> plans = behavior.plan(vehicleInfos, agentInfo
                                    .getTasks());

                            // prevent modification
                            plans = new ArrayList<Plan>(plans);
                            for (Plan plan : plans)
                                plan.seal();
                            return plans;
                        }
                    });

            if (planList.size() != getVehicles().size())
                throw new LogistException(
                        "The number of plans must be equal to the number of vehicles.");

            PlanVerifier verifier = new PlanVerifier(sim.getTopology(), tasks);
            for (int i = 0; i < planList.size(); i++) {
                verifier.verifyPlan(vehicleInfos.get(i), planList.get(i));
                verifier.verifyDelivery();
            }
            verifier.verifyPickup();

            plans = new ArrayList<Iterator<Action>>();
            for (Plan plan : planList)
                plans.add(plan.iterator());
        }
        Iterator<Action> plan = plans.get(vid);
        return plan.hasNext() ? plan.next() : null;
    }

    @Override
    public void stuckAction(int vid, Action action) {
        throw new AssertionError("A valid plan got stuck !");
    }

    @Override
    public Type type() {
        return Type.AUCTION;
    }

}
