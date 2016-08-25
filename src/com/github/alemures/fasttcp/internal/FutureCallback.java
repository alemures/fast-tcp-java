package com.github.alemures.fasttcp.internal;

public interface FutureCallback<V> {
	void onSuccess(V result);

	void onFailure(Throwable failure);
}