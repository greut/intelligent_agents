import uchicago.src.sim.space.Object2DGrid;

/**
 * Class that implements the simulation space of the rabbits grass simulation.
 * @author Yoan Blanc <yoan.blanc@epfl.ch>
 */
public class RabbitsGrassSimulationSpace {
    private int width;
    private int height;

    private Object2DGrid grass;

    public RabbitsGrassSimulationSpace(int w, int h) {
        width = w;
        height = h;
        
        grass = new Object2DGrid(width, height);
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
                    ((Integer) grass.getObjectAt(x, y)).intValue() + 1);
        }
    }

    public Object2DGrid getCurrentGrassSpace() {
        return grass;
    }
}
