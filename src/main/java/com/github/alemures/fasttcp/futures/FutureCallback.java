package com.github.alemures.fasttcp.futures;

public interface FutureCallback<V> {
    void onSuccess(V result);

    void onFailure(Throwable failure);
}