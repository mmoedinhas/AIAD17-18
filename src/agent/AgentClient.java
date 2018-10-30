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

public class AgentClient extends Agent {

	private int serverNo; // number of servers available
	private int memoryNeeded; // memory needed in KBs
	private int cpuNeeded; // cpu power needed in Mhz
	private int timeNeeded; // time needed in s
	private String[] superPCsNames; // names of the superPCs
	private ConcurrentLinkedQueue<AgentController> agentsQueue;

	public void setup() {

		initClient();
		// add test client behaviour
		addBehaviour(new RequireSuperPC(this, new ACLMessage(ACLMessage.CFP)));

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

	/*
	 * public ACLMessage createCFP() {
	 * 
	 * }
	 */

	// http://jade.tilab.com/doc/api/jade/proto/ContractNetInitiator.html
	public class RequireSuperPC extends ContractNetInitiator {

		private AgentClient agent;

		public RequireSuperPC(AgentClient a, ACLMessage cfp) {
			super(a, cfp);
			this.agent = a;
			// TODO Auto-generated constructor stub
		}
		// private int serverNo; //number of servers available
		// private int memoryNeeded; //memory needed in KBs
		// private int cpuNeeded; //cpu power needed in Mhz
		// private int timeNeeded; //time needed in s

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
		
		protected void processProposal(Vector responses, Vector acceptances) {
			
			double minPrice = Double.MAX_VALUE;
			double minPriceIndex = -1;
			
			for(int i=0; i<responses.size(); i++) {
	
				if(((ACLMessage) responses.get(i)).getPerformative() == ACLMessage.REFUSE) {
					System.out.println("Sou um cliente rejeitado :(");
					continue;
				}
				
				JSONParser parser = new JSONParser();
				JSONObject content;
				double proposedPrice = 0;
				
				try {
					content = (JSONObject) parser.parse(((ACLMessage) responses.get(i)).getContent());
					proposedPrice = ((double)content.get("price"));
				} catch (ParseException e) {
					e.printStackTrace();
				}
				
				if(minPrice >= proposedPrice) {
					minPrice = proposedPrice;
					minPriceIndex = i;
				}
			}
			
			for(int i=0; i<responses.size(); i++) {
				ACLMessage msg = ((ACLMessage) responses.get(i)).createReply();
				
				if(minPriceIndex == i)
					msg.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
				else
					msg.setPerformative(ACLMessage.REJECT_PROPOSAL);
				
				acceptances.add(msg);
			}
		}
		
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
			callNextAgentInQueue();
		}
		
		

	}
}
