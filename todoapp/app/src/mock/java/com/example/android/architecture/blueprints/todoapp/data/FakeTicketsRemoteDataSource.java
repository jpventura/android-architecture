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

package com.example.android.architecture.blueprints.todoapp.data;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.example.android.architecture.blueprints.todoapp.data.source.TicketsDataSource;
import com.google.common.collect.Lists;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Implementation of a remote data source with static access to the data for easy testing.
 */
public class FakeTicketsRemoteDataSource implements TicketsDataSource {

    private static FakeTicketsRemoteDataSource INSTANCE;

    private static final Map<String, Ticket> TASKS_SERVICE_DATA = new LinkedHashMap<>();

    // Prevent direct instantiation.
    private FakeTicketsRemoteDataSource() {}

    public static FakeTicketsRemoteDataSource getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FakeTicketsRemoteDataSource();
        }
        return INSTANCE;
    }

    @Override
    public void getTickets(@NonNull LoadTicketsCallback callback) {
        callback.onTicketsLoaded(Lists.newArrayList(TASKS_SERVICE_DATA.values()));
    }

    @Override
    public void getTicket(@NonNull String taskId, @NonNull GetTicketCallback callback) {
        Ticket ticket = TASKS_SERVICE_DATA.get(taskId);
        callback.onTicketLoaded(ticket);
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
        // Not required for the remote data source.
    }

    @Override
    public void activateTicket(@NonNull Ticket ticket) {
        Ticket activeTicket = new Ticket(ticket.getTitle(), ticket.getDescription(), ticket.getId());
        TASKS_SERVICE_DATA.put(ticket.getId(), activeTicket);
    }

    @Override
    public void activateTicket(@NonNull String taskId) {
        // Not required for the remote data source.
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

    public void refreshTickets() {
        // Not required because the {@link TicketsRepository} handles the logic of refreshing the
        // tasks from all the available data sources.
    }

    @Override
    public void deleteTicket(@NonNull String taskId) {
        TASKS_SERVICE_DATA.remove(taskId);
    }

    @Override
    public void deleteAllTickets() {
        TASKS_SERVICE_DATA.clear();
    }

    @VisibleForTesting
    public void addTasks(Ticket... tickets) {
        for (Ticket ticket : tickets) {
            TASKS_SERVICE_DATA.put(ticket.getId(), ticket);
        }
    }
}
