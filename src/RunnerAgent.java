import jade.content.lang.sl.SLCodec;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.AMSService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.mobility.MobilityOntology;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.Logger;
import jade.wrapper.ControllerException;

public class RunnerAgent extends Agent {

    private final Logger logger = Logger.getMyLogger(getClass().getName());
    private static final long serialVersionUID = 7941245165294941476L;

    private Behaviour runnerBehaviour;
    private boolean captain;
    private String targetAgent;
    private String originLocation;
    private int numLaps;
    private int completedLaps;

    @Override
    protected void setup() {
        // Get arguments (isCaptain, targetAgent)
        Object[] args = getArguments();
        if (args != null && args.length == 2) {
            captain = ((String) args[0]).equalsIgnoreCase("true");
            targetAgent = (String) args[1];
            if (targetAgent != null) {
                logger.info("Runner " + getLocalName() + " (C:" + captain + "). Target: " + targetAgent);
            } else {
                logger.log(Logger.SEVERE, "Agent " + getLocalName() + " - Incorrect target agent");
                doDelete();
            }
        } else {
            logger.log(Logger.SEVERE, "Agent " + getLocalName() + " - Incorrect number of arguments");
            doDelete();
        }
        // Register agent in yellow pages
        String type = captain ? "RAc" : "RA";
        AgentHelper.registerAgent(this, type);
        // Save origin location
        try {
            originLocation = getContainerController().getContainerName();
        } catch (ControllerException e) {
            logger.log(Logger.SEVERE, "Agent " + getLocalName() + " - Cannot get container name");
        }
        // Init laps counter
        completedLaps = 0;
        // Register content language and mobility ontology
        getContentManager().registerLanguage(new SLCodec(), FIPANames.ContentLanguage.FIPA_SL0);
        getContentManager().registerOntology(MobilityOntology.getInstance());
        // Add behaviour
        if (captain) {
            addBehaviour(new InitialBehaviour());
        } else {
            addBehaviour(new RunnerBehaviour(0));
        }
    }

    @Override
    protected void afterMove() {
        // Get new location
        String newLocation = null;
        try {
            newLocation = getContainerController().getContainerName();
        } catch (ControllerException e) {
            logger.log(Logger.SEVERE, "Agent " + getLocalName() + " - Cannot get container name");
        }
        logger.info(getLocalName() + ": Reach new location: " + newLocation);
        if (captain) {
            if (newLocation.equals(originLocation)) {
                // If is the origin -> one lap completed
                completedLaps++;
                logger.info(getLocalName() + ": New lap completed!. " + completedLaps + "/" + numLaps);
                if (completedLaps >= numLaps) {
                    // All laps completed
                    logger.info(getLocalName() + ": All laps completed!!!");
                    // Send competition message to judge and finish
                    ACLMessage compMsg = new ACLMessage(ACLMessage.INFORM);
                    compMsg.setConversationId("completion");
                    compMsg.addReceiver(new AID("ExperimentAgent", AID.ISLOCALNAME));
                    send(compMsg);
                    // Restart behaviours (Remove RunnerBehaviour and add InitialBehaviour)
                    originLocation = newLocation;
                    removeBehaviour(runnerBehaviour);
                    addBehaviour(new InitialBehaviour());
                    return;
                }
            }
        }
        // Register content language and mobility ontology
        getContentManager().registerLanguage(new SLCodec(), FIPANames.ContentLanguage.FIPA_SL0);
        getContentManager().registerOntology(MobilityOntology.getInstance());
        // Send message to local agent to start running
        ACLMessage runMsg = new ACLMessage(ACLMessage.REQUEST);
        runMsg.setConversationId("running");
        runMsg.addReceiver(getTargetAgent());
        send(runMsg);
        logger.info(getLocalName() + ": Relay given");
        // Wait its anwer
        MessageTemplate mtRunner = MessageTemplate.MatchConversationId("running");
        ACLMessage msg;
        do {
            msg = receive(mtRunner);
        } while (msg == null);
    }

    AID getTargetAgent() {
        //Get Agent AID
        SearchConstraints sC = new SearchConstraints();
        sC.setMaxResults((long) -1);
        AMSAgentDescription [] agents;
        try {
            agents = AMSService.search(this, new AMSAgentDescription(), sC);
            for (AMSAgentDescription agent : agents) {
                if (agent.getName().getLocalName().equals(targetAgent)) {
                    return agent.getName();
                }
            }
        } catch (FIPAException e) {
            e.printStackTrace();
        }

        return null;
    }

    void setNumLaps(int num) {
        this.numLaps = num;
    }

    void setRunnerBehaviour(Behaviour b) {
        this.runnerBehaviour = b;
    }


}
