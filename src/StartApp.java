import jade.core.Runtime;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;

import generator.ClientArgsGenerator;
import generator.SuperPCArgsGenerator;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.*;
import util.Config;

public class StartApp {

	/**
	 * @param args [<configFileName>]
	 * @throws InterruptedException
	 **/
	public static void main(String[] args) throws InterruptedException {

		String configFileName = args[0];

		// Get a JADE runtime
		Runtime rt = Runtime.instance();

		// Create the main container
		Profile p1 = new ProfileImpl();
		ContainerController mainContainer = rt.createMainContainer(p1);

		// Create an additional container
		Profile p2 = new ProfileImpl();
		ContainerController container = rt.createAgentContainer(p2);

		// Get clients configurations
		Config config = new Config(configFileName);
		String random=config.getProperty("shuffle");
		int cheapClientNo = Integer.parseInt(config.getProperty("cheapClientsNo"));
		int inAHurryClientNo = Integer.parseInt(config.getProperty("inAHurryClientsNo"));
		int smartClientNo = Integer.parseInt(config.getProperty("smartClientsNo"));
		int superPCNo = Integer.parseInt(config.getProperty("superPCNo"));
		int[] memoryNeededBounds = { Integer.parseInt(config.getProperty("clientMemoryLower")),
				Integer.parseInt(config.getProperty("clientMemoryUpper")) };
		int[] cpuNeededBounds = { Integer.parseInt(config.getProperty("clientCPULower")),
				Integer.parseInt(config.getProperty("clientCPUUpper")) };
		int[] timeNeededBounds = { Integer.parseInt(config.getProperty("clientTimeLower")),
				Integer.parseInt(config.getProperty("clientTimeUpper")) };
		String[] superPCNames = new String[superPCNo];
		
		
		try {
			// Get superPCs configurations
			int[] memoryAvailableBounds = { Integer.parseInt(config.getProperty("superPCMemoryLower")),
					Integer.parseInt(config.getProperty("superPCMemoryUpper")) };
			int[] cpuAvailableBounds = { Integer.parseInt(config.getProperty("superPCCPULower")),
					Integer.parseInt(config.getProperty("superPCCPUUpper")) };
			double[] pricePerMemoryUnitBounds = { Double.parseDouble(config.getProperty("pricePerMemoryUnitLower")),
					Double.parseDouble(config.getProperty("pricePerMemoryUnitUpper")) };
			double[] pricePerCpuUnitBounds = { Double.parseDouble(config.getProperty("pricePerCpuUnitLower")),
					Double.parseDouble(config.getProperty("pricePerCpuUnitUpper")) };
			double[] pricePerSecondBounds = { Double.parseDouble(config.getProperty("pricePerSecondLower")),
					Double.parseDouble(config.getProperty("pricePerSecondUpper")) };
			double[] discountPerWatingSecondBounds = { Double.parseDouble(config.getProperty("discountPerWatingSecondLower")),
					Double.parseDouble(config.getProperty("discountPerWatingSecondUpper")) };
			SuperPCArgsGenerator superPcsGen = new SuperPCArgsGenerator(superPCNo, memoryAvailableBounds,
					cpuAvailableBounds,pricePerMemoryUnitBounds,pricePerCpuUnitBounds,pricePerSecondBounds,discountPerWatingSecondBounds);

			Object[][] superPcsQuirks = superPcsGen.generate();
			
			// Launch SuperPC agents
			for (int i = 0; i < superPcsQuirks.length; i++) {
				String superPCName = "superPC" + i;
				Object[] superPCArgs = { superPCName, superPcsQuirks[i] };
				AgentController ac = container.createNewAgent(superPCName, "agent.AgentSuperPC", superPCArgs);
				superPCNames[i] = ac.getName();
				ac.start();
			}

		} catch (StaleProxyException e) {
			System.out.println("Error launching superPCs");
			e.printStackTrace();
			System.exit(1);
		}
		
		// Launch client agents
		int clientNo = cheapClientNo + inAHurryClientNo + smartClientNo;

		try {
			ClientArgsGenerator clientsGen = new ClientArgsGenerator(clientNo, superPCNo, memoryNeededBounds,
					cpuNeededBounds, timeNeededBounds);
			Integer[][] clientsQuirks = clientsGen.generate();
			
			Vector<AgentController> agentsVector = new Vector<AgentController>();
			ConcurrentLinkedQueue<AgentController> agentsQueue = new ConcurrentLinkedQueue<AgentController>();
			
			int i = 0;
			
			//create cheap clients
			for(int j = 0; j < cheapClientNo; i++, j++) {
				String clientName = "cheapClient" + i;
				Object[] clientArgs = { clientsQuirks[i], superPCNames, agentsQueue};
				AgentController ac = container.createNewAgent(clientName, "agent.AgentCheapClient", clientArgs);
				agentsVector.add(ac);
			}
			
			//create clients in a hurry
			for(int j = 0; j < inAHurryClientNo; i++, j++) {
				String clientName = "inAHurryClient" + i;
				Object[] clientArgs = { clientsQuirks[i], superPCNames, agentsQueue};
				AgentController ac = container.createNewAgent(clientName, "agent.AgentInAHurryClient", clientArgs);
				agentsVector.add(ac);
			}
			
			//create smart clients
			for(int j = 0; j < smartClientNo; i++, j++) {
				String clientName = "smartClient" + i;
				Object[] clientArgs = { clientsQuirks[i], superPCNames, agentsQueue};
				AgentController ac = container.createNewAgent(clientName, "agent.AgentSmartClient", clientArgs);
				agentsVector.add(ac);
			}

			if(random.compareTo("true") == 0)
				Collections.shuffle(agentsVector);

			i = 0;
			for(; i < clientNo; i++) {
				agentsQueue.add(agentsVector.elementAt(i));
			}
			
			agentsQueue.peek().start();

		} catch (StaleProxyException e) {
			System.out.println("Error launching first client");
			e.printStackTrace();
			System.exit(1);
		}

	}

}