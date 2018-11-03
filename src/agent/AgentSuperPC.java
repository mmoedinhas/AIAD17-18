package agent;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.json.simple.JSONObject;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetResponder;
import jade.wrapper.AgentController;
import util.ClientsQueue;
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
	private ConcurrentHashMap<String, RequiredSpecs> acceptedProposals = 
			new ConcurrentHashMap<String, RequiredSpecs>(); 
	
	//pool to execute timer threads
	private ScheduledExecutorService scheduledPool = Executors.newScheduledThreadPool(20);
	
	//client queue
	private ClientsQueue queue = new ClientsQueue();

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
	 * Allocates the specs needed for the client that accepted the proposal and starts timer
	 * @param specs
	 * @param clientName
	 */
	public synchronized void allocateSpecs(RequiredSpecs specs,String clientName) {
		setMemoryTaken(getMemoryTaken() + specs.getMemory() );
		setCpuTaken(getCpuTaken() + specs.getCpu());
		acceptedProposals.put(clientName, specs);
		Timer timer = new Timer(this,clientName);
		this.scheduledPool.schedule(timer,specs.getTime(),TimeUnit.MILLISECONDS);
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
	 * Either starts running the client or puts it in queue
	 * @param specs
	 * @param clientName
	 */
	private String allocateClient(RequiredSpecs specs,String clientName) {
		
		if(canRun(specs)) {
			allocateSpecs(specs, clientName);
			return clientName + " started running now in " + this.getName();
		} else {
			queue.addClient(clientName, specs);
			return clientName + " was added to queue of " + this.getName();
		}
		
	}
	
	private synchronized void runClientsInQueue() {
		
		while(true){
			int cpuNeeded = queue.peekClientSpecs().getCpu();
			int memNeeded = queue.peekClientSpecs().getMemory();
			String clientName = queue.peekClientName();
			
			if(cpu - cpuTaken > cpuNeeded &&  memory - memoryTaken > memNeeded){
				RequiredSpecs specs = queue.pollClient().getSpecs();
				allocateSpecs(specs,clientName);
			}else{
				return;
			}
		}
	}
	
	public synchronized void prepareNextClient(String oldClientName) {
		deallocateSpecs(oldClientName);
		runClientsInQueue();
	}
	
	private boolean canRun(RequiredSpecs specs) {
		if(queue.size() == 0 && cpu - cpuTaken >= specs.getCpu() && memory - memoryTaken >= specs.getMemory()){
			return true;
		}
		return false;
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
			boolean accept = canAccept(specs);
			if(accept) {
				reply.setPerformative(ACLMessage.PROPOSE);
				reply.setContent(createResponse(specs));
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
		private String createResponse(RequiredSpecs specs) {
			JSONObject response = new JSONObject();
			response.put("price", superPC.getPrice(specs));
			response.put("waitingTime", getWaitingTime(specs));
			return response.toJSONString();
		}

		/**
		 * Checks if we can accept the proposal
		 * @return true if superPC has enough memory and cpu available
		 */
		protected boolean canAccept(RequiredSpecs specs){
			if(superPC.getMemory() > specs.getMemory() && superPC.getCpu() > specs.getCpu()){
				return true;
			}
			return false;
		}
		
		/**
		 * 
		 * @param specs
		 * @return 0 if can run immediately or time to be spent in queue
		 */
		protected int getWaitingTime(RequiredSpecs specs) {
			//time to be spent in queue is time of people in queue + time of people in server
			
			//get the specs of all waiting programs in a queue
			ConcurrentLinkedQueue<RequiredSpecs> waitingSpecs = queue.getSpecs();
			
			//get the specs of all running programs in a vector
			Vector<RequiredSpecs> runningPrograms = new Vector<RequiredSpecs>();
			for (Entry<String, RequiredSpecs> entry : acceptedProposals.entrySet()) {
				RequiredSpecs copia = new RequiredSpecs(entry.getValue());
			    runningPrograms.add(copia);
			}
			waitingSpecs.add(specs);
			Integer cpuTaken = superPC.getCpuTaken();
			Integer memTaken = superPC.getMemoryTaken();
			int maxCpu = superPC.getCpu();
			int maxMem = superPC.getMemory();
			
			int totalTime = 0;
			//checks if has space remaining
			if(waitingSpecs.size() == 1 && maxCpu - cpuTaken >= specs.getCpu() && maxMem - memTaken >= specs.getMemory()){
				return 0;
			}
			
			// ta mal aqui
			while(waitingSpecs.size() != 0){
				//ve o tempo minimo para sair um programa de correr
				int minTime = getMinTime(runningPrograms);
				totalTime += minTime;
				//retira o tempo minimo de todos os programas
				removeTimeFromRunningPrograms(runningPrograms, minTime);
				int[] resultTaken = removeEndedRunningPrograms(runningPrograms,cpuTaken,memTaken);
				cpuTaken = resultTaken[0];
				memTaken = resultTaken[1];
				resultTaken = addProgramstoRunning(runningPrograms,waitingSpecs,maxCpu,cpuTaken,maxMem,memTaken);
				cpuTaken = resultTaken[0];
				memTaken = resultTaken[1];
			}
			
			return totalTime;
		}
		
		private int[] addProgramstoRunning(Vector<RequiredSpecs> runningPrograms, 
				ConcurrentLinkedQueue<RequiredSpecs> waitingSpecs, int maxCpu, int cpuTaken, int maxMem, int memTaken) {
			while(waitingSpecs.size() > 0){
				int cpuNeeded = waitingSpecs.peek().getCpu();
				int memNeeded = waitingSpecs.peek().getMemory();
				if(maxCpu - cpuTaken >= cpuNeeded &&  maxMem - memTaken >= memNeeded){
					RequiredSpecs specs = waitingSpecs.poll();
					cpuTaken += specs.getCpu();
					memTaken += specs.getMemory();
					runningPrograms.addElement(specs);
				}else{
					return new int[]{cpuTaken,memTaken};
				}
			}
			return new int[]{cpuTaken,memTaken};			
		}

		protected int getMinTime(Vector<RequiredSpecs> runningPrograms){
			int min = Integer.MAX_VALUE;
			for (RequiredSpecs requiredSpecs : runningPrograms) {
				if(requiredSpecs.getTime() < min)
					min = requiredSpecs.getTime();
			}
			return min;
		}
		
		protected void removeTimeFromRunningPrograms(Vector<RequiredSpecs> runningPrograms, int time){
			for (RequiredSpecs requiredSpecs : runningPrograms) {
				requiredSpecs.setTime(requiredSpecs.getTime() - time);
			}
		}
		
		protected int[] removeEndedRunningPrograms(Vector<RequiredSpecs> runningPrograms,Integer cpuTaken,Integer memTaken){
			for (int i = 0; i <  runningPrograms.size(); i++) {
				if(runningPrograms.elementAt(i).getTime() == 0){
					cpuTaken -= runningPrograms.elementAt(i).getCpu();
					memTaken -= runningPrograms.elementAt(i).getMemory();
					runningPrograms.remove(i);
					i--;
				}	
			}
			return new int[]{cpuTaken,memTaken};
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
			
			//Allocates space in superPC or queue for client
			RequiredSpecs specs = new RequiredSpecs(cfp.getContent());
			String resultMsg = allocateClient(specs, cfp.getSender().getName());
			
			// Creates reply to inform the client
			ACLMessage result = accept.createReply();
			result.setPerformative(ACLMessage.INFORM);
			result.setContent(resultMsg);
			
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
