package logist.agent;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import logist.LogistException;
import logist.LogistPlatform;
import logist.LogistSettings.TimeoutKey;

class TimeoutGuard {

	private static ExecutorService executor = Executors.newCachedThreadPool();

	private TimeoutGuard() {
	}
	
	static void terminate() {
		executor.shutdownNow();
	}

	static <T> T schedule(String agentName, TimeoutKey key, Callable<T> task) {

		try {
			long timeout = LogistPlatform.getSettings().get(key);
			return executor.submit(task).get(timeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException cause) {
			throw new LogistException(
					"agent " + agentName + " was interrupted", cause);
		} catch (ExecutionException cause) {
			throw new LogistException(
					"agent " + agentName + " crashed", cause);
		} catch (TimeoutException cause) {
			throw new LogistException(
					"agent " + agentName + " timed out", cause);
		}
	}
	
	static void schedule(String agentName, TimeoutKey key, Runnable task) {

		try {
			long timeout = LogistPlatform.getSettings().get(key);
			executor.submit(task).get(timeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException cause) {
			throw new LogistException(
					"agent " + agentName + " was interrupted", cause);
		} catch (ExecutionException cause) {
			throw new LogistException(
					"agent " + agentName + " crashed", cause);
		} catch (TimeoutException cause) {
			throw new LogistException(
					"agent " + agentName + " timed out", cause);
		}
	}
}
