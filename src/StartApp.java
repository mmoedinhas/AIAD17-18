import jade.core.Runtime;
import generator.ClientArgsGenerator;
import generator.SuperPCArgsGenerator;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.*;
import util.Config;

public class StartApp {

	/**
	 * @param args [<configFileName>]
	 **/
	public static void main(String[] args) {
		
		String configFileName = args[0];

		// Get a JADE runtime
		Runtime rt = Runtime.instance();

		// Create the main container
		Profile p1 = new ProfileImpl();
		ContainerController mainContainer = rt.createMainContainer(p1);

		// Create an additional container
		Profile p2 = new ProfileImpl();
		ContainerController container = rt.createAgentContainer(p2);
		
		// Get configurations
		Config config = new Config(configFileName);
		int clientNo = Integer.parseInt(config.getProperty("clientsNo"));
		int superPCNo = Integer.parseInt(config.getProperty("serversNo"));
		int[] memoryNeededBounds = {Integer.parseInt(config.getProperty("memoryLower")),Integer.parseInt(config.getProperty("memoryUpper"))};
		int[] cpuNeededBounds = {Integer.parseInt(config.getProperty("cpuLower")),Integer.parseInt(config.getProperty("cpuUpper"))};
		int[] timeNeededBounds = {Integer.parseInt(config.getProperty("timeLower")),Integer.parseInt(config.getProperty("timeUpper"))};

		// Launch client agents

		try {

			ClientArgsGenerator clientsGen = new ClientArgsGenerator(clientNo, superPCNo, memoryNeededBounds,
					cpuNeededBounds, timeNeededBounds);

			Integer[][] clientsQuirks = clientsGen.generate();

			for (int i = 0; i < clientsQuirks.length; i++) {
				String clientName = "client" + i;
				Object[] clientArgs = { clientName, clientsQuirks[i] };
				AgentController ac = container.createNewAgent(clientName, "agent.AgentClient", clientArgs);
				ac.start();
			}

		} catch (StaleProxyException e) {
			System.out.println("Error launching clients");
			e.printStackTrace();
			System.exit(1);
		}

		// Launch SuperPC agents

		try {

			SuperPCArgsGenerator superPcsGen = new SuperPCArgsGenerator(superPCNo, 
					memoryAvailableBounds, cpuAvailableBounds);

			Integer[][] superPcsQuirks = superPcsGen.generate();

			for (int i = 0; i < superPcsQuirks.length; i++) {
				String superPCName = "superPC" + i;
				Object[] superPCArgs = { superPCName, superPcsQuirks[i] };
				AgentController ac = container.createNewAgent(superPCName, "agent.AgentSuperPC", superPCArgs);
				ac.start();
			}

		} catch (StaleProxyException e) {
			System.out.println("Error launching superPCs");
			e.printStackTrace();
			System.exit(1);
		}
	}

}