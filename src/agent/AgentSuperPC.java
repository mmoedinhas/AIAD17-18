package agent;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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
		AgentSuperPC agent;
		public AnswerRequest(AgentSuperPC a, MessageTemplate mt) {
			super(a, mt);
			this.agent = a;
		}
		
		protected ACLMessage handleCfp(ACLMessage cfp) {
			ACLMessage reply = cfp.createReply();
			reply.setPerformative(ACLMessage.PROPOSE);
			
			// ...
			JSONParser parser = new JSONParser();
			JSONObject content;
			try {
				content = (JSONObject) parser.parse(cfp.getContent());
				int memoryNeeded = ((Long)content.get("memoryNeeded")).intValue();
				int cpuNeeded = ((Long)content.get("cpuNeeded")).intValue();
				int timeNeeded = ((Long)content.get("timeNeeded")).intValue();
				boolean accept = canAccept(memoryNeeded,cpuNeeded);
				reply.setContent(createResponse(accept));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			
			return reply;
		}
		
		/**
		 * Creates reply to the proposal
		 * @param accept
		 * @return reply to proposal
		 */
		private String createResponse(boolean accept) {
			JSONObject response = new JSONObject();
			response.put("Accept", accept);
			return response.toJSONString();
		}

		/**
		 * Checks if we can accept the proposal
		 * @param memoryNeeded
		 * @param cpuNeeded
		 * @return true if superPC has enough memory and cpu available
		 */
		protected boolean canAccept(int memoryNeeded, int cpuNeeded){
			if(agent.getMemoryAvailable() > memoryNeeded && agent.getCpuAvailable() > cpuNeeded){
				return true;
			}
			return false;
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

	public double getPercentageMemoryTaken() {
		return ((double) memoryTaken / memory) * 100;
	}

	public double getPercentageCpuTaken() {
		return ((double) cpuTaken / cpu) * 100;
	}

}
