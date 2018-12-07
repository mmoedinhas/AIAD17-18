package agent;

import java.util.Arrays;
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
import util.CSVUtil;
import util.RequiredSpecs;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import generator.Generator;

public abstract class AgentClient extends Agent {

	protected int serverNo; // number of servers available
	protected int memoryNeeded; // memory needed in KBs
	protected int cpuNeeded; // cpu power needed in Mhz
	protected int timeNeeded; // time needed in s
	protected String[] superPCsNames; // names of the superPCs
	protected ConcurrentLinkedQueue<AgentController> agentsQueue;
	protected int arrivalTime = 0;

	public void setup() {
		int waitingTime = initClient();
		CSVUtil.runningTime += waitingTime;
		arrivalTime = CSVUtil.runningTime;
		//System.out.println("vou esperar " + waitingTime);
		try {
			Thread.sleep(waitingTime);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public int initClient() {
		Object[] args = getArguments();
		
		Integer[] quirks = (Integer[]) args[0];
		String[] superPCNames = (String[]) args[1];
		this.agentsQueue = (ConcurrentLinkedQueue<AgentController>)args[2];
		this.serverNo = quirks[0];
		this.memoryNeeded = quirks[1];
		this.cpuNeeded = quirks[2];
		this.timeNeeded = quirks[3];
		this.superPCsNames = superPCNames;
		
		return quirks[4];
	}

	@Override
	public String toString() {
		return this.getName() + "[serverNo=" + serverNo + ", memoryNeeded=" + memoryNeeded + ", cpuNeeded=" + cpuNeeded
				+ ", timeNeeded=" + timeNeeded;
	}

	protected abstract class RequireSuperPC extends ContractNetInitiator {

		protected AgentClient agent;

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
			System.out.println(this.agent + " sent a request");
			return v;
		}

		protected void handleAllResponses(Vector responses, Vector acceptances) {
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
			myAgent.doDelete();
		}
		
		protected String getRejectionMsg(ACLMessage response) {
			String senderName = response.getSender().getName();
			return this.agent.getName() + " was rejected by " + senderName;
		}
		
		protected String getProposalMessage(ACLMessage response) {
			String senderName = response.getSender().getName();
			
			JSONParser parser = new JSONParser();
			JSONObject content;
			int proposedWaitingTime = 0;
			double proposedPrice = 0;
			
			try {
				//get proposal content
				content = (JSONObject) parser.parse(response.getContent());
				proposedWaitingTime = ((Long) content.get("waitingTime")).intValue();
				proposedPrice = (Double) content.get("price");
			} catch (ParseException e) {
				e.printStackTrace();
			}
			
			String msg = this.agent.getName() + ": " + 
					senderName + " offered me price=" + proposedPrice + " and waitingTime=" + proposedWaitingTime;
			return msg;
		}

	}
}
