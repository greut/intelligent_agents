package ch.epfl.people.blanc.in;

import uchicago.src.sim.space.Object2DGrid;

public class GrassSpace {
    private Object2DGrid grass;
    private Object2DGrid agents;

    public GrassSpace (int width, int height) {
        grass = new Object2DGrid(width, height);
        agents = new Object2DGrid(width, height);

        for (int i=0; i<width; i++) {
            for (int j=0; j<height; j++) {
                grass.putObjectAt(i, j, new Integer(0));
            }
        }
    }

    public void spreadGrass(int quantity) {
        for(int i=0; i<quantity; i++) {
            int x, y, currentValue;
            x = (int) (Math.random() * grass.getSizeX());
            y = (int) (Math.random() * grass.getSizeY());
            currentValue = getMoneyAt(x, y);
            grass.putObjectAt(x, y, new Integer(currentValue + 1));
        }
    }

    public int getMoneyAt(int x, int y) {
        if (grass.getObjectAt(x, y) != null) {
            return ((Integer) grass.getObjectAt(x, y)).intValue();
        }
        return 0;
    }

    public Object2DGrid getCurrentGrassSpace() {
        return grass;
    }

    public Object2DGrid getCurrentAgentsSpace() {
        return agents;
    }

    public boolean isCellOccupied(int x, int y) {
        return agents.getObjectAt(x, y) != null;
    }

    public void addAgent(RabbitAgent agent) {
        int x, y;
        do {
            x = (int) (Math.random() * agents.getSizeX());
            y = (int) (Math.random() * agents.getSizeY());
        } while(isCellOccupied(x, y));
        agents.putObjectAt(x, y, agent);
        agent.setPosition(x, y);
        agent.setSpace(this);
    }

    public void removeAgent(RabbitAgent agent) {
        agents.putObjectAt(agent.getX(), agent.getY(), null);
    }

    public boolean moveAgent(int x, int y, int nx, int ny) {
        if (isCellOccupied(nx, ny)) {
            return false;
        }

        RabbitAgent agent = (RabbitAgent) agents.getObjectAt(x, y);
        agents.putObjectAt(x, y, null);
        agent.setPosition(nx, ny);
        agents.putObjectAt(nx, ny, agent);
        return true;
    }
}
