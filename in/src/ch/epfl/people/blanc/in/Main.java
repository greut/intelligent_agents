package ch.epfl.people.blanc.in;

import java.awt.Color;
import java.util.ArrayList;

import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.gui.ColorMap;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.Object2DDisplay;
import uchicago.src.sim.gui.Value2DDisplay;
import uchicago.src.sim.util.SimUtilities;


/**
 * @author Yoan Blanc <yoan.blanc@epfl.ch>
 */
public class Main extends SimModelImpl {
    // Default values
    private static final int NUM_AGENTS = 100;
    private static final int WORLD_WIDTH = 40;
    private static final int WORLD_HEIGHT = 40;
    private static final int GRASS = 10;
    private static final int AGENT_MIN_LIFESPAN = 30;
    private static final int AGENT_MAX_LIFESPAN = 50;

    private Schedule schedule;
    private GrassSpace grassSpace;
    private DisplaySurface displaySurface;
    private ArrayList<RabbitAgent> agents;

    private int numAgents = NUM_AGENTS;
    private int worldWidth = WORLD_WIDTH;
    private int worldHeight = WORLD_HEIGHT;
    private int grass = GRASS;
    private int agentMinLifespan = AGENT_MIN_LIFESPAN;
    private int agentMaxLifespan = AGENT_MAX_LIFESPAN;

    public String getName() {
        return "Rabbit";
    }

    public void setup() {
        grassSpace = null;
        agents = new ArrayList<RabbitAgent>();
        schedule = new Schedule(1);

        if (displaySurface != null) {
            displaySurface.dispose();
        }
        displaySurface = new DisplaySurface(this, "Window");
        registerDisplaySurface("Window", displaySurface);
    }

    public void begin() {
        // build model
        grassSpace = new GrassSpace(worldWidth, worldHeight);
        grassSpace.spreadGrass(grass);

        for (int i=0; i<numAgents; i++) {
            addNewAgent();
        }
        for (RabbitAgent agent : agents) {
            agent.report();
        }
        // build schedule
        class RabbitStep extends BasicAction {
            public void execute() {
                SimUtilities.shuffle(agents);
                for (RabbitAgent agent: agents) {
                    agent.step();
                }

                displaySurface.display();
            }
        }

        schedule.scheduleActionBeginning(0, new RabbitStep());

        // build display
        ColorMap map = new ColorMap();
        for (int i=1; i<16; i++) {
            map.mapColor(i, new Color(i * 8 + 127, 0, 0));
        }
        map.mapColor(0, Color.white);

        Value2DDisplay displayGrass = new Value2DDisplay(grassSpace.getCurrentGrassSpace(), map);
        Object2DDisplay displayAgents = new Object2DDisplay(grassSpace.getCurrentAgentsSpace());
        displayAgents.setObjectList(agents);

        displaySurface.addDisplayable(displayGrass, "Grass");
        displaySurface.addDisplayable(displayAgents, "Agents");


        displaySurface.display();
    }

    private void addNewAgent() {
        RabbitAgent agent = new RabbitAgent(agentMinLifespan, agentMaxLifespan);
        agents.add(agent);
        grassSpace.addAgent(agent);
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public int getNumAgents() {
        return numAgents;
    }

    public void setNumAgents(int na) {
        numAgents = na;
    }

    public int getWorldWidth() {
        return worldWidth;
    }

    public void setWorldWidth(int width) {
        worldWidth = width;
    }

    public int getWorldHeight() {
        return worldHeight;
    }

    public void setWorldHeight(int height) {
        worldHeight = height;
    }

    public int getGrass() {
        return grass;
    }

    public void setGrass(int quantity) {
        grass = quantity;
    }

    public int getAgentMinLifespan() {
        return agentMinLifespan;
    }

    public void setAgentMinLifespan(int lifespan) {
        agentMinLifespan = lifespan;
    }

    public int getAgentMaxLifespan() {
        return agentMaxLifespan;
    }

    public void setAgentMaxLifespan(int lifespan) {
        agentMaxLifespan = lifespan;
    }

    public String[] getInitParam() {
        String[] initParams = { "NumAgents", "WorldWidth", "WorldHeight",
                "Grass", "AgentMinLifespan", "AgentMaxLifespan" };
        return initParams;
    }

    public static void main(String[] args) {
        SimInit init = new SimInit();
        Main model = new Main();
        init.loadModel(model, "", false);
    }
}
