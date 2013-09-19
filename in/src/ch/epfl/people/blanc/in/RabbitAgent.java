package ch.epfl.people.blanc.in;

import java.awt.Color;

import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;
import uchicago.src.sim.space.Object2DGrid;


public class RabbitAgent implements Drawable {
    private int x;
    private int y;
    private int vx;
    private int vy;
    private int money;
    private int ttl;
    private static int IDNumber = 0;
    private int id;
    private GrassSpace space;

    public RabbitAgent(int minLifespan, int maxLifespan) {
        x = -1;
        y = -1;
        setSpeed();
        money = 0;
        ttl = (int) ((Math.random() * (maxLifespan - minLifespan)) + minLifespan);
        id = IDNumber++;
    }

    public void setPosition(int newX, int newY) {
        x = newX;
        y = newY;
    }

    public void setSpeed() {
        do {
            // From -1 to 1
            vx = (int) Math.floor(Math.random() * 3) - 1;
            vy = (int) Math.floor(Math.random() * 3) - 1;
        } while (vx == 0 && vy == 0);
    }

    public void setSpace(GrassSpace grassSpace) {
        space = grassSpace;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void draw(SimGraphics G) {
        Color color = ttl > 10 ? Color.green : Color.blue;
        G.drawFastRoundRect(color);
    }

    public String getId() {
        return "A-"+id;
    }

    public int getMoney() {
        return money;
    }

    public int getTtl() {
        return ttl;
    }

    public void report() {
        System.out.println(getId() + " (" + x + "," + y + ") " + money + "$ TTL:" + ttl);
    }

    public void step() {
        int newx, newy;

        newx = x + vx;
        newy = y + vy;

        Object2DGrid grid = space.getCurrentAgentsSpace();
        newx = (newx + grid.getSizeX()) % grid.getSizeX();
        newy = (newy + grid.getSizeY()) % grid.getSizeY();

        if (space.moveAgent(x, y, newx, newy)) {
            // do shit
        } else {
            // change speed
            setSpeed();
        }

        ttl--;
    }
}
