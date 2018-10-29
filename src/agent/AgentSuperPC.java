package agent;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class AgentSuperPC extends Agent {
	
	private String name; //superPC's name
	private int memory; // PC's memory in kB
	private int cpu; // PC's cpu in MHz
	private int memoryTaken; // PC's memory taken by programs
	private int cpuTaken; // PC's cpu taken by programs

	public void setup() {
		
		initPC();
		
		System.out.println("Hello world! I'm " + this.name + "!");
		System.out.println("I have " + this.memory + "KB of memory");
		System.out.println("I have " + this.cpu + "MHz of cpu power");
	}
	
	public void initPC() {
		this.memoryTaken = 0;
		this.cpuTaken = 0;
		
		Object[] args = getArguments();
		
		this.name = (String)args[0];
		Integer[] quirks = (Integer[])args[1];
		
		this.memory = quirks[0];
		this.cpu = quirks[1];
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

}
