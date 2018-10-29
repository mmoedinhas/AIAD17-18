package agent;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetResponder;
import jade.lang.acl.MessageTemplate;

public class AgentSuperPC extends Agent {
	
	private int memory; // PC's memory in kB
	private int cpu; // PC's cpu in MHz
	private int memoryTaken; // PC's memory taken by programs
	private int cpuTaken; // PC's cpu taken by programs

	public void setup() {
		
		initPC();
		
		/*System.out.println("Hello world! I'm " + this.getName() + "!");
		System.out.println("I have " + this.memory + "KB of memory");
		System.out.println("I have " + this.cpu + "MHz of cpu power");*/
		
		//add superPC test behaviour
		addBehaviour(new AnswerRequest(this, MessageTemplate.MatchPerformative(ACLMessage.CFP)));
	}
	
	public void initPC() {
		this.memoryTaken = 0;
		this.cpuTaken = 0;
		
		Object[] args = getArguments();

		Integer[] quirks = (Integer[])args[1];
		
		this.memory = quirks[0];
		this.cpu = quirks[1];
	}
	
	class AnswerRequest extends ContractNetResponder {
		
		public AnswerRequest(Agent a, MessageTemplate mt) {
			super(a, mt);
		}
		
		protected ACLMessage handleCfp(ACLMessage cfp) {
			ACLMessage reply = cfp.createReply();
			//reply.setPerformative(ACLMessage.PROPOSE);
			//reply.setContent("I will do it for free!!!");
			// ...
			System.out.println("content");
			System.out.println(cfp.getContent());
			return reply;
		}
		
		
	}
	
	
	public int getMemory() {
		return memory;
	}

	public int getCpu() {
		return cpu;
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

	public double getPercentageMemoryTaken() {
		return ((double) memoryTaken / memory) * 100;
	}

	public double getPercentageCpuTaken() {
		return ((double) cpuTaken / cpu) * 100;
	}

}
