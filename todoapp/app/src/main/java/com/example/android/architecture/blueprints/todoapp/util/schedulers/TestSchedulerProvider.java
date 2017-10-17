package com.example.android.architecture.blueprints.todoapp.util.schedulers;

import android.support.annotation.NonNull;

import io.reactivex.Scheduler;
import io.reactivex.schedulers.TestScheduler;

/**
 * Implementation of the {@link BaseSchedulerProvider} making all {@link Scheduler}s immediate.
 */
public class TestSchedulerProvider implements BaseSchedulerProvider {

    private final Scheduler mScheduler;

    public TestSchedulerProvider() {
        mScheduler = new TestScheduler();
    }

    @NonNull
    @Override
    public Scheduler computation() {
        return mScheduler;
    }

    @NonNull
    @Override
    public Scheduler io() {
        return mScheduler;
    }

    @NonNull
    @Override
    public Scheduler ui() {
        return mScheduler;
    }

}
