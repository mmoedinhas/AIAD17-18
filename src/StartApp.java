import jade.core.Runtime;
import generator.ClientArgsGenerator;
import generator.SuperPCArgsGenerator;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.*;

public class StartApp {

	/**
	 * @param args
	 *            [<numberOfClients>,<numberOfSuperPCs>,<memoryNeededLowerBound>,<memoryNeededUpperBound>,
	 *            <cpuNeededLowerBound>, <cpuNeededUpperBound>,
	 *            <timeNeededLowerBound>, <timeNeededUpperBound>,
	 *            <memoryAvailableLowerBound>, <memoryAvailableUpperBound>,
	 *            <cpuAvailableLowerBound>, <cpuAvailableUpperBound>]
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
		int superPCNo = Integer.parseInt(args[1]);
		int[] memoryNeededBounds = { Integer.parseInt(args[2]), Integer.parseInt(args[3]) };
		int[] cpuNeededBounds = { Integer.parseInt(args[4]), Integer.parseInt(args[5]) };
		int[] timeNeededBounds = { Integer.parseInt(args[6]), Integer.parseInt(args[7]) };
		int[] memoryAvailableBounds = { Integer.parseInt(args[8]), Integer.parseInt(args[9]) };
		int[] cpuAvailableBounds = { Integer.parseInt(args[10]), Integer.parseInt(args[11]) };

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
