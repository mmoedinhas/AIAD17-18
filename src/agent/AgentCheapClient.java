package agent;

import java.util.Vector;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import jade.lang.acl.ACLMessage;

public class AgentCheapClient extends AgentClient {

	public void setup() {
		super.setup();
		addBehaviour(new RequireCheapSuperPC(this, new ACLMessage(ACLMessage.CFP)));
	}

	protected class RequireCheapSuperPC extends RequireSuperPC {

		public RequireCheapSuperPC(AgentClient a, ACLMessage cfp) {
			super(a, cfp);
		}

		protected void processProposal(Vector responses, Vector acceptances) {

			double minPrice = Double.MAX_VALUE;
			double minPriceIndex = -1;
			boolean rejectedByAll = true;

			for (int i = 0; i < responses.size(); i++) {

				if (((ACLMessage) responses.get(i)).getPerformative() == ACLMessage.REFUSE) {
					System.out.println("Sou um cliente rejeitado :(");
					continue;
				}

				rejectedByAll = false;

				JSONParser parser = new JSONParser();
				JSONObject content;
				double proposedPrice = 0;
				//int proposedWaitingTime = 0;

				try {
					content = (JSONObject) parser.parse(((ACLMessage) responses.get(i)).getContent());
					proposedPrice = ((double) content.get("price"));
				} catch (ParseException e) {
					e.printStackTrace();
				}

				if (minPrice >= proposedPrice) {
					minPrice = proposedPrice;
					minPriceIndex = i;
				}
			}

			if (rejectedByAll) {
				callNextAgentInQueue();
				return;
			}

			for (int i = 0; i < responses.size(); i++) {
				ACLMessage msg = ((ACLMessage) responses.get(i)).createReply();

				if (minPriceIndex == i)
					msg.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
				else
					msg.setPerformative(ACLMessage.REJECT_PROPOSAL);

				acceptances.add(msg);
			}
		}
	}

}
