import java.awt.Color;

import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;
import uchicago.src.sim.space.Object2DGrid;


/**
 * Class that implements the simulation agent for the rabbits grass simulation.

 * @author Yoan Blanc <yoan.blanc@epfl.ch>
 */

public class RabbitsGrassSimulationAgent implements Drawable {

    private int id;
    private int energy;
    private int x;
    private int y;
    private int vx;
    private int vy;
    private int bread;
    private static int IDNumber = 0;

    public RabbitsGrassSimulationAgent(int minEnergy, int maxEnergy, int breadEnergy) {
        synchronized(RabbitsGrassSimulationAgent.class) {
            id = IDNumber++;
        }
        energy = (int) (Math.random() * (Math.min(maxEnergy, breadEnergy) - minEnergy)) + minEnergy;
        bread = breadEnergy;
        // x, y are defined later on via setPosition
        setSpeed();
    }

    public String toString() {
        return id + " @" + x + "," + y + " (" + energy + ")";
    }

    /**
     * Changes the speed (where the rabbit will move).
     *
     * Only N,W,S,E options are valid, NW or SE are not.
     */
    private void setSpeed() {
        do {
            vx = (int) Math.floor(Math.random() * 3) - 1;
            vy = (int) Math.floor(Math.random() * 3) - 1;
        } while (vx == 0 && vy == 0 || vx != 0 && vy != 0);
    }

    /**
     * Draws the rabbit
     *
     *
     * @param g graphical context to draw in.
     */
	public void draw(SimGraphics g) {
        Color color;
        if (energy < 2) {
            color = Color.darkGray;
        } else if (energy < 5) {
            color = Color.gray;
        } else if (energy < 10) {
            color = Color.lightGray;
        } else if (energy > bread - 2) {
            color = Color.pink;
        } else {
            color = Color.white;
        }
	    g.drawFastCircle(color);
	}

    public void setPosition(int nx, int ny) {
        x = nx;
        y = ny;
    }

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

    public boolean isDead() {
        return energy < 1;
    }

    public int getEnergy() {
        return energy;
    }

    public void setEnergy(int newEnergy) {
        energy = newEnergy;
    }

    public void step(RabbitsGrassSimulationSpace space, int grassEnergy) {
        int newx, newy;

        Object2DGrid grass = space.getCurrentGrassSpace();
        Object2DGrid rabbits = space.getCurrentRabbitSpace();

        // Move
        newx = x + vx;
        newy = y + vy;

        newx = (newx + grass.getSizeX()) % grass.getSizeX();
        newy = (newy + grass.getSizeY()) % grass.getSizeY();

        // Change the direction if we meet someone else or once in a while
        // to avoid going always in the same direction if we are alone (p=.95)
        // NB: the rabbits space has been emptied beforehand so there is no
        // needs to remove ourselves from where we where.
        if (rabbits.getObjectAt(newx, newy) == null && Math.random() < .95) {
            x = newx;
            y = newy;
            // Moving takes some energy
            energy -= 1;
        } else {
            setSpeed();
        }

        // Grabbing energy
        energy += ((Integer) grass.getObjectAt(x, y)).intValue() * grassEnergy;
        if (!isDead()) {
            grass.putObjectAt(x, y, new Integer(0));
            rabbits.putObjectAt(x, y, this);
        }

        // Breading is done in the space code.
    }
}
