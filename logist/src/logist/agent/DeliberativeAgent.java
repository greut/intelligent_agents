package logist.agent;

import java.util.Iterator;
import java.util.concurrent.Callable;

import logist.behavior.DeliberativeBehavior;

import logist.LogistException;
import logist.LogistSettings.TimeoutKey;
import logist.simulation.Company;
import logist.simulation.Context;
import logist.simulation.VehicleImpl;
import logist.simulation.Vehicle;
import logist.plan.Action;
import logist.plan.Plan;
import logist.plan.PlanVerifier;
import logist.task.Task;
import logist.task.TaskSet;

class DeliberativeAgent extends AgentImpl {

	private final Class<? extends DeliberativeBehavior> behaviorClass;

	private DeliberativeBehavior behavior;
	private Iterator<Action> plan = null;
	private Vehicle vehicleInfo;

	public DeliberativeAgent(String name,
			Class<? extends DeliberativeBehavior> behaviorClass) {
		super(name);
		this.behaviorClass = behaviorClass;
		// this.vehicle = null;
	}

	/* Agent */

	@Override
	public void setup(int id, final Context sim, Company company) {
		super.setup(id, sim, company);

		vehicleInfo = vehicles.get(0).getInfo();
		for (VehicleImpl vehicle : vehicles)
			vehicle.setController(this);

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

		if (plan == null) {

			if (tasks.isEmpty() && vehicleInfo.getCurrentTasks().isEmpty()) {
				plan = new EmptyIterator<Action>();
			} else {
				Plan plan = TimeoutGuard.schedule(name, TimeoutKey.PLAN,
						new Callable<Plan>() {
							@Override
							public Plan call() throws Exception {

								Plan newPlan = behavior.plan(vehicleInfo,
										agentInfo.getTasks());

								// prevent modification
								return newPlan.seal();
							}
						});

				PlanVerifier verifier = new PlanVerifier(sim.getTopology(),
						tasks);
				verifier.verifyPlan(vehicleInfo, plan);
				verifier.verifyDelivery();
				verifier.verifyPickup();
				this.plan = plan.iterator();
			}
		}
		return plan.hasNext() ? plan.next() : null;
	}

	@Override
	public void stuckAction(int vid, Action action) {
		final TaskSet carriedTasks = vehicleInfo.getCurrentTasks();
		System.out.println("LP: Stuck action : " + action);
		System.out.println("LP: Tasks        : " + carriedTasks);
		
		TimeoutGuard.schedule(name, TimeoutKey.PLAN,
				new Runnable() {
					@Override
					public void run() {
						behavior.planCancelled(carriedTasks);
					}
				});

		// just delete the plan
		this.plan = null;
	}

	@Override
	public Long askBid(Task task) {
		return null;
	}

	@Override
	public Type type() {
		return Type.DELIBERATIVE;
	}

}
