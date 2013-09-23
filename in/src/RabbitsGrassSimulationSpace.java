import uchicago.src.sim.space.Object2DGrid;

/**
 * Class that implements the simulation space of the rabbits grass simulation.
 * @author Yoan Blanc <yoan.blanc@epfl.ch>
 */
public class RabbitsGrassSimulationSpace {
    private static int MAX_GRASS = 15;

    private int width;
    private int height;

    private Object2DGrid grass;
    private Object2DGrid rabbits;

    public RabbitsGrassSimulationSpace(int w, int h) {
        width = w;
        height = h;

        grass = new Object2DGrid(width, height);
        rabbits = new Object2DGrid(width, height);

        for (int i=0; i<width; i++) {
            for (int j=0; j<height; j++) {
                grass.putObjectAt(i, j, new Integer(0));
            }
        }
    }

    public void spreadGrass(int quantity) {
        for (int i=0; i<quantity; i++) {
            int x, y;
            x = (int) (Math.random() * width);
            y = (int) (Math.random() * height);
            grass.putObjectAt(x, y,
                    Math.min(((Integer) grass.getObjectAt(x, y)).intValue() + 1,
                             MAX_GRASS));
        }
    }

    public void addRabbit(RabbitsGrassSimulationAgent rabbit) {
        int x, y;
        do {
            x = (int) (Math.random() * width);
            y = (int) (Math.random() * height);
        } while (rabbits.getObjectAt(x, y) != null);
        rabbits.putObjectAt(x, y, rabbit);
        rabbit.setPosition(x, y);
    }

    public void removeRabbit(RabbitsGrassSimulationAgent rabbit) {
        rabbits.putObjectAt(rabbit.getX(), rabbit.getY(), null);
    }

    public Object2DGrid getCurrentGrassSpace() {
        return grass;
    }

    public Object2DGrid getCurrentRabbitSpace() {
        return rabbits;
    }

    public int getTotalGrass() {
        int total = 0;
        for(int i = 0; i < grass.getSizeX(); i++)
            for(int j = 0; j < grass.getSizeY(); j++)
                if(((Integer)grass.getObjectAt(i, j)).intValue() > 0)
                    total++;
        return total;
    }
}
