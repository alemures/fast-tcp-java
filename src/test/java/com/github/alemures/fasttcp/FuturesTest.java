package com.github.alemures.fasttcp;

import com.github.alemures.fasttcp.futures.FutureCallback;
import com.github.alemures.fasttcp.futures.FutureExecutor;
import com.github.alemures.fasttcp.futures.ListenableFuture;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class FuturesTest {
    private static ExecutorService executor;

    @BeforeClass
    public static void startExecutorService() {
        executor = Executors.newSingleThreadExecutor();
    }

    @AfterClass
    public static void stopExecutorService() {
        executor.shutdown();
    }

    @Test
    public void shouldCompleteASuccessfulTask() throws InterruptedException {
        FutureExecutor futureExecutor = new FutureExecutor(executor);
        ListenableFuture<Boolean> future = futureExecutor.submit(isPrimeCallable(5));
        final CountDownLatch latch = new CountDownLatch(1);

        final AtomicBoolean onSuccessExecuted = new AtomicBoolean(false);
        final AtomicBoolean onFailureExecuted = new AtomicBoolean(false);
        future.addCallback(new FutureCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                onSuccessExecuted.set(true);
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable failure) {
                onFailureExecuted.set(true);
                latch.countDown();
            }
        });

        latch.await();
        Assert.assertEquals("onSuccess was not executed", true, onSuccessExecuted.get());
        Assert.assertEquals("onFailure was executed", false, onFailureExecuted.get());
    }

    @Test
    public void shouldCompleteAFailurefulTask() throws InterruptedException {
        FutureExecutor futureExecutor = new FutureExecutor(executor);
        ListenableFuture<Boolean> future = futureExecutor.submit(isPrimeCallable(0));
        final CountDownLatch latch = new CountDownLatch(1);

        final AtomicBoolean onSuccessExecuted = new AtomicBoolean(false);
        final AtomicBoolean onFailureExecuted = new AtomicBoolean(false);
        future.addCallback(new FutureCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                onSuccessExecuted.set(true);
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable failure) {
                onFailureExecuted.set(true);
                latch.countDown();
            }
        });

        latch.await();
        Assert.assertEquals("onSuccess was executed", false, onSuccessExecuted.get());
        Assert.assertEquals("onFailure was not executed", true, onFailureExecuted.get());
    }

    private Callable<Boolean> isPrimeCallable(final int number) {
        return new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return isPrime(number);
            }
        };
    }

    private boolean isPrime(int number) throws Exception {
        if (number <= 1) {
            throw new Exception("number has to be greater than 1");
        }

        int limit = (int) Math.sqrt(number);
        for (int i = 2; i <= limit; i++) {
            if (number % i == 0) {
                return false;
            }
        }
        return true;
    }
}
