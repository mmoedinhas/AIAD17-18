package agent;

import java.util.Vector;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import jade.lang.acl.ACLMessage;

public class AgentSmartClient extends AgentClient {

	public void setup() {
		super.setup();
		addBehaviour(new RequireBestSuperPC(this, new ACLMessage(ACLMessage.CFP)));
	}

	protected class RequireBestSuperPC extends RequireSuperPC {

		public RequireBestSuperPC(AgentClient a, ACLMessage cfp) {
			super(a, cfp);
		}
		
		/**
		 * Accepts the computer that offers the combination between lower price and queue time.
		 */
		protected void processProposal(Vector responses, Vector acceptances) {
			
			//ve o mais barato
			//se o mais barato tiver queue, ve se outros nao tem queue
			//se todos tiverem queue, escolher o com menor queue
			
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
