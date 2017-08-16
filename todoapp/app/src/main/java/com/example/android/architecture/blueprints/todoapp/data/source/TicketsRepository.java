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

package com.example.android.architecture.blueprints.todoapp.data.source;

import static com.google.common.base.Preconditions.checkNotNull;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.android.architecture.blueprints.todoapp.data.Ticket;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Concrete implementation to load tasks from the data sources into a cache.
 * <p>
 * For simplicity, this implements a dumb synchronisation between locally persisted data and data
 * obtained from the server, by using the remote data source only if the local database doesn't
 * exist or is empty.
 */
public class TicketsRepository implements TicketsDataSource {

    private static TicketsRepository INSTANCE = null;

    private final TicketsDataSource mTicketsRemoteDataSource;

    private final TicketsDataSource mTicketLocalDataSource;

    /**
     * This variable has package local visibility so it can be accessed from tests.
     */
    Map<String, Ticket> mCachedTickets;

    /**
     * Marks the cache as invalid, to force an update the next time data is requested. This variable
     * has package local visibility so it can be accessed from tests.
     */
    boolean mCacheIsDirty = false;

    // Prevent direct instantiation.
    private TicketsRepository(@NonNull TicketsDataSource tasksRemoteDataSource,
                              @NonNull TicketsDataSource tasksLocalDataSource) {
        mTicketsRemoteDataSource = checkNotNull(tasksRemoteDataSource);
        mTicketLocalDataSource = checkNotNull(tasksLocalDataSource);
    }

    /**
     * Returns the single instance of this class, creating it if necessary.
     *
     * @param tasksRemoteDataSource the backend data source
     * @param tasksLocalDataSource  the device storage data source
     * @return the {@link TicketsRepository} instance
     */
    public static TicketsRepository getInstance(TicketsDataSource tasksRemoteDataSource,
                                                TicketsDataSource tasksLocalDataSource) {
        if (INSTANCE == null) {
            INSTANCE = new TicketsRepository(tasksRemoteDataSource, tasksLocalDataSource);
        }
        return INSTANCE;
    }

    /**
     * Used to force {@link #getInstance(TicketsDataSource, TicketsDataSource)} to create a new instance
     * next time it's called.
     */
    public static void destroyInstance() {
        INSTANCE = null;
    }

    /**
     * Gets tasks from cache, local data source (SQLite) or remote data source, whichever is
     * available first.
     * <p>
     * Note: {@link LoadTicketsCallback#onDataNotAvailable()} is fired if all data sources fail to
     * get the data.
     */
    @Override
    public void getTickets(@NonNull final LoadTicketsCallback callback) {
        checkNotNull(callback);

        // Respond immediately with cache if available and not dirty
        if (mCachedTickets != null && !mCacheIsDirty) {
            callback.onTicketsLoaded(new ArrayList<>(mCachedTickets.values()));
            return;
        }

        if (mCacheIsDirty) {
            // If the cache is dirty we need to fetch new data from the network.
            getTasksFromRemoteDataSource(callback);
        } else {
            // Query the local storage if available. If not, query the network.
            mTicketLocalDataSource.getTickets(new LoadTicketsCallback() {
                @Override
                public void onTicketsLoaded(List<Ticket> tickets) {
                    refreshCache(tickets);
                    callback.onTicketsLoaded(new ArrayList<>(mCachedTickets.values()));
                }

                @Override
                public void onDataNotAvailable() {
                    getTasksFromRemoteDataSource(callback);
                }
            });
        }
    }

    @Override
    public void saveTicket(@NonNull Ticket ticket) {
        checkNotNull(ticket);
        mTicketsRemoteDataSource.saveTicket(ticket);
        mTicketLocalDataSource.saveTicket(ticket);

        // Do in memory cache update to keep the app UI up to date
        if (mCachedTickets == null) {
            mCachedTickets = new LinkedHashMap<>();
        }
        mCachedTickets.put(ticket.getId(), ticket);
    }

    @Override
    public void completeTicket(@NonNull Ticket ticket) {
        checkNotNull(ticket);
        mTicketsRemoteDataSource.completeTicket(ticket);
        mTicketLocalDataSource.completeTicket(ticket);

        Ticket completedTicket = new Ticket(ticket.getTitle(), ticket.getDescription(), ticket.getId(), true);

        // Do in memory cache update to keep the app UI up to date
        if (mCachedTickets == null) {
            mCachedTickets = new LinkedHashMap<>();
        }
        mCachedTickets.put(ticket.getId(), completedTicket);
    }

    @Override
    public void completeTicket(@NonNull String taskId) {
        checkNotNull(taskId);
        completeTicket(getTaskWithId(taskId));
    }

    @Override
    public void activateTicket(@NonNull Ticket ticket) {
        checkNotNull(ticket);
        mTicketsRemoteDataSource.activateTicket(ticket);
        mTicketLocalDataSource.activateTicket(ticket);

        Ticket activeTicket = new Ticket(ticket.getTitle(), ticket.getDescription(), ticket.getId());

        // Do in memory cache update to keep the app UI up to date
        if (mCachedTickets == null) {
            mCachedTickets = new LinkedHashMap<>();
        }
        mCachedTickets.put(ticket.getId(), activeTicket);
    }

    @Override
    public void activateTicket(@NonNull String taskId) {
        checkNotNull(taskId);
        activateTicket(getTaskWithId(taskId));
    }

    @Override
    public void clearCompletedTickets() {
        mTicketsRemoteDataSource.clearCompletedTickets();
        mTicketLocalDataSource.clearCompletedTickets();

        // Do in memory cache update to keep the app UI up to date
        if (mCachedTickets == null) {
            mCachedTickets = new LinkedHashMap<>();
        }
        Iterator<Map.Entry<String, Ticket>> it = mCachedTickets.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Ticket> entry = it.next();
            if (entry.getValue().isCompleted()) {
                it.remove();
            }
        }
    }

    /**
     * Gets tasks from local data source (sqlite) unless the table is new or empty. In that case it
     * uses the network data source. This is done to simplify the sample.
     * <p>
     * Note: {@link GetTicketCallback#onDataNotAvailable()} is fired if both data sources fail to
     * get the data.
     */
    @Override
    public void getTicket(@NonNull final String taskId, @NonNull final GetTicketCallback callback) {
        checkNotNull(taskId);
        checkNotNull(callback);

        Ticket cachedTicket = getTaskWithId(taskId);

        // Respond immediately with cache if available
        if (cachedTicket != null) {
            callback.onTicketLoaded(cachedTicket);
            return;
        }

        // Load from server/persisted if needed.

        // Is the task in the local data source? If not, query the network.
        mTicketLocalDataSource.getTicket(taskId, new GetTicketCallback() {
            @Override
            public void onTicketLoaded(Ticket ticket) {
                // Do in memory cache update to keep the app UI up to date
                if (mCachedTickets == null) {
                    mCachedTickets = new LinkedHashMap<>();
                }
                mCachedTickets.put(ticket.getId(), ticket);
                callback.onTicketLoaded(ticket);
            }

            @Override
            public void onDataNotAvailable() {
                mTicketsRemoteDataSource.getTicket(taskId, new GetTicketCallback() {
                    @Override
                    public void onTicketLoaded(Ticket ticket) {
                        // Do in memory cache update to keep the app UI up to date
                        if (mCachedTickets == null) {
                            mCachedTickets = new LinkedHashMap<>();
                        }
                        mCachedTickets.put(ticket.getId(), ticket);
                        callback.onTicketLoaded(ticket);
                    }

                    @Override
                    public void onDataNotAvailable() {
                        callback.onDataNotAvailable();
                    }
                });
            }
        });
    }

    @Override
    public void refreshTickets() {
        mCacheIsDirty = true;
    }

    @Override
    public void deleteAllTickets() {
        mTicketsRemoteDataSource.deleteAllTickets();
        mTicketLocalDataSource.deleteAllTickets();

        if (mCachedTickets == null) {
            mCachedTickets = new LinkedHashMap<>();
        }
        mCachedTickets.clear();
    }

    @Override
    public void deleteTicket(@NonNull String taskId) {
        mTicketsRemoteDataSource.deleteTicket(checkNotNull(taskId));
        mTicketLocalDataSource.deleteTicket(checkNotNull(taskId));

        mCachedTickets.remove(taskId);
    }

    private void getTasksFromRemoteDataSource(@NonNull final LoadTicketsCallback callback) {
        mTicketsRemoteDataSource.getTickets(new LoadTicketsCallback() {
            @Override
            public void onTicketsLoaded(List<Ticket> tickets) {
                refreshCache(tickets);
                refreshLocalDataSource(tickets);
                callback.onTicketsLoaded(new ArrayList<>(mCachedTickets.values()));
            }

            @Override
            public void onDataNotAvailable() {
                callback.onDataNotAvailable();
            }
        });
    }

    private void refreshCache(List<Ticket> tickets) {
        if (mCachedTickets == null) {
            mCachedTickets = new LinkedHashMap<>();
        }
        mCachedTickets.clear();
        for (Ticket ticket : tickets) {
            mCachedTickets.put(ticket.getId(), ticket);
        }
        mCacheIsDirty = false;
    }

    private void refreshLocalDataSource(List<Ticket> tickets) {
        mTicketLocalDataSource.deleteAllTickets();
        for (Ticket ticket : tickets) {
            mTicketLocalDataSource.saveTicket(ticket);
        }
    }

    @Nullable
    private Ticket getTaskWithId(@NonNull String id) {
        checkNotNull(id);
        if (mCachedTickets == null || mCachedTickets.isEmpty()) {
            return null;
        } else {
            return mCachedTickets.get(id);
        }
    }
}
