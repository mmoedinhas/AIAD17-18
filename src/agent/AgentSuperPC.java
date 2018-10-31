package agent;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.json.simple.JSONObject;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetResponder;
import util.RequiredSpecs;
import util.Timer;
import jade.lang.acl.MessageTemplate;

public class AgentSuperPC extends Agent {
	
	private final static double pricePerMemoryUnit = 0.2; //1MB is 0.2 euro
	private final static double pricePerCpuUnit = 1.2; //1Mhz of processing power is 1.2 euro
	private final static double pricePerSecond = 0.01; //1 second of usage is 0.01 euro
	
	private int memory; // PC's memory in kB
	private int cpu; // PC's cpu in MHz
	private int memoryTaken; // PC's memory taken by programs
	private int cpuTaken; // PC's cpu taken by programs
	//PC's accepted proposals with the name of the agent and the memory and cpu required by the agent
	private ConcurrentHashMap<String,RequiredSpecs> acceptedProposals = 
			new ConcurrentHashMap<String, RequiredSpecs>(); 
	
	//pool to execute timer threads
	private ScheduledExecutorService scheduledPool = Executors.newScheduledThreadPool(20);

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
	
	/**
	 * Allocates the specs needed for the client that accepted the proposal
	 * @param specs
	 * @param clientName
	 */
	public synchronized void allocateSpecs(RequiredSpecs specs,String clientName) {
		setMemoryTaken(getMemoryTaken() + specs.getMemory() );
		setCpuTaken(getCpuTaken() + specs.getCpu());
		acceptedProposals.put(clientName, specs);
	}
	
	/**
	 * Releases cpu and memory being used by leaving client
	 * @param clientName
	 */
	public synchronized void deallocateSpecs(String clientName) {
		
		RequiredSpecs proposalSpecs = acceptedProposals.get(clientName);
		setMemoryTaken(getMemoryTaken() - proposalSpecs.getMemory() );
		setCpuTaken(getCpuTaken() - proposalSpecs.getCpu());
		acceptedProposals.remove(clientName);
	}
	
	/**
	 * 
	 * @param specs
	 * @param clientName
	 */
	private void startRunningClient(RequiredSpecs specs,String clientName) {
		allocateSpecs(specs, clientName);
		
		Timer timer = new Timer(this,clientName);
		this.scheduledPool.schedule(timer,specs.getTime(),TimeUnit.MILLISECONDS);
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
				return true;
			}
			return false;
		}

		/**
		 * Handles the rejection of a proposal
		 */
		protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
			System.out.println("sou um pc e fui rejeitado");
		}
		
		/**
		 * Handles the acceptance of a proposal
		 * Informs the client and starts running the client program in the superPC
		 */
		protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) {
			System.out.println(myAgent.getLocalName() + " got an accept!");
			
			//Allocates space in superPC for client and starts timer
			RequiredSpecs specs = new RequiredSpecs(cfp.getContent());
			startRunningClient(specs, cfp.getSender().getName());
			
			// Creates reply to inform the client
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
	
	/**
	 * The price is higher for super PC's that have less memory and cpu occupied.
	 * @param specs The specifications needed for the client program to run.
	 * @return the price proposed by the super pc
	 */
	public double getPrice(RequiredSpecs specs){
		double priceMemory = specs.getMemory()*this.pricePerMemoryUnit*(1 - getFractionMemoryTaken());
		double priceCPU = specs.getCpu()*this.pricePerCpuUnit*(1 - getFractionCpuTaken());
		return (priceMemory + priceCPU)*specs.getTime()*this.pricePerSecond;
	}
}
