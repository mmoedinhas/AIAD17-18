import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class AgentSuperPC extends Agent {

	private int memory; // PC's memory in kB
	private int cpu; // PC's cpu in MHz
	private int memoryTaken; // PC's memory taken by programs
	private int cpuTaken; // PC's cpu taken by programs

	public void setup() {
		addBehaviour(new ListeningBehaviour());
	}

	public int getMemory() {
		return memory;
	}

	public int getCpu() {
		return cpu;
	}

	public int getMemoryTaken() {
		return memoryTaken;
	}

	public void setMemoryTaken(int memoryTaken) {
		this.memoryTaken = memoryTaken;
	}

	public int getCpuTaken() {
		return cpuTaken;
	}

	public void setCpuTaken(int cpuTaken) {
		this.cpuTaken = cpuTaken;
	}

	public double getPercentageMemoryTaken() {
		return ((double) memoryTaken / memory) * 100;
	}

	public double getPercentageCpuTaken() {
		return ((double) cpuTaken / cpu) * 100;
	}

	class ListeningBehaviour extends CyclicBehaviour {

		public void action() {
			ACLMessage msg = receive();
			if (msg != null) {
				System.out.println(msg);
				ACLMessage reply = msg.createReply();
				reply.setPerformative(ACLMessage.INFORM);
				reply.setContent("Got your message!");
				send(reply);
			} else {
				block();
			}
		}
	}

}
