package logist.gui;

import java.awt.Dimension;

import logist.LogistSettings;
import logist.LogistSettings.ColorKey;
import logist.simulation.Simulation;
import logist.simulation.VehicleImpl;

import uchicago.src.reflector.RangePropertyDescriptor;
import uchicago.src.sim.analysis.DataSource;
import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.analysis.Sequence;
import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimEvent;
import uchicago.src.sim.engine.SimEventListener;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.gui.DisplaySurface;

/**
 * The repast model that creates the GUI.
 * 
 * @author Robin Steiger
 */
public class SimulationModel extends SimModelImpl implements SimEventListener {

	private Schedule schedule;
	private DisplaySurface display;
	private OpenSequenceGraph graph;

	private long previousNanoTime;
	private long lastGraphStep;
	private Simulation simulation;
	private boolean paused;
	private boolean first;
	private int simSpeed;

	public SimulationModel(Simulation simulation) {
		this.simulation = simulation;
		setSimSpeed(1);
	}

	/**
	 * Prepare the model for a new run by building the separate elements that
	 * make up the model.
	 */
	@Override
	public void begin() {
		// System.out.println("Begin");

		// registers this class
		this.addSimEventListener(this);

		buildModel();
		buildSchedule();
		buildDisplay();

		this.previousNanoTime = System.nanoTime();
		this.paused = true;
		this.first = true;
	}

	@Override
	public String[] getInitParam() {
		return new String[] { "SimSpeed" };
	}

	@Override
	public String getName() {
		return "EPFL-LIA Logist Platform";
	}

	@Override
	public Schedule getSchedule() {
		return schedule;
	}

	/**
	 * Tear down any existing pieces of the model and prepare for a new run.
	 */
	@Override
	public void setup() {
		this.schedule = new Schedule();

		// Tear down Displays
		if (display != null) {
			display.dispose();
			display = null;
		}

		// creates the default display surface and registers it
		display = new DisplaySurface(this, "PICKUP AND DELIVERY SIMULATION");
		registerDisplaySurface("PICKUP AND DELIVERY SIMULATION", display);

		// Tear down graph
		if (graph != null)
			graph.dispose();
		graph = null;

		// Create graph
		graph = new OpenSequenceGraph("PICKUP AND DELIVERY GRAPH", this);
		graph.setAxisTitles("time", "reward per km");
		graph.setXRange(0, 1000);
		graph.setYRange(0, 100);

		addSlider("SimSpeed", 1, 100, 10);
	}

	@SuppressWarnings("unchecked")
	private void addSlider(String name, int min, int max, int step) {
		descriptors
				.put(name, new RangePropertyDescriptor(name, min, max, step));
	}

	/**
	 * Initialize the basic model by creating the space and populating it with
	 * grass and rabbits.
	 */
	private void buildModel() {
		paused = true;
	}

	/**
	 * Create the schedule object(s) that will be executed during the running of
	 * the model
	 */
	private void buildSchedule() {

		/*
		 * Schedules the execution of the specified method on the specified
		 * object to start at the specified clock tick and continue every tick
		 * thereafter.
		 */
		schedule.scheduleActionBeginning(2, new BasicAction() {
			@Override
			public void execute() {
				if (simulation.isRoundFinished()) {
					// TODO invoke manager
					// simulation.beginRound(1);
//					simulation.close();
//					System.exit(0);
				}

				if (paused) {
					paused = false;
					previousNanoTime = System.nanoTime();

					if (first)
						first = false;
					else
						simulation.step(1000000000 * simSpeed);
				} else {
					long nanoTime = System.nanoTime();
					long nanoDelta = nanoTime - previousNanoTime;

					simulation.step(nanoDelta * simSpeed);
					
					previousNanoTime = System.nanoTime();
					
					lastGraphStep += nanoDelta;
					if (lastGraphStep > 1000000000) {
						lastGraphStep = 0;
						graph.step();
					}
				}
				display.updateDisplay();
			}
		});
	}

	/**
	 * Build the display elements for this model.
	 */
	private void buildDisplay() {

		// our custom network display for accelerating the rendering
		NetworkDisplay networkDisplay = new NetworkDisplay(simulation);
		Dimension displaySize = networkDisplay.getSize();

		// there's only one top layer
		display.addDisplayable(networkDisplay, "Topology");
		display.setDoubleBuffered(true);
		display.setMinimumSize(displaySize);
		display.setMaximumSize(displaySize);
		display.setPreferredSize(displaySize);

		// default colors
		LogistSettings settings = simulation.getSettings();
		display.setBackground(settings.get(ColorKey.BACKGROUND));
		display.setForeground(settings.get(ColorKey.FOREGROUND));

		// Add graph series
		for (final VehicleImpl vehicle : simulation.getVehicles()) {
			graph.addSequence(vehicle.getInfo().name(),
					new DataSourceSequence() {
						public double getSValue() {
							return vehicle.getRewardRatio();
						}
					},
					vehicle.getInfo().color());
		}

		// create the legend
		// if ( mGlobals.ShowLegend ) {
		// mDisplay.createLegend( "Vehicles" );
		//			
		// for ( AgentProfile ap : mAgentMgr.getProfileList() ) {
		// if ( ap.getDisplayable() != null ) {
		// mDisplay.addLegendLabel( ap.getName(), 0,
		// ap.getDisplayable().getColor(), false );
		// } else {
		// mDisplay.addLegendLabel( ap.getName(), 1, Color.BLACK, false );
		// }
		// }
		// }

		// show the display and graph
		display.display();
		graph.display();

	}

	/*****/

	@Override
	public void simEventPerformed(SimEvent event) {

		switch (event.getId()) {

		case SimEvent.PAUSE_EVENT:
			paused = true;
			break;

		case SimEvent.STOP_EVENT:
		case SimEvent.END_EVENT:

			// destroying all services
			// this.destroyAllServices();

			// flushing all entries in the log manager
			// mLogMgr.log( LogManager.DEFAULT, LogSeverityEnum.LSV_INFO,
			// "Flushing entries in " + "the log management subsystem..." );

			// simulation ended here...
			// mLogMgr.log( LogManager.DEFAULT, LogSeverityEnum.LSV_INFO,
			// "Simulation ended successfully..." );
			// flushing all entries

			// mLogMgr.flush( LogManager.DEFAULT );
			// mLogMgr.flush( LogManager.OUT );

			// shut everything down
			// mLogMgr.shutdown();

			// system goes by
			simulation.close();
			System.exit(0);
			
			// System.err.println("Call system exit now ?");
			break;

		default:
			break;
		}
	}

	// Used to create an anonymous class that implements both interfaces
	private abstract class DataSourceSequence implements DataSource, Sequence {
		public Object execute() {
			return new Double(getSValue());
		}
	}

	public int getSimSpeed() {
		return simSpeed;
	}

	public void setSimSpeed(int speed) {
		simSpeed = speed;

		// System.out.println("Set sim speed to " + simSpeed);
		// System.out.println("New millis are   " + Measures.MILLIS_PER_SIM_HOUR
		// );
	}
}
