package util;

public class CSVInfo {
	
	public static int nPCLowMem = 0;
	public static int nPCLowCpu = 0;
	public static int nPCMedMem = 0;
	public static int nPCMedCpu = 0;
	public static int nPCHighMem = 0;
	public static int nPCHighCpu = 0;
	
	public static void addPC(int mem, int cpu) {
		if(mem >= 1000 && mem < 3000) {
			nPCLowMem++;
		} else if (mem >= 3000 && mem < 7000) {
			nPCMedMem++;
		} else {
			nPCHighMem++;
		}
		
		if(cpu >= 1000 && cpu < 3000) {
			nPCLowCpu++;
		} else if (cpu >= 3000 && cpu < 7000) {
			nPCLowCpu++;
		} else {
			nPCLowCpu++;
		}
	}

}
