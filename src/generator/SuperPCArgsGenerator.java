package generator;

/*
 * Generates array of arrays with the format
 * [<memoryAvailable>,<cpuAvailable>]
 */
public class SuperPCArgsGenerator extends Generator {
	private final int fieldsNo = 6;
	
	private int superPCNo;  //number of superPCs to generate
	private int[] memoryAvailable;  //int[2] with lower and upper bounds for memoryAvailable
	private int[] cpuAvailable;  //int[2] with lower and upper bounds for cpuAvailable
	private double[] pricePerMemoryUnit; //double[2] with lower and upper bounds for pricePerMemoryUnit
	private double[] pricePerCpuUnit; //double[2] with lower and upper bounds for pricePerCpuUnit
	private double[] pricePerSecond; //double[2] with lower and upper bounds for pricePerSecond
	private double[] discountPerWatingSecond; //double[2] with lower and upper bounds for discountPerWatingSecond
	
	public SuperPCArgsGenerator(int superPCNo, int[] memoryAvailable,
			int[] cpuAvailable, double[] pricePerMemoryUnit, double[] pricePerCpuUnit, double[] pricePerSecond, double[] discountPerWatingSecond) {
		
		this.superPCNo = superPCNo;
		this.memoryAvailable = memoryAvailable;
		this.cpuAvailable = cpuAvailable;
		this.pricePerMemoryUnit = pricePerMemoryUnit;
		this.pricePerCpuUnit = pricePerCpuUnit;
		this.pricePerSecond = pricePerSecond;
		this.discountPerWatingSecond = discountPerWatingSecond;
	}
	
	public Object[][] generate() {
		
		Object[][] superPcs = new Object[this.superPCNo][];
		
		for(int i = 0; i < this.superPCNo; i++){
			Object[] superPc = new Object[fieldsNo];
			
			superPc[0] = generate(this.memoryAvailable[0],this.memoryAvailable[1]);
			superPc[1] = generate(this.cpuAvailable[0],this.cpuAvailable[1]);
			superPc[2] = generate(this.pricePerMemoryUnit[0],this.pricePerMemoryUnit[1]);
			superPc[3] = generate(this.pricePerCpuUnit[0],this.pricePerCpuUnit[1]);
			superPc[4] = generate(this.pricePerSecond[0],this.pricePerSecond[1]);
			superPc[5] = generate(this.discountPerWatingSecond[0],this.discountPerWatingSecond[1]);
			
			superPcs[i] = superPc;
		}
		return superPcs;
	}
}
