package util;

import java.util.concurrent.ConcurrentLinkedQueue;

// OPTIMIZE deeznuts
public class ClientsQueue {
	
	private ConcurrentLinkedQueue<Object[]> queue;
	private int currentWaitTime;
	
	public ClientsQueue() {
		queue  = new ConcurrentLinkedQueue<Object[]>();
		currentWaitTime = 0;
	}
	
	public synchronized void addClient(String clientName, RequiredSpecs specs) {
		Object[] client = {clientName,specs};
		queue.add(client);
		currentWaitTime += specs.getTime();
 	}
	
	public synchronized void pollClient() {
		Object[] client = queue.poll();
		RequiredSpecs specs = (RequiredSpecs)client[1];
		currentWaitTime -= specs.getTime();
	}
	
	public String peekClientName() {
		Object[] client = queue.peek();
		return (String)client[0];
	}
	
	public RequiredSpecs peekClientSpecs() {
		Object[] client = queue.peek();
		return (RequiredSpecs)client[1];
	}
	
	public int getCurrentWaitTime() {
		return this.currentWaitTime;
	}
}
