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
    private static int IDNumber = 0;

    public RabbitsGrassSimulationAgent(int minEnergy, int maxEnergy) {
        synchronized(RabbitsGrassSimulationAgent.class) {
            id = IDNumber++;
        }
        energy = (int) (Math.random() * (maxEnergy - minEnergy)) + minEnergy;
        // x, y are defined later on via setPosition
        setSpeed();
    }

    public String toString() {
        return id + " @" + x + "," + y + " (" + energy + ")";
    }

    private void setSpeed() {
        do {
            vx = (int) Math.floor(Math.random() * 3) - 1;
            vy = (int) Math.floor(Math.random() * 3) - 1;
        } while (vx == 0 && vy == 0);
    }

	public void draw(SimGraphics g) {
        // TODO display rabbits about to bread as well
        Color color = energy < 10 ? Color.gray : Color.white;
	    g.drawFastRoundRect(color);
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
        // to avoid going always in the same direction if we are alone (p=.9)
        if (rabbits.getObjectAt(newx, newy) == null && Math.random() < .9) {
            rabbits.putObjectAt(x, y, null);
            x = newx;
            y = newy;
            rabbits.putObjectAt(newx, newy, this);
        } else {
            setSpeed();
        }

        // Grabbing energy
        energy += ((Integer) grass.getObjectAt(x, y)).intValue() * grassEnergy;
        grass.putObjectAt(x, y, new Integer(0));

        // Breading is done in the space code.

        // Slowly dying.
        energy -= 1;
    }
}
