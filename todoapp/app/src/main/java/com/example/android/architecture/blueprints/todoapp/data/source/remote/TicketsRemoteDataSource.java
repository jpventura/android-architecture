/*
 * Copyright 2016, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.architecture.blueprints.todoapp.data.source.remote;

import android.os.Handler;
import android.support.annotation.NonNull;

import com.example.android.architecture.blueprints.todoapp.data.Ticket;
import com.example.android.architecture.blueprints.todoapp.data.source.TicketsDataSource;
import com.google.common.collect.Lists;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Implementation of the data source that adds a latency simulating network.
 */
public class TicketsRemoteDataSource implements TicketsDataSource {

    private static TicketsRemoteDataSource INSTANCE;

    private static final int SERVICE_LATENCY_IN_MILLIS = 5000;

    private final static Map<String, Ticket> TASKS_SERVICE_DATA;

    static {
        TASKS_SERVICE_DATA = new LinkedHashMap<>(2);
        addTask("Build tower in Pisa", "Ground looks good, no foundation work required.");
        addTask("Finish bridge in Tacoma", "Found awesome girders at half the cost!");
    }

    public static TicketsRemoteDataSource getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TicketsRemoteDataSource();
        }
        return INSTANCE;
    }

    // Prevent direct instantiation.
    private TicketsRemoteDataSource() {}

    private static void addTask(String title, String description) {
        Ticket newTicket = new Ticket(title, description);
        TASKS_SERVICE_DATA.put(newTicket.getId(), newTicket);
    }

    /**
     * Note: {@link LoadTicketsCallback#onDataNotAvailable()} is never fired. In a real remote data
     * source implementation, this would be fired if the server can't be contacted or the server
     * returns an error.
     */
    @Override
    public void getTickets(final @NonNull LoadTicketsCallback callback) {
        // Simulate network by delaying the execution.
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                callback.onTicketsLoaded(Lists.newArrayList(TASKS_SERVICE_DATA.values()));
            }
        }, SERVICE_LATENCY_IN_MILLIS);
    }

    /**
     * Note: {@link GetTicketCallback#onDataNotAvailable()} is never fired. In a real remote data
     * source implementation, this would be fired if the server can't be contacted or the server
     * returns an error.
     */
    @Override
    public void getTicket(@NonNull String taskId, final @NonNull GetTicketCallback callback) {
        final Ticket ticket = TASKS_SERVICE_DATA.get(taskId);

        // Simulate network by delaying the execution.
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                callback.onTicketLoaded(ticket);
            }
        }, SERVICE_LATENCY_IN_MILLIS);
    }

    @Override
    public void saveTicket(@NonNull Ticket ticket) {
        TASKS_SERVICE_DATA.put(ticket.getId(), ticket);
    }

    @Override
    public void completeTicket(@NonNull Ticket ticket) {
        Ticket completedTicket = new Ticket(ticket.getTitle(), ticket.getDescription(), ticket.getId(), true);
        TASKS_SERVICE_DATA.put(ticket.getId(), completedTicket);
    }

    @Override
    public void completeTicket(@NonNull String taskId) {
        // Not required for the remote data source because the {@link TicketsRepository} handles
        // converting from a {@code taskId} to a {@link task} using its cached data.
    }

    @Override
    public void activateTicket(@NonNull Ticket ticket) {
        Ticket activeTicket = new Ticket(ticket.getTitle(), ticket.getDescription(), ticket.getId());
        TASKS_SERVICE_DATA.put(ticket.getId(), activeTicket);
    }

    @Override
    public void activateTicket(@NonNull String taskId) {
        // Not required for the remote data source because the {@link TicketsRepository} handles
        // converting from a {@code taskId} to a {@link task} using its cached data.
    }

    @Override
    public void clearCompletedTickets() {
        Iterator<Map.Entry<String, Ticket>> it = TASKS_SERVICE_DATA.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Ticket> entry = it.next();
            if (entry.getValue().isCompleted()) {
                it.remove();
            }
        }
    }

    @Override
    public void refreshTickets() {
        // Not required because the {@link TicketsRepository} handles the logic of refreshing the
        // tasks from all the available data sources.
    }

    @Override
    public void deleteAllTickets() {
        TASKS_SERVICE_DATA.clear();
    }

    @Override
    public void deleteTicket(@NonNull String taskId) {
        TASKS_SERVICE_DATA.remove(taskId);
    }
}
