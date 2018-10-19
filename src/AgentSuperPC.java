import jade.core.Agent;

public class AgentSuperPC extends Agent {
	
	private int memory; //PC's memory in kB
	private int cpu; //PC's cpu in MHz
	private int memoryTaken; //PC's memory taken by programs
	private int cpuTaken; //PC's cpu taken by programs
	
	public void setup() {
		System.out.println("Hello world! I'm SuperPC!");
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
	
}
