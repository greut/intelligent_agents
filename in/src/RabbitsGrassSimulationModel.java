import java.awt.Color;

import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.gui.ColorMap;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.Value2DDisplay;

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

    private Schedule schedule;
    private DisplaySurface displaySurface;
    private RabbitsGrassSimulationSpace space;

    public static void main(String[] args) {
        SimInit init = new SimInit();
        RabbitsGrassSimulationModel model = new RabbitsGrassSimulationModel();
        init.loadModel(model, "", false);
    }

    public void begin() {
        // build model
        space = new RabbitsGrassSimulationSpace(worldWidth, worldHeight);
        space.spreadGrass(grassGrowRate);

        // build schedule
        // TODO

        // build display
        ColorMap map = new ColorMap();
        for (int i=1; i<16; i++) {
            map.mapColor(i, new Color(0, i * 8 + 127, 0));
        }
        map.mapColor(0, Color.black);
        Value2DDisplay displayGrass = new Value2DDisplay(space.getCurrentGrassSpace(), map);
        displaySurface.addDisplayableProbeable(displayGrass, "Grass");
        displaySurface.display();
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
        schedule = new Schedule(1);
        if (displaySurface != null) {
            displaySurface.dispose();
        }
        displaySurface = new DisplaySurface(this, "Window");
        registerDisplaySurface("Window", displaySurface);
    }
}
