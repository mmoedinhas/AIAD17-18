package util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class CSVUtil {
	
	public static int nPCLowMem = 0;
	public static int nPCLowCpu = 0;
	public static int nPCMedMem = 0;
	public static int nPCMedCpu = 0;
	public static int nPCHighMem = 0;
	public static int nPCHighCpu = 0;
	public static String dataName;
	public static int runningTime=0;
	private static int clientsStillRunning;
	
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
	
	public static void writeInfo(String tipo, long arrival_time, int mem, int cpu, int tempoPedido, int tempoEspera) {
		try {
			File f = new File(dataName);		
		    FileWriter fw = new FileWriter(f,true); //the true will append the new data
		    BufferedWriter bw = new BufferedWriter(fw);
			String s = makeString(tipo,arrival_time,mem, cpu, tempoPedido, tempoEspera);
			bw.write(s);//appends the string to the file
			bw.close();
		
		    
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static String makeString(String tipo, long arrival_time, int mem, int cpu, int tempoPedido, int tempoEspera) {
		return tipo + ";" + arrival_time + ";"+ mem + ";" + cpu + ";" + tempoPedido + ";" + nPCLowMem + ";" + 
				nPCLowCpu + ";" + nPCMedMem + ";" + nPCMedCpu + ";" + nPCHighMem + ";" + 
				nPCHighCpu + ";" + tempoEspera + "\n";
	}
	
	public static String makeHeader() {
		return "tipo;arrival_time;memoria_pedida;processamento_pedido;tempo_pedido;n_servidores_low_mem(1000 - 3000);n_servidores_low_cpu(1000 - 3000);n_servidores_med_mem(3000 - 7000);n_servidores_med_cpu(3000 - 7000);n_servidores_high_mem(7000 - 10000);n_servidores_high_cpu(7000 - 10000); tempo_de_espera \n";
	}
	
	public static void writeHeader() {
		try {
			
			File file = new File(dataName);
		    FileWriter fw = new FileWriter(file,true); //the true will append the new data
		    if(!file.exists()) {
		    	fw.write(makeHeader());//appends the string to the file
			    fw.close();
		    }
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static synchronized void setClientsStillRunning(int clients) {
		clientsStillRunning = clients;
	}
	
	public static synchronized void decrementClientsStillRunning() {
		clientsStillRunning--;
	}
	
	public static int getClientsStillRunning() {
		return clientsStillRunning;
	}

}
