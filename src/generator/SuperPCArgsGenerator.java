package generator;

/*
 * Generates array of arrays with the format
 * [<memoryAvailable>,<cpuAvailable>]
 */
public class SuperPCArgsGenerator extends Generator {
	
	private int superPCNo;  //number of superPCs to generate
	private int[] memoryAvailable;  //int[2] with lower and upper bounds for memoryAvailable
	private int[] cpuAvailable;  //int[2] with lower and upper bounds for cpuAvailable
	
	public SuperPCArgsGenerator(int superPCNo, int[] memoryAvailable,
			int[] cpuAvailable) {
		
		this.superPCNo = superPCNo;
		this.memoryAvailable = memoryAvailable;
		this.cpuAvailable = cpuAvailable;
	}
	
	public Integer[][] generate() {
		
		Integer[][] superPcs = new Integer[this.superPCNo][];
		
		for(int i = 0; i < this.superPCNo; i++){
			Integer[] superPc = new Integer[4];
			
			superPc[0] = generate(this.memoryAvailable[0],this.memoryAvailable[1]);
			superPc[1] = generate(this.cpuAvailable[0],this.cpuAvailable[1]);
			
			superPcs[i] = superPc;
		}
		return superPcs;
	}
}
