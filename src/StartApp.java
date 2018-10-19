import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.*; 

public class StartApp {

	public static void main(String[] args) {
		
		// Get a JADE runtime
		Runtime rt = Runtime.instance();

		// Create the main container
		Profile p1 = new ProfileImpl();
		ContainerController mainContainer = rt.createMainContainer(p1);
		
		// Create an additional container
		Profile p2 = new ProfileImpl();
		ContainerController container = rt.createAgentContainer(p2);
		
		// Launch agents
		
		try {
			AgentController ac1 = container.acceptNewAgent("superPC", new AgentSuperPC());
			ac1.start();
			AgentController ac2 = container.acceptNewAgent("shadyClient", new AgentClient());
			ac2.start();
		} catch (StaleProxyException e) {
			System.out.println("oof");
			e.printStackTrace();
			System.exit(1);
		}
	}

}
