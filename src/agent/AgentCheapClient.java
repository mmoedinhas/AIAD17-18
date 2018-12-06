package agent;

import java.util.Vector;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import jade.lang.acl.ACLMessage;
import util.CSVUtil;

public class AgentCheapClient extends AgentClient {

	public void setup() {
		super.setup();
		addBehaviour(new RequireCheapSuperPC(this, new ACLMessage(ACLMessage.CFP)));
	}

	@Override
	public String toString() {
		return super.toString() + "]";
	}

	protected class RequireCheapSuperPC extends RequireSuperPC {

		public RequireCheapSuperPC(AgentClient a, ACLMessage cfp) {
			super(a, cfp);
		}

		/**
		 * Accepts the computer that offers the lower price.
		 */
		protected void processProposal(Vector responses, Vector acceptances) {

			double minPrice = Double.MAX_VALUE;
			int minPriceIndex = -1;
			boolean rejectedByAll = true;

			for (int i = 0; i < responses.size(); i++) {

				if (((ACLMessage) responses.get(i)).getPerformative() == ACLMessage.REFUSE) {
					System.out.println(getRejectionMsg((ACLMessage) responses.get(i)));
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

				System.out.println(getProposalMessage((ACLMessage) responses.get(i)));
			}

			if (rejectedByAll) {
				callNextAgentInQueue();
				return;
			}

			for (int i = 0; i < responses.size(); i++) {
				ACLMessage msg = ((ACLMessage) responses.get(i)).createReply();

				if (minPriceIndex == i) {
					msg.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
					// write the data in the data file using class CSVInfo
					JSONParser parser = new JSONParser();
					JSONObject content;
					try {
						content = (JSONObject) parser.parse(((ACLMessage) responses.get(i)).getContent());
						int waitingTime = ((Long) content.get("waitingTime")).intValue();
						CSVUtil.writeInfo("Cheap",arrivalTime, memoryNeeded, cpuNeeded, timeNeeded, waitingTime);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}

				else
					msg.setPerformative(ACLMessage.REJECT_PROPOSAL);

				acceptances.add(msg);
			}
		}
	}

}
