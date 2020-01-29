import ssixprojet.server.Server;

public class Start {
	public static void main(String[] args) throws InterruptedException {
		new Server(2206, 2080, false).startServers();
	}
}
