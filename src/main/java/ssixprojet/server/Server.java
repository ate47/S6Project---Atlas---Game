package ssixprojet.server;

public abstract class Server extends Thread {

	@Override
	public void run() {
		try {
			startServer();
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.run();
	}

	/**
	 * start the server
	 * 
	 * @throws Exception
	 *             all exception the server can return
	 */
	protected abstract void startServer() throws Exception;
}
