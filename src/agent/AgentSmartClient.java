package agent;

import java.util.Collections;
import java.util.Vector;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import generator.Generator;
import jade.lang.acl.ACLMessage;
import util.CSVUtil;

public class AgentSmartClient extends AgentClient {

	private static final double[] possibleDecisionWeights = { 0.25, 0.5, 0.75 };
	private double decisionWeight;

	public void setup() {
		super.setup();

		Generator gen = new Generator();
		int i = gen.generate(0, 2);
		this.decisionWeight = possibleDecisionWeights[i];

		addBehaviour(new RequireBestSuperPC(this, new ACLMessage(ACLMessage.CFP)));
	}

	@Override
	public String toString() {
		return super.toString() + ", priceWeight=" + decisionWeight + ", queueTimeWeight=" + (1-decisionWeight) + "]";
	}

	protected class RequireBestSuperPC extends RequireSuperPC {

		public RequireBestSuperPC(AgentClient a, ACLMessage cfp) {
			super(a, cfp);
		}

		/**
		 * Accepts the computer that offers the combination between lower price and
		 * queue time.
		 */
		protected void processProposal(Vector responses, Vector acceptances) {

			Vector<Double> proposedPrices = new Vector<Double>();
			Vector<Integer> proposedWaitingTimes = new Vector<Integer>();
			boolean rejectedByAll = true;

			for (int i = 0; i < responses.size(); i++) {

				if (((ACLMessage) responses.get(i)).getPerformative() == ACLMessage.REFUSE) {
					System.out.println(getRejectionMsg((ACLMessage) responses.get(i)));
					proposedPrices.add(Double.MAX_VALUE);
					proposedWaitingTimes.add(Integer.MAX_VALUE);
					continue;
				}

				rejectedByAll = false;

				JSONParser parser = new JSONParser();
				JSONObject content;
				try {
					content = (JSONObject) parser.parse(((ACLMessage) responses.get(i)).getContent());
				} catch (ParseException e1) {
					e1.printStackTrace();
					proposedPrices.add(Double.MAX_VALUE);
					proposedWaitingTimes.add(Integer.MAX_VALUE);
					continue;
				}

				double proposedPrice = (double) content.get("price");
				proposedPrices.add(proposedPrice);

				int proposedWaitingTime = ((Long) content.get("waitingTime")).intValue();
				proposedWaitingTimes.add(proposedWaitingTime);
				
				System.out.println(getProposalMessage((ACLMessage) responses.get(i)));
			}

			if (rejectedByAll) {
				callNextAgentInQueue();
				return;
			}

			int chosenPCIndex = getBestSuperPC(proposedPrices, proposedWaitingTimes);

			for (int i = 0; i < responses.size(); i++) {
				ACLMessage msg = ((ACLMessage) responses.get(i)).createReply();

				if (chosenPCIndex == i) {
					msg.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
					// write the data in the data file using class CSVInfo
					JSONParser parser = new JSONParser();
					JSONObject content;
					try {
						content = (JSONObject) parser.parse(((ACLMessage) responses.get(i)).getContent());
						int waitingTime = ((Long) content.get("waitingTime")).intValue();
						CSVUtil.writeInfo(1,arrivalTime,memoryNeeded, cpuNeeded, timeNeeded, waitingTime);
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

		protected Object[] getCheapestSuperPC(Vector<Double> proposedPrices) {
			Double minPrice = Collections.min(proposedPrices);
			Integer index = proposedPrices.indexOf(minPrice);
			Object[] returnValue = { index, minPrice };
			return returnValue;
		}

		protected boolean hasQueue(int index, Vector<Integer> proposedWaitingTimes) {
			return !(proposedWaitingTimes.get(index) == 0);
		}

		protected int getBestSuperPC(Vector<Double> proposedPrices, Vector<Integer> proposedWaitingTimes) {

			double priceWeight = decisionWeight;
			double timeWeight = 1 - decisionWeight;

			Vector<Double> decisions = new Vector<Double>();

			for (int i = 0; i < proposedPrices.size(); i++) {
				double proposedPrice = proposedPrices.get(i);
				double proposedWaitingTime = (double) proposedWaitingTimes.get(i);
				double decision = proposedPrice * priceWeight + proposedWaitingTime * timeWeight;

				decisions.add(decision);
			}

			double minDecision = Collections.min(decisions);

			return decisions.indexOf(minDecision);
		}
	}

}
