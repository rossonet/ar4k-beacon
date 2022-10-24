package org.rossonet.beacon.utils;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class ThreadHelper {

	public static <RETURN_TYPE> RETURN_TYPE runWithTimeout(final Callable<RETURN_TYPE> callable, final long timeout,
			final TimeUnit timeUnit) throws Exception {
		final ExecutorService executor = Executors.newSingleThreadExecutor();
		final Future<RETURN_TYPE> future = executor.submit(callable);
		executor.shutdown();
		try {
			return future.get(timeout, timeUnit);
		} catch (final TimeoutException e) {
			future.cancel(true);
			throw e;
		} catch (final ExecutionException e) {
			final Throwable t = e.getCause();
			if (t instanceof Error) {
				throw (Error) t;
			} else if (t instanceof Exception) {
				throw e;
			} else {
				throw new IllegalStateException(t);
			}
		}
	}

	public static void runWithTimeout(final Runnable runnable, final long timeout, final TimeUnit timeUnit)
			throws Exception {
		runWithTimeout(new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				runnable.run();
				return null;
			}
		}, timeout, timeUnit);
	}

	private ThreadHelper() {
		throw new UnsupportedOperationException("Just for static usage");

	}

}
