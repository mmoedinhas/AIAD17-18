package agent;

import java.util.Vector;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import jade.lang.acl.ACLMessage;

public class AgentInAHurryClient extends AgentClient {
	
	public void setup() {
		super.setup();
		addBehaviour(new RequireFastQueueSuperPC(this, new ACLMessage(ACLMessage.CFP)));
	}

	protected class RequireFastQueueSuperPC extends RequireSuperPC {

		public RequireFastQueueSuperPC(AgentClient a, ACLMessage cfp) {
			super(a, cfp);
		}
		
		/**
		 * Accepts the computer that offers the lower price.
		 */
		protected void processProposal(Vector responses, Vector acceptances) {

			double minTime = Double.MAX_VALUE;
			double minTimeIndex = -1;
			boolean rejectedByAll = true;

			for (int i = 0; i < responses.size(); i++) {

				if (((ACLMessage) responses.get(i)).getPerformative() == ACLMessage.REFUSE) {
					System.out.println("Sou um cliente rejeitado :(");
					continue;
				}

				rejectedByAll = false;

				JSONParser parser = new JSONParser();
				JSONObject content;
				int proposedWaitingTime = 0;

				try {
					content = (JSONObject) parser.parse(((ACLMessage) responses.get(i)).getContent());
					proposedWaitingTime = ((Long) content.get("waitingTime")).intValue();
					String senderName = ((ACLMessage)responses.get(i)).getSender().getName();
					System.out.println(senderName + " offered me a waiting time = " + proposedWaitingTime);
				} catch (ParseException e) {
					e.printStackTrace();
				}

				if (minTime >= proposedWaitingTime) {
					minTime = proposedWaitingTime;
					minTimeIndex = i;
				}
			}

			if (rejectedByAll) {
				callNextAgentInQueue();
				return;
			}

			for (int i = 0; i < responses.size(); i++) {
				ACLMessage msg = ((ACLMessage) responses.get(i)).createReply();

				if (minTimeIndex == i)
					msg.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
				else
					msg.setPerformative(ACLMessage.REJECT_PROPOSAL);

				acceptances.add(msg);
			}
		}
	}
}
