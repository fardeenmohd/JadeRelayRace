import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.Logger;


public class ExperimentAgentBehaviour extends SimpleBehaviour {

    private final Logger logger = Logger.getMyLogger(getClass().getName());
    private static final long serialVersionUID = 6100703780980004688L;

    private static final int DELAY = 10;

    private int numTeams;
    private int numMachines;
    private DFAgentDescription[] captains;
    private int numLaps;
    private int currentAttempt;
    private int maxRetries;
    private int step;
    private MessageTemplate initialMT;
    private MessageTemplate completionMT;

    ExperimentAgentBehaviour(int numAttempts, int initLaps, int step) {
        this.currentAttempt = 1;
        this.numLaps = initLaps;
        this.maxRetries = numAttempts;
        this.step = step;
        this.initialMT = MessageTemplate.MatchConversationId("init");
        this.completionMT = MessageTemplate.MatchConversationId("completion");
    }
    @Override
    public void action() {

        try {
            // Get captains of all teams (RAc)
            captains = DFService.search(myAgent, AgentHelper.addDFD(AgentHelper.addSD("RAc")));
            // Count number of teams
            numTeams = captains.length;
            // Get rest of members of the teams (RA)
            DFAgentDescription[] runners = DFService.search(myAgent, AgentHelper.addDFD(AgentHelper.addSD("RA")));
            // Count number of machines (one member per machine)
            numMachines = runners.length / numTeams;
        } catch (FIPAException e) {
            logger.log(Logger.SEVERE, "Cannot get runners", e);
        }
        // Send start time and number of laps to team captains
        ACLMessage startMsg = new ACLMessage(ACLMessage.REQUEST);
        startMsg.setConversationId("init");
        for (DFAgentDescription captain : captains) {
            startMsg.addReceiver(captain.getName());
        }
        long startTime = System.currentTimeMillis() + DELAY * 1000;
        startMsg.setContent(startTime + ";" + numLaps);
        myAgent.send(startMsg);
        logger.info("Start msg sent!");
        // Receive confirmations
        int finalTeamCount = 0;
        while (finalTeamCount != numTeams) {
            ACLMessage confMsg = this.getAgent().receive(initialMT);
            if (confMsg != null) {
                finalTeamCount++;
                logger.info("Teams confirmed: " + finalTeamCount + "/" + numTeams);
            }
        }
        logger.info("Running experiment...");
        // Receive completion confirmations
        finalTeamCount = 0;
        while (finalTeamCount != numTeams) {
            ACLMessage compMsg = this.getAgent().receive(completionMT);
            if (compMsg != null) {
                long endTime = System.currentTimeMillis();
                finalTeamCount++;
                logger.info("Team " + compMsg.getSender().getLocalName() + " finished with time " + (endTime - startTime));
                logger.info("Teams finished: " + finalTeamCount + "/" + numTeams);
            }
        }
        // Get end time
        long endTime = System.currentTimeMillis();
        // Log results results
        logger.info("--------Results-------");
        logger.info("Number of machines: " + numMachines);
        logger.info("Number of teams: " + numTeams);
        logger.info("Number of laps: " + numLaps);
        logger.info("Total runtime: " + (endTime - startTime));

        // Update variables
        numLaps += step;
        currentAttempt++;

    }

    @Override
    public boolean done() {
        return currentAttempt > maxRetries;
    }


}
