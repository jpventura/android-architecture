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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import android.content.Context;

import com.example.android.architecture.blueprints.todoapp.data.Ticket;
import com.google.common.collect.Lists;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

/**
 * Unit tests for the implementation of the in-memory repository with cache.
 */
public class TicketsRepositoryTest {

    private final static String TASK_TITLE = "title";

    private final static String TASK_TITLE2 = "title2";

    private final static String TASK_TITLE3 = "title3";

    private static List<Ticket> Tickets = Lists.newArrayList(new Ticket("Title1", "Description1"),
            new Ticket("Title2", "Description2"));

    private TicketsRepository mTicketsRepository;

    @Mock
    private TicketsDataSource mTicketsRemoteDataSource;

    @Mock
    private TicketsDataSource mTicketsLocalDataSource;

    @Mock
    private Context mContext;

    @Mock
    private TicketsDataSource.GetTicketCallback mGetTicketCallback;

    @Mock
    private TicketsDataSource.LoadTicketsCallback mLoadTicketsCallback;

    /**
     * {@link ArgumentCaptor} is a powerful Mockito API to capture argument values and use them to
     * perform further actions or assertions on them.
     */
    @Captor
    private ArgumentCaptor<TicketsDataSource.LoadTicketsCallback> mTicketsCallbackCaptor;

    /**
     * {@link ArgumentCaptor} is a powerful Mockito API to capture argument values and use them to
     * perform further actions or assertions on them.
     */
    @Captor
    private ArgumentCaptor<TicketsDataSource.GetTicketCallback> mTicketCallbackCaptor;

    @Before
    public void setupTicketsRepository() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        // Get a reference to the class under test
        mTicketsRepository = TicketsRepository.getInstance(
                mTicketsRemoteDataSource, mTicketsLocalDataSource);
    }

    @After
    public void destroyRepositoryInstance() {
        TicketsRepository.destroyInstance();
    }

    @Test
    public void getTickets_repositoryCachesAfterFirstApiCall() {
        // Given a setup Captor to capture callbacks
        // When two calls are issued to the tasks repository
        twoTicketsLoadCallsToRepository(mLoadTicketsCallback);

        // Then tasks were only requested once from Service API
        verify(mTicketsRemoteDataSource).getTickets(any(TicketsDataSource.LoadTicketsCallback.class));
    }

    @Test
    public void getTickets_requestsAllTicketsFromLocalDataSource() {
        // When tasks are requested from the tasks repository
        mTicketsRepository.getTickets(mLoadTicketsCallback);

        // Then tasks are loaded from the local data source
        verify(mTicketsLocalDataSource).getTickets(any(TicketsDataSource.LoadTicketsCallback.class));
    }

    @Test
    public void saveTicket_savesTicketToServiceAPI() {
        // Given a stub task with title and description
        Ticket newTicket = new Ticket(TASK_TITLE, "Some Ticket Description");

        // When a task is saved to the tasks repository
        mTicketsRepository.saveTicket(newTicket);

        // Then the service API and persistent repository are called and the cache is updated
        verify(mTicketsRemoteDataSource).saveTicket(newTicket);
        verify(mTicketsLocalDataSource).saveTicket(newTicket);
        assertThat(mTicketsRepository.mCachedTickets.size(), is(1));
    }

    @Test
    public void completeTicket_completesTicketToServiceAPIUpdatesCache() {
        // Given a stub active task with title and description added in the repository
        Ticket newTicket = new Ticket(TASK_TITLE, "Some Ticket Description");
        mTicketsRepository.saveTicket(newTicket);

        // When a task is completed to the tasks repository
        mTicketsRepository.completeTicket(newTicket);

        // Then the service API and persistent repository are called and the cache is updated
        verify(mTicketsRemoteDataSource).completeTicket(newTicket);
        verify(mTicketsLocalDataSource).completeTicket(newTicket);
        assertThat(mTicketsRepository.mCachedTickets.size(), is(1));
        assertThat(mTicketsRepository.mCachedTickets.get(newTicket.getId()).isActive(), is(false));
    }

    @Test
    public void completeTicketId_completesTicketToServiceAPIUpdatesCache() {
        // Given a stub active task with title and description added in the repository
        Ticket newTicket = new Ticket(TASK_TITLE, "Some Ticket Description");
        mTicketsRepository.saveTicket(newTicket);

        // When a task is completed using its id to the tasks repository
        mTicketsRepository.completeTicket(newTicket.getId());

        // Then the service API and persistent repository are called and the cache is updated
        verify(mTicketsRemoteDataSource).completeTicket(newTicket);
        verify(mTicketsLocalDataSource).completeTicket(newTicket);
        assertThat(mTicketsRepository.mCachedTickets.size(), is(1));
        assertThat(mTicketsRepository.mCachedTickets.get(newTicket.getId()).isActive(), is(false));
    }

    @Test
    public void activateTicket_activatesTicketToServiceAPIUpdatesCache() {
        // Given a stub completed task with title and description in the repository
        Ticket newTicket = new Ticket(TASK_TITLE, "Some Ticket Description", true);
        mTicketsRepository.saveTicket(newTicket);

        // When a completed task is activated to the tasks repository
        mTicketsRepository.activateTicket(newTicket);

        // Then the service API and persistent repository are called and the cache is updated
        verify(mTicketsRemoteDataSource).activateTicket(newTicket);
        verify(mTicketsLocalDataSource).activateTicket(newTicket);
        assertThat(mTicketsRepository.mCachedTickets.size(), is(1));
        assertThat(mTicketsRepository.mCachedTickets.get(newTicket.getId()).isActive(), is(true));
    }

    @Test
    public void activateTicketId_activatesTicketToServiceAPIUpdatesCache() {
        // Given a stub completed task with title and description in the repository
        Ticket newTicket = new Ticket(TASK_TITLE, "Some Ticket Description", true);
        mTicketsRepository.saveTicket(newTicket);

        // When a completed task is activated with its id to the tasks repository
        mTicketsRepository.activateTicket(newTicket.getId());

        // Then the service API and persistent repository are called and the cache is updated
        verify(mTicketsRemoteDataSource).activateTicket(newTicket);
        verify(mTicketsLocalDataSource).activateTicket(newTicket);
        assertThat(mTicketsRepository.mCachedTickets.size(), is(1));
        assertThat(mTicketsRepository.mCachedTickets.get(newTicket.getId()).isActive(), is(true));
    }

    @Test
    public void getTicket_requestsSingleTicketFromLocalDataSource() {
        // When a task is requested from the tasks repository
        mTicketsRepository.getTicket(TASK_TITLE, mGetTicketCallback);

        // Then the task is loaded from the database
        verify(mTicketsLocalDataSource).getTicket(eq(TASK_TITLE), any(
                TicketsDataSource.GetTicketCallback.class));
    }

    @Test
    public void deleteCompletedTickets_deleteCompletedTicketsToServiceAPIUpdatesCache() {
        // Given 2 stub completed tasks and 1 stub active tasks in the repository
        Ticket newTicket = new Ticket(TASK_TITLE, "Some Ticket Description", true);
        mTicketsRepository.saveTicket(newTicket);
        Ticket newTicket2 = new Ticket(TASK_TITLE2, "Some Ticket Description");
        mTicketsRepository.saveTicket(newTicket2);
        Ticket newTicket3 = new Ticket(TASK_TITLE3, "Some Ticket Description", true);
        mTicketsRepository.saveTicket(newTicket3);

        // When a completed tasks are cleared to the tasks repository
        mTicketsRepository.clearCompletedTickets();


        // Then the service API and persistent repository are called and the cache is updated
        verify(mTicketsRemoteDataSource).clearCompletedTickets();
        verify(mTicketsLocalDataSource).clearCompletedTickets();

        assertThat(mTicketsRepository.mCachedTickets.size(), is(1));
        assertTrue(mTicketsRepository.mCachedTickets.get(newTicket2.getId()).isActive());
        assertThat(mTicketsRepository.mCachedTickets.get(newTicket2.getId()).getTitle(), is(TASK_TITLE2));
    }

    @Test
    public void deleteAllTickets_deleteTicketsToServiceAPIUpdatesCache() {
        // Given 2 stub completed tasks and 1 stub active tasks in the repository
        Ticket newTicket = new Ticket(TASK_TITLE, "Some Ticket Description", true);
        mTicketsRepository.saveTicket(newTicket);
        Ticket newTicket2 = new Ticket(TASK_TITLE2, "Some Ticket Description");
        mTicketsRepository.saveTicket(newTicket2);
        Ticket newTicket3 = new Ticket(TASK_TITLE3, "Some Ticket Description", true);
        mTicketsRepository.saveTicket(newTicket3);

        // When all tasks are deleted to the tasks repository
        mTicketsRepository.deleteAllTickets();

        // Verify the data sources were called
        verify(mTicketsRemoteDataSource).deleteAllTickets();
        verify(mTicketsLocalDataSource).deleteAllTickets();

        assertThat(mTicketsRepository.mCachedTickets.size(), is(0));
    }

    @Test
    public void deleteTicket_deleteTicketToServiceAPIRemovedFromCache() {
        // Given a task in the repository
        Ticket newTicket = new Ticket(TASK_TITLE, "Some Ticket Description", true);
        mTicketsRepository.saveTicket(newTicket);
        assertThat(mTicketsRepository.mCachedTickets.containsKey(newTicket.getId()), is(true));

        // When deleted
        mTicketsRepository.deleteTicket(newTicket.getId());

        // Verify the data sources were called
        verify(mTicketsRemoteDataSource).deleteTicket(newTicket.getId());
        verify(mTicketsLocalDataSource).deleteTicket(newTicket.getId());

        // Verify it's removed from repository
        assertThat(mTicketsRepository.mCachedTickets.containsKey(newTicket.getId()), is(false));
    }

    @Test
    public void getTicketsWithDirtyCache_tasksAreRetrievedFromRemote() {
        // When calling getTickets in the repository with dirty cache
        mTicketsRepository.refreshTickets();
        mTicketsRepository.getTickets(mLoadTicketsCallback);

        // And the remote data source has data available
        setTicketsAvailable(mTicketsRemoteDataSource, Tickets);

        // Verify the tasks from the remote data source are returned, not the local
        verify(mTicketsLocalDataSource, never()).getTickets(mLoadTicketsCallback);
        verify(mLoadTicketsCallback).onTicketsLoaded(Tickets);
    }

    @Test
    public void getTicketsWithLocalDataSourceUnavailable_tasksAreRetrievedFromRemote() {
        // When calling getTickets in the repository
        mTicketsRepository.getTickets(mLoadTicketsCallback);

        // And the local data source has no data available
        setTicketsNotAvailable(mTicketsLocalDataSource);

        // And the remote data source has data available
        setTicketsAvailable(mTicketsRemoteDataSource, Tickets);

        // Verify the tasks from the local data source are returned
        verify(mLoadTicketsCallback).onTicketsLoaded(Tickets);
    }

    @Test
    public void getTicketsWithBothDataSourcesUnavailable_firesOnDataUnavailable() {
        // When calling getTickets in the repository
        mTicketsRepository.getTickets(mLoadTicketsCallback);

        // And the local data source has no data available
        setTicketsNotAvailable(mTicketsLocalDataSource);

        // And the remote data source has no data available
        setTicketsNotAvailable(mTicketsRemoteDataSource);

        // Verify no data is returned
        verify(mLoadTicketsCallback).onDataNotAvailable();
    }

    @Test
    public void getTicketWithBothDataSourcesUnavailable_firesOnDataUnavailable() {
        // Given a task id
        final String taskId = "123";

        // When calling getTicket in the repository
        mTicketsRepository.getTicket(taskId, mGetTicketCallback);

        // And the local data source has no data available
        setTicketNotAvailable(mTicketsLocalDataSource, taskId);

        // And the remote data source has no data available
        setTicketNotAvailable(mTicketsRemoteDataSource, taskId);

        // Verify no data is returned
        verify(mGetTicketCallback).onDataNotAvailable();
    }

    @Test
    public void getTickets_refreshesLocalDataSource() {
        // Mark cache as dirty to force a reload of data from remote data source.
        mTicketsRepository.refreshTickets();

        // When calling getTickets in the repository
        mTicketsRepository.getTickets(mLoadTicketsCallback);

        // Make the remote data source return data
        setTicketsAvailable(mTicketsRemoteDataSource, Tickets);

        // Verify that the data fetched from the remote data source was saved in local.
        verify(mTicketsLocalDataSource, times(Tickets.size())).saveTicket(any(Ticket.class));
    }

    /**
     * Convenience method that issues two calls to the tasks repository
     */
    private void twoTicketsLoadCallsToRepository(TicketsDataSource.LoadTicketsCallback callback) {
        // When tasks are requested from repository
        mTicketsRepository.getTickets(callback); // First call to API

        // Use the Mockito Captor to capture the callback
        verify(mTicketsLocalDataSource).getTickets(mTicketsCallbackCaptor.capture());

        // Local data source doesn't have data yet
        mTicketsCallbackCaptor.getValue().onDataNotAvailable();


        // Verify the remote data source is queried
        verify(mTicketsRemoteDataSource).getTickets(mTicketsCallbackCaptor.capture());

        // Trigger callback so tasks are cached
        mTicketsCallbackCaptor.getValue().onTicketsLoaded(Tickets);

        mTicketsRepository.getTickets(callback); // Second call to API
    }

    private void setTicketsNotAvailable(TicketsDataSource dataSource) {
        verify(dataSource).getTickets(mTicketsCallbackCaptor.capture());
        mTicketsCallbackCaptor.getValue().onDataNotAvailable();
    }

    private void setTicketsAvailable(TicketsDataSource dataSource, List<Ticket> tickets) {
        verify(dataSource).getTickets(mTicketsCallbackCaptor.capture());
        mTicketsCallbackCaptor.getValue().onTicketsLoaded(tickets);
    }

    private void setTicketNotAvailable(TicketsDataSource dataSource, String taskId) {
        verify(dataSource).getTicket(eq(taskId), mTicketCallbackCaptor.capture());
        mTicketCallbackCaptor.getValue().onDataNotAvailable();
    }

    private void setTicketAvailable(TicketsDataSource dataSource, Ticket ticket) {
        verify(dataSource).getTicket(eq(ticket.getId()), mTicketCallbackCaptor.capture());
        mTicketCallbackCaptor.getValue().onTicketLoaded(ticket);
    }
 }
