import uchicago.src.sim.engine.SimInit;

/**
 * Launching the Simulation.
 *
 * @author Yoan Blanc <yoan.blanc@epfl.ch>
 */
public class MainRabbit {

    public static void main(String[] args){
        SimInit init = new SimInit();
        RabbitsGrassSimulationModel model = new RabbitsGrassSimulationModel();
        init.loadModel(model, "", false);
    }

}
