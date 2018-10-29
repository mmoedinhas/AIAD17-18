package agent;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetInitiator;

public class AgentClient extends Agent {
	
	private String name;  //client's name
	private int serverNo; //number of servers available
	private int memoryNeeded; //memory needed in KBs
	private int cpuNeeded; //cpu power needed in Mhz
	private int timeNeeded; //time needed in s

	public void setup() {
		
		initClient();
		
		System.out.println("Hello world! I'm " + this.name + "!");
		System.out.println("There are " + serverNo + " servers available.");
		System.out.println("I need " + this.memoryNeeded + "KB of memory");
		System.out.println("I need " + this.cpuNeeded + "MHz of cpu power");
		System.out.println("I want to stay in this computer for " + this.timeNeeded + " seconds ");
		
		//ACLMessage
		
		//addBehaviour(new RequireServer(this,));
	}
	
	public void initClient() {
		Object[] args = getArguments();
		
		this.name = (String)args[0];
		Integer[] quirks = (Integer[])args[1];
		
		this.serverNo = quirks[0];
		this.memoryNeeded = quirks[1];
		this.cpuNeeded = quirks[2];
		this.timeNeeded = quirks[3];
	}
	
	/*public ACLMessage createCFP() {
		
	}*/
	
	// http://jade.tilab.com/doc/api/jade/proto/ContractNetInitiator.html
	public class RequireServer extends ContractNetInitiator {

		public RequireServer(Agent a, ACLMessage cfp) {
			super(a, cfp);
			// TODO Auto-generated constructor stub
		}
		
		
		
	}
}
