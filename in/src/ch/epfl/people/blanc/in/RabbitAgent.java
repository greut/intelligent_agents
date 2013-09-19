package ch.epfl.people.blanc.in;

import java.awt.Color;

import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;


public class RabbitAgent implements Drawable {
    private int x;
    private int y;
    private int money;
    private int ttl;
    private static int IDNumber = 0;
    private int id;

    public RabbitAgent(int minLifespan, int maxLifespan) {
        x = -1;
        y = -1;
        money = 0;
        ttl = (int) ((Math.random() * (maxLifespan - minLifespan)) + minLifespan);
        id = IDNumber++;
    }

    public void setPosition(int newX, int newY) {
        x = newX;
        y = newY;
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
        ttl--;
    }
}
