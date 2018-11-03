package generator;

/*
 * Generates array of arrays with the format
 * [<memoryAvailable>,<cpuAvailable>]
 */
public class SuperPCArgsGenerator extends Generator {
	
	private int superPCNo;  //number of superPCs to generate
	private int[] memoryAvailable;  //int[2] with lower and upper bounds for memoryAvailable
	private int[] cpuAvailable;  //int[2] with lower and upper bounds for cpuAvailable
	private double[] pricePerMemoryUnit; //int[2] with lower and upper bounds for pricePerMemoryUnit
	private double[] pricePerCpuUnit; //int[2] with lower and upper bounds for pricePerCpuUnit
	private double[] pricePerSecond; //int[2] with lower and upper bounds for pricePerSecond
	
	public SuperPCArgsGenerator(int superPCNo, int[] memoryAvailable,
			int[] cpuAvailable, double[] pricePerMemoryUnit, double[] pricePerCpuUnit, double[] pricePerSecond) {
		
		this.superPCNo = superPCNo;
		this.memoryAvailable = memoryAvailable;
		this.cpuAvailable = cpuAvailable;
		this.pricePerMemoryUnit = pricePerMemoryUnit;
		this.pricePerCpuUnit = pricePerCpuUnit;
		this.pricePerSecond = pricePerSecond;
	}
	
	public Object[][] generate() {
		
		Object[][] superPcs = new Object[this.superPCNo][];
		
		for(int i = 0; i < this.superPCNo; i++){
			Object[] superPc = new Object[5];
			
			superPc[0] = generate(this.memoryAvailable[0],this.memoryAvailable[1]);
			superPc[1] = generate(this.cpuAvailable[0],this.cpuAvailable[1]);
			superPc[2] = generate(this.pricePerMemoryUnit[0],this.pricePerMemoryUnit[1]);
			superPc[3] = generate(this.pricePerCpuUnit[0],this.pricePerCpuUnit[1]);
			superPc[4] = generate(this.pricePerSecond[0],this.pricePerSecond[1]);
			
			superPcs[i] = superPc;
		}
		return superPcs;
	}
}
