import ssixprojet.server.ServerManager;

public class Start {
	public static void main(String[] args) throws InterruptedException {
		new ServerManager(2080, false).startServers();
	}
}
