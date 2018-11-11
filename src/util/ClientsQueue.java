package util;

import java.util.concurrent.ConcurrentLinkedQueue;

public class ClientsQueue {
	
	//queue of clients waiting
	private ConcurrentLinkedQueue<Client> queue;
	private int currentWaitTime;
	
	public class Client {
		private RequiredSpecs specs;
		private String name;
		
		public Client(RequiredSpecs specs, String name) {
			super();
			this.specs = specs;
			this.name = name;
		}

		public RequiredSpecs getSpecs() {
			return specs;
		}

		public String getName() {
			return name;
		}
	}
	
	public ClientsQueue() {
		queue  = new ConcurrentLinkedQueue<Client>();
		currentWaitTime = 0;
	}
	
	public synchronized void addClient(String clientName, RequiredSpecs specs) {
		Client client = new Client(specs,clientName);
		queue.add(client);
		currentWaitTime += specs.getTime();
		System.out.println("Added " + clientName + " to queue.");
 	}
	
	public synchronized Client pollClient() {
		Client client = queue.poll();
		RequiredSpecs specs = client.getSpecs();
		currentWaitTime -= specs.getTime();
		return client;
	}
	
	public String peekClientName() {
		Client client = queue.peek();
		return client.getName();
	}
	
	public RequiredSpecs peekClientSpecs() {
		Client client = queue.peek();
		return client.getSpecs();
	}
	
	public int getCurrentWaitTime() {
		return this.currentWaitTime;
	}
	
	public ConcurrentLinkedQueue<Client> getQueue(){
		return queue;
	}
	
	public ConcurrentLinkedQueue<RequiredSpecs> getSpecs(){

		ConcurrentLinkedQueue<RequiredSpecs> specsQueue = new ConcurrentLinkedQueue<RequiredSpecs>();
		for (Client client : queue) {
			RequiredSpecs copia = new RequiredSpecs(client.getSpecs());
			specsQueue.add(copia);
		}
		return specsQueue;
	}
	
	public int size() {
		return queue.size();
	}
}
