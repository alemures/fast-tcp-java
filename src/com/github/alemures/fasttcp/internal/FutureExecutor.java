package com.github.alemures.fasttcp.internal;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

public class FutureExecutor {
	private ExecutorService executor;

	public FutureExecutor(ExecutorService executor) {
		this.executor = executor;
	}

	public <V> ListenableFuture<V> submit(final Callable<V> callable) {
		final ListenableFuture<V> future = new ListenableFuture<>();
		executor.submit(new Callable<V>() {
			@Override
			public V call() throws Exception {
				try {
					V result = callable.call();
					future.setResult(result);
					return result;
				} catch (Exception e) {
					future.setFailure(e);
					throw e;
				}
			}
		});

		return future;
	}
}
