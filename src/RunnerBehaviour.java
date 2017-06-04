import jade.core.Location;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.Logger;

public class RunnerBehaviour extends CyclicBehaviour {

    private final Logger logger = Logger.getMyLogger(getClass().getName());
    private static final long serialVersionUID = 7823632300018804712L;

    private int step;
    private Location destination;
    private MessageTemplate runnerMT;

    /**
     * RunnerBehaviour constructor.
     *
     * @param step
     *            initial action to perform (0: wait for message; 1: run to
     *            destination)
     */
    RunnerBehaviour(int step) {
        super();
        this.step = step;
        this.runnerMT = MessageTemplate.MatchConversationId("running");
    }

    @Override
    public void action() {
        switch (step) {
            case 0:
                // Receive message from previous runner
                ACLMessage msg = myAgent.receive(runnerMT);
                if (msg != null) {
                    logger.info(myAgent.getLocalName() + ":Request to start running received!");
                    // Confirm message
                    ACLMessage confMsg = new ACLMessage(ACLMessage.INFORM);
                    confMsg.setConversationId("running");
                    confMsg.addReceiver(msg.getSender());
                    myAgent.send(confMsg);
                    // Start running
                    step++;
                } else {
                    block();
                }
                break;
            case 1:
                // Ask the location of targetAgent
                ACLMessage request = AgentHelper.prepareRequestToAMS(myAgent, ((RunnerAgent) myAgent).getTargetAgent());
                myAgent.send(request);
                step++;
                break;
            case 2:
                // Get reply from AMS with the location
                MessageTemplate mt = MessageTemplate.MatchSender(myAgent.getAMS());
                ACLMessage response = myAgent.receive(mt);
                if (response != null) {
                    destination = AgentHelper.parseAMSResponse(myAgent, response);
                    step++;
                } else {
                    block();
                }
                break;
            case 3:
                // Run to the destination
                logger.info(myAgent.getLocalName() + ":Start running to " + destination.getName());
                myAgent.doMove(destination);
                step = 0;
                break;
        }
    }
}
