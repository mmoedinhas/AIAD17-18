package generator;

/*
 * Generates array of arrays with the format
 * [<numberOfServers>,<memoryNeeded>,<cpuNeeded>,<timeNeeded>]
 */
public class ClientArgsGenerator extends Generator {
	
	private int clientNo; //number of clients to generate
	private int serverNo; //number of servers available
	private int[] memoryNeeded;  //int[2] with lower and upper bounds for memoryNeeded
	private int[] cpuNeeded;  //int[2] with lower and upper bounds for cpuNeeded
	private int[] timeNeeded;  //int[2] with lower and upper bounds for timeNeeded
	
	public ClientArgsGenerator(int clientNo, int serverNo, int[] memoryNeeded,
			int[] cpuNeeded, int[] timeNeeded) {
		
		this.clientNo = clientNo;
		this.serverNo = serverNo;
		this.memoryNeeded = memoryNeeded;
		this.cpuNeeded = cpuNeeded;
		this.timeNeeded = timeNeeded;
	}
	
	public Integer[][] generate() {
		
		Integer[][] clients = new Integer[this.clientNo][];
		
		for(int i = 0; i < this.clientNo; i++){
			Integer[] client = new Integer[4];
			
			client[0] = this.serverNo;
			client[1] = generate(this.memoryNeeded[0],this.memoryNeeded[1]);
			client[2] = generate(this.cpuNeeded[0], this.cpuNeeded[1]);
			client[3] = generate(this.timeNeeded[0], this.timeNeeded[1]);
			
			clients[i] = client;
		}
		return clients;
	}
	
}
