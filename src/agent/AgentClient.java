package agent;

import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetInitiator;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import util.RequiredSpecs;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public abstract class AgentClient extends Agent {

	protected int serverNo; // number of servers available
	protected int memoryNeeded; // memory needed in KBs
	protected int cpuNeeded; // cpu power needed in Mhz
	protected int timeNeeded; // time needed in s
	protected String[] superPCsNames; // names of the superPCs
	protected ConcurrentLinkedQueue<AgentController> agentsQueue;

	public void setup() {
		initClient();
	}

	public void initClient() {
		Object[] args = getArguments();

		Integer[] quirks = (Integer[]) args[0];
		String[] superPCNames = (String[]) args[1];
		this.agentsQueue = (ConcurrentLinkedQueue<AgentController>)args[2];
		this.serverNo = quirks[0];
		this.memoryNeeded = quirks[1];
		this.cpuNeeded = quirks[2];
		this.timeNeeded = quirks[3];
		this.superPCsNames = superPCNames;
	}

	protected abstract class RequireSuperPC extends ContractNetInitiator {

		private AgentClient agent;

		public RequireSuperPC(AgentClient a, ACLMessage cfp) {
			super(a, cfp);
			this.agent = a;
		}

		protected Vector prepareCfps(ACLMessage cfp) {
			Vector v = new Vector();

			for (int i = 0; i < superPCsNames.length; i++) {
				cfp.addReceiver(new AID(superPCsNames[i], AID.ISGUID));
			}

			// create message content
			RequiredSpecs specs = new RequiredSpecs(this.agent.memoryNeeded,this.agent.cpuNeeded,this.agent.timeNeeded);
			cfp.setContent(specs.toString());
			v.add(cfp);
			return v;
		}

		protected void handleAllResponses(Vector responses, Vector acceptances) {

			System.out.println("got " + responses.size() + " responses!");
			processProposal(responses, acceptances);
		}
		
		protected abstract void processProposal(Vector responses, Vector acceptances);
		
		protected void callNextAgentInQueue() {
			agent.agentsQueue.poll();
			if(agent.agentsQueue.size() != 0) {
				try {
					agent.agentsQueue.peek().start();
				} catch (StaleProxyException e) {
					System.out.println("Error launching next client");
					e.printStackTrace();
					System.exit(1);
				}
			}
		}
		
		protected void handleAllResultNotifications(Vector resultNotifications) {
			for (Object result : resultNotifications) {
				System.out.println(((ACLMessage)result).getContent());
			}
			callNextAgentInQueue();
		}
		
		

	}
}
