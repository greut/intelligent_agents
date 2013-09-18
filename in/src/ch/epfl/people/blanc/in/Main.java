package ch.epfl.people.blanc.in;

import java.awt.Color;

import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.gui.ColorMap;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.Value2DDisplay;


public class Main extends SimModelImpl {
    // Default values
    private static final int NUM_AGENTS = 100;
    private static final int WORLD_WIDTH = 40;
    private static final int WORLD_HEIGHT = 40;
    private static final int GRASS = 10;

    private Schedule schedule;
    private GrassSpace grassSpace;
    private DisplaySurface displaySurface;

    private int numAgents = NUM_AGENTS;
    private int worldWidth = WORLD_WIDTH;
    private int worldHeight = WORLD_HEIGHT;
    private int grass = GRASS;

    public String getName() {
        return "Rabbit";
    }

    public void setup() {
        grassSpace = null;
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
        // build schedule:w
        //
        // build display
        ColorMap map = new ColorMap();
        for (int i=1; i<16; i++) {
            map.mapColor(i, new Color((int) (i * 8 + 127), 0, 0));
        }
        map.mapColor(0, Color.white);

        Value2DDisplay displayGrass = new Value2DDisplay(grassSpace.getCurrentSpace(), map);
        displaySurface.addDisplayable(displayGrass, "Grass");

        displaySurface.display();
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

    public String[] getInitParam() {
        String[] initParams = { "NumAgents", "WorldWidth", "WorldHeight", "Grass" };
        return initParams;
    }

    public static void main(String[] args) {
        SimInit init = new SimInit();
        Main model = new Main();
        init.loadModel(model, "", false);
    }
}
