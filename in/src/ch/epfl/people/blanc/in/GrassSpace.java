package ch.epfl.people.blanc.in;

import uchicago.src.sim.space.Object2DGrid;

public class GrassSpace {
    private Object2DGrid grass;

    public GrassSpace (int width, int height) {
        grass = new Object2DGrid(width, height);
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

    public Object2DGrid getCurrentSpace() {
        return grass;
    }
}
