package ssixprojet.server;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class GameServer extends Server {
	private final BlockingQueue<Runnable> actions = new ArrayBlockingQueue<>(1024);
	private boolean started;
	private AtlasGame atlas;
	
	public GameServer(AtlasGame atlas) {
		super("GameServer");
		this.atlas = atlas;
	}
	
	@Override
	protected void startServer() throws Exception {
		started = true;
		final long rate = 1000 / 20;
		while (started) {
			long start = System.currentTimeMillis();
			
			executeActions();
			atlas.tick();
			
			long deltaTime = start + rate - System.currentTimeMillis();
			if (deltaTime > 0)
				sleepNE(deltaTime);
		}
	}
	
	private void executeActions() {
		Runnable r;
		
		while ((r = actions.poll()) != null) {
			r.run();
		}
	}

	
	public void registerAction(Runnable action) {
		try {
			actions.put(action);
		} catch (InterruptedException e) {
		}
	}
	
	private void sleepNE(long millis) {
		try {
			sleep(millis);
		} catch (Exception e) {}
	}
	
	public void stopServer() {
		started = false;
	}

}
