import java.awt.Color;

import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;
import uchicago.src.sim.space.Object2DGrid;


/**
 * Class that implements the simulation agent for the rabbits grass simulation.

 * @author Yoan Blanc <yoan.blanc@epfl.ch>
 */

public class RabbitsGrassSimulationAgent implements Drawable {

    private int energy;
    private int x;
    private int y;
    private int vx;
    private int vy;

    public RabbitsGrassSimulationAgent(int minEnergy, int maxEnergy) {
        x = -1;
        y = -1;
        setSpeed();
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
	    g.drawFastRoundRect(Color.white);
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

    public void step(Object2DGrid grid) {
        int newx, newy;

        newx = x + vx;
        newy = y + vy;

        newx = (newx + grid.getSizeX()) % grid.getSizeX();
        newy = (newy + grid.getSizeY()) % grid.getSizeY();

        if (grid.getObjectAt(newx, newy) == null) {
            grid.putObjectAt(x, y, null);
            x = newx;
            y = newy;
            grid.putObjectAt(newx, newy, this);
        } else {
            setSpeed();
        }

        // Slowly dying.
        energy -= 1;
    }
}
