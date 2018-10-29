import jade.core.Runtime;
import generator.ClientArgsGenerator;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.*; 

public class StartApp {
	
	/**
	 * @param args [<numberOfClients>,<numberOfServers>,<memoryNeededLowerBound>,<memoryNeededUpperBound>,
	 * <cpuNeededLowerBound>, <cpuNeededUpperBound>, <timeNeededLowerBound>, <timeNeededUpperBound>]
	 **/
	public static void main(String[] args) {
		
		// Get a JADE runtime
		Runtime rt = Runtime.instance();

		// Create the main container
		Profile p1 = new ProfileImpl();
		ContainerController mainContainer = rt.createMainContainer(p1);
		
		// Create an additional container
		Profile p2 = new ProfileImpl();
		ContainerController container = rt.createAgentContainer(p2);
		
		int clientNo = Integer.parseInt(args[0]);
		int serverNo = Integer.parseInt(args[1]);
		int[] memoryNeededBounds = {Integer.parseInt(args[2]),Integer.parseInt(args[3])};
		int[] cpuNeededBounds = {Integer.parseInt(args[4]),Integer.parseInt(args[5])};
		int[] timeNeededBounds = {Integer.parseInt(args[6]),Integer.parseInt(args[7])};
		
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
