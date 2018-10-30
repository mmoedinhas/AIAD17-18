package agent;
import java.util.concurrent.ConcurrentHashMap;

import org.json.simple.JSONObject;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetResponder;
import util.RequiredSpecs;
import jade.lang.acl.MessageTemplate;

public class AgentSuperPC extends Agent {
	
	private final static double pricePerMemoryUnit = 0.2; //1MB is 0.2 euro
	private final static double pricePerCpuUnit = 1.2; //1Mhz of processing power is 1.2 euro
	private final static double pricePerSecond = 0.01; //1 second of usage is 0.01 euro
	
	private int memory; // PC's memory in kB
	private int cpu; // PC's cpu in MHz
	private int memoryTaken; // PC's memory taken by programs
	private int cpuTaken; // PC's cpu taken by programs
	ConcurrentHashMap<String,RequiredSpecs> acceptedProposals = 
			new ConcurrentHashMap<String, RequiredSpecs>(); //PC's accepted proposals with the name of the agent and the memory and cpu required by the agent

	public void setup() {
		
		initPC();	
		//add superPC test behaviour
		addBehaviour(new AnswerRequest(this, MessageTemplate.MatchPerformative(ACLMessage.CFP)));
	}
	
	/**
	 * Gets the characteristics for the superPC
	 */
	public void initPC() {
		this.memoryTaken = 0;
		this.cpuTaken = 0;
		
		Object[] args = getArguments();

		Integer[] quirks = (Integer[])args[1];
		
		this.memory = quirks[0];
		this.cpu = quirks[1];
	}
	
	class AnswerRequest extends ContractNetResponder {
		
		AgentSuperPC superPC;
		
		public AnswerRequest(AgentSuperPC a, MessageTemplate mt) {
			super(a, mt);
			this.superPC = a;
		}
		
		/**
		 * Handle received cfp message
		 */
		protected ACLMessage handleCfp(ACLMessage cfp) {
			System.out.println("tou a receber mensagem do " + cfp.getSender().getName());
			ACLMessage reply = cfp.createReply();
			RequiredSpecs specs = new RequiredSpecs(cfp.getContent());		
			boolean accept = canAccept(specs,cfp.getSender().getName());
			
			if(accept) {
				reply.setPerformative(ACLMessage.PROPOSE);
				reply.setContent(createResponse(accept,specs));
			} else {
				reply.setPerformative(ACLMessage.REFUSE);
			}
			
			return reply;
		}
				
		/**
		 * Creates reply to the proposal
		 * @param accept
		 * @return reply to proposal
		 */
		private String createResponse(boolean accept,RequiredSpecs specs) {
			JSONObject response = new JSONObject();
			response.put("Accept", accept);
			if(accept)
				response.put("price", superPC.getPrice(specs));
			return response.toJSONString();
		}

		/**
		 * Checks if we can accept the proposal
		 * @return true if superPC has enough memory and cpu available
		 */
		protected boolean canAccept(RequiredSpecs specs, String sender){
			if(superPC.getMemoryAvailable() > specs.getMemory() && superPC.getCpuAvailable() > specs.getCpu()){
				allocateSpecs(specs,sender);
				return true;
			}
			return false;
		}
		
		/**
		 * Allocates the specs needed when we accept a proposal
		 * @param specs
		 * @param sender
		 */
		private void allocateSpecs(RequiredSpecs specs,String sender) {
			superPC.setMemoryTaken(superPC.getMemoryTaken() + specs.getMemory() );
			superPC.setCpuTaken(superPC.getCpuTaken() + specs.getCpu());
			superPC.acceptedProposals.put(sender, specs);
		}

		/**
		 * Deallocates the specs when the client rejects our proposals
		 */
		protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
			System.out.println("sou um pc e fui rejeitado");
			String clientName = reject.getSender().getName();
			deallocateSpecs(clientName);
			
		}

		private void deallocateSpecs(String clientName) {
			
			RequiredSpecs proposalSpecs = superPC.acceptedProposals.get(clientName);
			superPC.setMemoryTaken(superPC.getMemoryTaken() - proposalSpecs.getMemory() );
			superPC.setCpuTaken(superPC.getCpuTaken() - proposalSpecs.getCpu());
			superPC.acceptedProposals.remove(clientName);
			
		}

		protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) {
			System.out.println(myAgent.getLocalName() + " got an accept!");
			ACLMessage result = accept.createReply();
			result.setPerformative(ACLMessage.INFORM);
			result.setContent("this is the result");
			
			return result;
		}
		
		
	}
	
	
	public int getMemory() {
		return memory;
	}
	
	public int getMemoryAvailable() {
		return memory - memoryTaken;
	}

	public int getCpu() {
		return cpu;
	}
	
	public int getCpuAvailable() {
		return cpu - cpuTaken;
	}

	public int getMemoryTaken() {
		return memoryTaken;
	}

	public void setMemoryTaken(int memoryTaken) {
		this.memoryTaken = memoryTaken;
	}

	public int getCpuTaken() {
		return cpuTaken;
	}

	public void setCpuTaken(int cpuTaken) {
		this.cpuTaken = cpuTaken;
	}

	public double getFractionMemoryTaken() {
		return ((double) memoryTaken / memory);
	}

	public double getFractionCpuTaken() {
		return ((double) cpuTaken / cpu);
	}

	public double getPrice(RequiredSpecs specs){
		double priceMemory = specs.getMemory()*this.pricePerMemoryUnit*(1 - getFractionMemoryTaken());
		double priceCPU = specs.getCpu()*this.pricePerCpuUnit*(1 - getFractionCpuTaken());
		return (priceMemory + priceCPU)*specs.getTime()*this.pricePerSecond;
	}
}
