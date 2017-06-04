import jade.core.Agent;
import jade.util.Logger;

/**
 * EMA guides the execution of the experiment, gives the start signal,
 * time the execution time and logs the results each lap.
 *
 * > Parameters:
 * 1. maxRetries: total number of attempts to run.
 * 2. initLaps: number of laps in first attempt.
 * 3. step: number of laps to increase in each attempt.
 */
public class ExperimentMasterAgent extends Agent {

    private final Logger logger = Logger.getMyLogger(getClass().getName());
    private static final long serialVersionUID = 934303410329286008L;

    private int maxRetries;
    private int initLaps;
    private int step;

    @Override
    protected void setup() {
        // Get arguments (nAttempts, initLaps, step)
        Object[] args = getArguments();
        if (args != null && args.length == 3) {
            maxRetries = Integer.parseInt((String) args[0]);
            initLaps = Integer.parseInt((String) args[1]);
            step = Integer.parseInt((String) args[2]);
            logger.info("Init experiment (attempts:" + maxRetries + ", initial laps:" + initLaps + ", step:" + step);
        } else {
            logger.log(Logger.SEVERE, "Agent " + getLocalName() + " - Incorrect number of arguments");
            doDelete();
        }
        // Init experiment
        addBehaviour(new ExperimentAgentBehaviour(maxRetries, initLaps, step));
    }



}
