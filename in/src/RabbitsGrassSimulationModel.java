import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;

import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.gui.ColorMap;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.Object2DDisplay;
import uchicago.src.sim.gui.Value2DDisplay;
import uchicago.src.sim.util.SimUtilities;

/**
 * Class that implements the simulation model for the rabbits grass
 * simulation.  This is the first class which needs to be setup in
 * order to run Repast simulation. It manages the entire RePast
 * environment and the simulation.
 *
 * @author Yoan Blanc <yoan.blanc@epfl.ch>
 */
public class RabbitsGrassSimulationModel extends SimModelImpl {

    private int worldWidth = 20; // 0 - 100
    private int worldHeight = 20; // 0 - 100
    private int numberOfRabbits = 150; // 0 - 500
    private int birthThreshold = 15; // 0 - 20
    private int grassGrowRate = 15; // 0 - 20
    private int grassEnergy = 5; // ?
    private int minEnergy = 10;
    private int maxEnergy = 20;

    private Schedule schedule;
    private DisplaySurface displaySurface;
    private RabbitsGrassSimulationSpace space;
    private ArrayList<RabbitsGrassSimulationAgent> rabbits;

    public void begin() {
        // build model
        space = new RabbitsGrassSimulationSpace(worldWidth, worldHeight);
        space.spreadGrass(grassGrowRate);

        for (int i=0; i<numberOfRabbits; i++) {
            addNewRabbit();
        }

        // build schedule
        class RabbitsGrassSimulationStep extends BasicAction {
            public void execute() {
                // Grass grows
                space.spreadGrass(grassGrowRate);

                // Rabbits move
                SimUtilities.shuffle(rabbits);
                int younglings = 0;
                for (RabbitsGrassSimulationAgent rabbit: rabbits) {
                    rabbit.step(space, grassEnergy);

                    if (rabbit.getEnergy() >= birthThreshold) {
                        younglings++;
                        rabbit.setEnergy(rabbit.getEnergy() / 2);
                    }
                }

                for (Iterator<RabbitsGrassSimulationAgent> iter = rabbits.iterator(); iter.hasNext(); ) {
                    RabbitsGrassSimulationAgent rabbit = iter.next();
                    if (rabbit.isDead()) {
                        space.removeRabbit(rabbit);
                        iter.remove();
                    }
                }

                for (int i=0; i<younglings; i++) {
                    addNewRabbit();
                }

                displaySurface.updateDisplay();
            }
        }

        schedule.scheduleActionBeginning(0, new RabbitsGrassSimulationStep());

        // build display
        ColorMap map = new ColorMap();
        for (int i=1; i<16; i++) {
            map.mapColor(i, new Color(0, i * 8 + 127, 0));
        }
        map.mapColor(0, Color.black);
        Value2DDisplay displayGrass = new Value2DDisplay(space.getCurrentGrassSpace(), map);
        Object2DDisplay displayRabbit = new Object2DDisplay(space.getCurrentRabbitSpace());
        displayRabbit.setObjectList(rabbits);

        displaySurface.addDisplayableProbeable(displayGrass, "Grass");
        displaySurface.addDisplayableProbeable(displayRabbit, "Rabbits");

        displaySurface.display();
    }

    private void addNewRabbit() {
        RabbitsGrassSimulationAgent rabbit = new RabbitsGrassSimulationAgent(minEnergy, maxEnergy);
        rabbits.add(rabbit);
        space.addRabbit(rabbit);
    }

    /**
     * @TODO use the parameter file: http://repast.sourceforge.net/repast_3/how-to/params.html
     */
    public String[] getInitParam() {
        return new String[]{ "GridSize",
                             "Population",
                             "BirthThreshold",
                             "GrassGrowRate" };
    }

    public int getGridSize() {
        return worldWidth;
    }

    public void setGridSize(int size) {
        worldWidth = size;
        worldHeight = size;
    }

    public int getPopulation() {
        return numberOfRabbits;
    }

    public void setPopulation(int population) {
        numberOfRabbits = population;
    }

    public int getBirthThreshold() {
        return birthThreshold;
    }

    public void setBirthThreshold(int threshold) {
        birthThreshold = threshold;
    }

    public int getGrassGrowRate() {
        return grassGrowRate;
    }

    public void setGrassGrowRate(int rate) {
        grassGrowRate = rate;
    }

    public String getName() {
        return "Rabbit";
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public void setup() {
        rabbits = new ArrayList<RabbitsGrassSimulationAgent>();
        schedule = new Schedule(1);

        if (displaySurface != null) {
            displaySurface.dispose();
        }
        displaySurface = new DisplaySurface(this, "Window");
        registerDisplaySurface("Window", displaySurface);
    }
}
