import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetInitiator;

public class AgentClient extends Agent {

	public void setup() {
		System.out.println("Hello world! I'm Client!");
		
		//ACLMessage
		
		//addBehaviour(new RequireServer(this,));
	}
	
	// http://jade.tilab.com/doc/api/jade/proto/ContractNetInitiator.html
	public class RequireServer extends ContractNetInitiator {

		public RequireServer(Agent a, ACLMessage cfp) {
			super(a, cfp);
			// TODO Auto-generated constructor stub
		}
		
		
		
	}
}
