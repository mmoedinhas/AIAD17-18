package util;

import agent.AgentSuperPC;

public class Timer implements Runnable {
	
	private AgentSuperPC superPC;
	private String clientName;

	public Timer(AgentSuperPC superPC, String clientName) {
		this.superPC = superPC;
		this.clientName = clientName;
	}

	@Override
	public void run() {
		System.out.println("Time for " + clientName + " to leave the " + superPC.getName());
		this.superPC.prepareNextClient(clientName);
	}

}
