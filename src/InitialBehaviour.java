import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.Logger;

public class InitialBehaviour extends SimpleBehaviour {
    private final Logger logger = Logger.getMyLogger(getClass().getName());
    private static final long serialVersionUID = -8974966583277442879L;

    private boolean initialized;
    private MessageTemplate emaMT;
    private Pattern pattern;

    InitialBehaviour() {
        super();
        this.initialized = false;
        this.emaMT = MessageTemplate.and(MessageTemplate.MatchSender(new AID("ExperimentAgent", AID.ISLOCALNAME)),
                MessageTemplate.MatchConversationId("init"));
        this.pattern = Pattern.compile("(\\d+);(\\d+)"); // "startTime;numLaps"
    }

    @Override
    public void action() {
        // Listen to START message from JudgeAgent
        ACLMessage msg = myAgent.receive(emaMT);
        if (msg != null) {
            Matcher m = pattern.matcher(msg.getContent());
            if (m.find()) {
                // Confirm reception
                ACLMessage confMsg = new ACLMessage(ACLMessage.INFORM);
                confMsg.setConversationId("init");
                confMsg.addReceiver(msg.getSender());
                myAgent.send(confMsg);
                // Get start time
                Date startTime = new Date(Long.parseLong(m.group(1)));
                // Get number of laps
                int numLaps = Integer.parseInt(m.group(2));
                ((RunnerAgent) myAgent).setNumLaps(numLaps);
                logger.info(myAgent.getLocalName() + ": Start msg received: (" + startTime.toString() + ";" + numLaps
                        + ")");
                // Start running at agreed time
                new Timer().schedule(new TimerTask() {
                    public void run() {
                        logger.info(myAgent.getLocalName() + ":RUN");
                        Behaviour runnerBehaviour = new RunnerBehaviour(1);
                        ((RunnerAgent) myAgent).setRunnerBehaviour(runnerBehaviour);
                        myAgent.addBehaviour(runnerBehaviour);
                    }
                }, startTime);
                initialized = true;
            }
        } else {
            block();
        }
    }

    @Override
    public boolean done() {
        return initialized;
    }

}
