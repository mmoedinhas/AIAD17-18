import jade.core.Runtime;
import generator.ClientArgsGenerator;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.*;
import util.Config; 

public class StartApp {
	
	/**
	 * @param args [<numberOfClients>,<numberOfServers>,<memoryNeededLowerBound>,<memoryNeededUpperBound>,
	 * <cpuNeededLowerBound>, <cpuNeededUpperBound>, <timeNeededLowerBound>, <timeNeededUpperBound>]
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
		Config config = new Config(configFileName);
		int clientNo = Integer.parseInt(config.getProperty("clientsNo"));
		int serverNo = Integer.parseInt(config.getProperty("serversNo"));
		int[] memoryNeededBounds = {Integer.parseInt(config.getProperty("memoryLower")),Integer.parseInt(config.getProperty("memoryUpper"))};
		int[] cpuNeededBounds = {Integer.parseInt(config.getProperty("cpuLower")),Integer.parseInt(config.getProperty("cpuUpper"))};
		int[] timeNeededBounds = {Integer.parseInt(config.getProperty("timeLower")),Integer.parseInt(config.getProperty("timeUpper"))};
		
		// Launch client agents
		
		try {
			
			ClientArgsGenerator clientsGen = new ClientArgsGenerator(
					clientNo,serverNo,memoryNeededBounds,
					cpuNeededBounds,timeNeededBounds);
			
			Integer[][] clientsQuirks = clientsGen.generate();
			
			for (int i = 0; i < clientsQuirks.length; i++) {
				String clientName = "client" + i;
				Object[] clientArgs = {clientName,clientsQuirks[i]};
				AgentController ac = container.createNewAgent(
						clientName, "agent.AgentClient", clientArgs);
				ac.start();
			}
	
		} catch (StaleProxyException e) {
			System.out.println("oof");
			e.printStackTrace();
			System.exit(1);
		}
	}

}
