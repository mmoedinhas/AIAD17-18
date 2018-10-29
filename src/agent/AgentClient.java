package agent;
import java.util.Vector;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetInitiator;
import org.json.simple.JSONObject;

public class AgentClient extends Agent {
	
	private int serverNo; //number of servers available
	private int memoryNeeded; //memory needed in KBs
	private int cpuNeeded; //cpu power needed in Mhz
	private int timeNeeded; //time needed in s
	private String[] superPCsNames; //names of the superPCs

	public void setup() {
		
		initClient();
		
		/*System.out.println("Hello world! I'm " + this.getName() + "!");
		System.out.println("There are " + serverNo + " servers available.");
		System.out.println("I need " + this.memoryNeeded + "KB of memory");
		System.out.println("I need " + this.cpuNeeded + "MHz of cpu power");
		System.out.println("I want to stay in this computer for " + this.timeNeeded + " seconds ");*/
			
		//add test client behaviour
		addBehaviour(new RequireSuperPC(this, new ACLMessage(ACLMessage.CFP)));
	
	}
	
	public void initClient() {
		Object[] args = getArguments();
		
		Integer[] quirks = (Integer[])args[0];
		String[] superPCNames = (String[])args[1];
		this.serverNo = quirks[0];
		this.memoryNeeded = quirks[1];
		this.cpuNeeded = quirks[2];
		this.timeNeeded = quirks[3];
		this.superPCsNames= superPCNames;
	}
	
	/*public ACLMessage createCFP() {
		
	}*/
	
	// http://jade.tilab.com/doc/api/jade/proto/ContractNetInitiator.html
	public class RequireSuperPC extends ContractNetInitiator {

		private AgentClient agent;
		
		public RequireSuperPC(AgentClient a, ACLMessage cfp) {
			super(a, cfp);
			this.agent = a;
			// TODO Auto-generated constructor stub
		}
		//private int serverNo; //number of servers available
		//private int memoryNeeded; //memory needed in KBs
		//private int cpuNeeded; //cpu power needed in Mhz
		//private int timeNeeded; //time needed in s
		
		protected Vector prepareCfps(ACLMessage cfp) {
			Vector v = new Vector();
			
			for (int i = 0; i < superPCsNames.length; i++) {
				System.out.println(superPCsNames[i]);
				System.out.println(i);
				cfp.addReceiver(new AID(superPCsNames[i],AID.ISGUID));
			}
			
			//create message content
			JSONObject messageContent = new JSONObject();
			messageContent.put("memoryNeeded",this.agent.memoryNeeded);
			messageContent.put("cpuNeeded",this.agent.cpuNeeded);
			messageContent.put("timeNeeded",this.agent.timeNeeded);
			
			cfp.setContent(messageContent.toJSONString());
			v.add(cfp);
			return v;
		}
		
		
		
	}
}
