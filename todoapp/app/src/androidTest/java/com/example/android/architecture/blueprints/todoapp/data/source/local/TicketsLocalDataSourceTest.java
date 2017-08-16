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

package com.example.android.architecture.blueprints.todoapp.data.source.local;

import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.example.android.architecture.blueprints.todoapp.data.Ticket;
import com.example.android.architecture.blueprints.todoapp.data.source.TicketsDataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Integration test for the {@link TicketsDataSource}, which uses the {@link TicketsDbHelper}.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class TicketsLocalDataSourceTest {

    private final static String TITLE = "title";

    private final static String TITLE2 = "title2";

    private final static String TITLE3 = "title3";

    private TicketsLocalDataSource mLocalDataSource;

    @Before
    public void setup() {
        mLocalDataSource = TicketsLocalDataSource.getInstance(
                InstrumentationRegistry.getTargetContext());
    }

    @After
    public void cleanUp() {
        mLocalDataSource.deleteAllTickets();
    }

    @Test
    public void testPreConditions() {
        assertNotNull(mLocalDataSource);
    }

    @Test
    public void saveTask_retrievesTask() {
        // Given a new task
        final Ticket newTicket = new Ticket(TITLE, "");

        // When saved into the persistent repository
        mLocalDataSource.saveTicket(newTicket);

        // Then the task can be retrieved from the persistent repository
        mLocalDataSource.getTicket(newTicket.getId(), new TicketsDataSource.GetTicketCallback() {
            @Override
            public void onTicketLoaded(Ticket ticket) {
                assertThat(ticket, is(newTicket));
            }

            @Override
            public void onDataNotAvailable() {
                fail("Callback error");
            }
        });
    }

    @Test
    public void completeTask_retrievedTaskIsComplete() {
        // Initialize mock for the callback.
        TicketsDataSource.GetTicketCallback callback = mock(TicketsDataSource.GetTicketCallback.class);
        // Given a new task in the persistent repository
        final Ticket newTicket = new Ticket(TITLE, "");
        mLocalDataSource.saveTicket(newTicket);

        // When completed in the persistent repository
        mLocalDataSource.completeTicket(newTicket);

        // Then the task can be retrieved from the persistent repository and is complete
        mLocalDataSource.getTicket(newTicket.getId(), new TicketsDataSource.GetTicketCallback() {
            @Override
            public void onTicketLoaded(Ticket ticket) {
                assertThat(ticket, is(newTicket));
                assertThat(ticket.isCompleted(), is(true));
            }

            @Override
            public void onDataNotAvailable() {
                fail("Callback error");
            }
        });
    }

    @Test
    public void activateTask_retrievedTaskIsActive() {
        // Initialize mock for the callback.
        TicketsDataSource.GetTicketCallback callback = mock(TicketsDataSource.GetTicketCallback.class);

        // Given a new completed task in the persistent repository
        final Ticket newTicket = new Ticket(TITLE, "");
        mLocalDataSource.saveTicket(newTicket);
        mLocalDataSource.completeTicket(newTicket);

        // When activated in the persistent repository
        mLocalDataSource.activateTicket(newTicket);

        // Then the task can be retrieved from the persistent repository and is active
        mLocalDataSource.getTicket(newTicket.getId(), callback);

        verify(callback, never()).onDataNotAvailable();
        verify(callback).onTicketLoaded(newTicket);

        assertThat(newTicket.isCompleted(), is(false));
    }

    @Test
    public void clearCompletedTask_taskNotRetrievable() {
        // Initialize mocks for the callbacks.
        TicketsDataSource.GetTicketCallback callback1 = mock(TicketsDataSource.GetTicketCallback.class);
        TicketsDataSource.GetTicketCallback callback2 = mock(TicketsDataSource.GetTicketCallback.class);
        TicketsDataSource.GetTicketCallback callback3 = mock(TicketsDataSource.GetTicketCallback.class);

        // Given 2 new completed tasks and 1 active task in the persistent repository
        final Ticket newTicket1 = new Ticket(TITLE, "");
        mLocalDataSource.saveTicket(newTicket1);
        mLocalDataSource.completeTicket(newTicket1);
        final Ticket newTicket2 = new Ticket(TITLE2, "");
        mLocalDataSource.saveTicket(newTicket2);
        mLocalDataSource.completeTicket(newTicket2);
        final Ticket newTicket3 = new Ticket(TITLE3, "");
        mLocalDataSource.saveTicket(newTicket3);

        // When completed tasks are cleared in the repository
        mLocalDataSource.clearCompletedTickets();

        // Then the completed tasks cannot be retrieved and the active one can
        mLocalDataSource.getTicket(newTicket1.getId(), callback1);

        verify(callback1).onDataNotAvailable();
        verify(callback1, never()).onTicketLoaded(newTicket1);

        mLocalDataSource.getTicket(newTicket2.getId(), callback2);

        verify(callback2).onDataNotAvailable();
        verify(callback2, never()).onTicketLoaded(newTicket1);

        mLocalDataSource.getTicket(newTicket3.getId(), callback3);

        verify(callback3, never()).onDataNotAvailable();
        verify(callback3).onTicketLoaded(newTicket3);
    }

    @Test
    public void deleteAllTasks_emptyListOfRetrievedTask() {
        // Given a new task in the persistent repository and a mocked callback
        Ticket newTicket = new Ticket(TITLE, "");
        mLocalDataSource.saveTicket(newTicket);
        TicketsDataSource.LoadTicketsCallback callback = mock(TicketsDataSource.LoadTicketsCallback.class);

        // When all tasks are deleted
        mLocalDataSource.deleteAllTickets();

        // Then the retrieved tasks is an empty list
        mLocalDataSource.getTickets(callback);

        verify(callback).onDataNotAvailable();
        verify(callback, never()).onTicketsLoaded(anyList());
    }

    @Test
    public void getTasks_retrieveSavedTasks() {
        // Given 2 new tasks in the persistent repository
        final Ticket newTicket1 = new Ticket(TITLE, "");
        mLocalDataSource.saveTicket(newTicket1);
        final Ticket newTicket2 = new Ticket(TITLE, "");
        mLocalDataSource.saveTicket(newTicket2);

        // Then the tasks can be retrieved from the persistent repository
        mLocalDataSource.getTickets(new TicketsDataSource.LoadTicketsCallback() {
            @Override
            public void onTicketsLoaded(List<Ticket> tickets) {
                assertNotNull(tickets);
                assertTrue(tickets.size() >= 2);

                boolean newTask1IdFound = false;
                boolean newTask2IdFound = false;
                for (Ticket ticket : tickets) {
                    if (ticket.getId().equals(newTicket1.getId())) {
                        newTask1IdFound = true;
                    }
                    if (ticket.getId().equals(newTicket2.getId())) {
                        newTask2IdFound = true;
                    }
                }
                assertTrue(newTask1IdFound);
                assertTrue(newTask2IdFound);
            }

            @Override
            public void onDataNotAvailable() {
                fail();
            }
        });
    }
}
