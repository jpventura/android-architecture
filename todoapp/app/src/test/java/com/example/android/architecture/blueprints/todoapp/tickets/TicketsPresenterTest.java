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

package com.example.android.architecture.blueprints.todoapp.tickets;

import com.example.android.architecture.blueprints.todoapp.data.Ticket;
import com.example.android.architecture.blueprints.todoapp.data.source.TicketsDataSource.LoadTicketsCallback;
import com.example.android.architecture.blueprints.todoapp.data.source.TicketsRepository;
import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the implementation of {@link TicketsPresenter}
 */
public class TicketsPresenterTest {

    private static List<Ticket> Tickets;

    @Mock
    private TicketsRepository mTicketsRepository;

    @Mock
    private TicketsContract.View mTicketsView;

    /**
     * {@link ArgumentCaptor} is a powerful Mockito API to capture argument values and use them to
     * perform further actions or assertions on them.
     */
    @Captor
    private ArgumentCaptor<LoadTicketsCallback> mLoadTicketsCallbackCaptor;

    private TicketsPresenter mTicketsPresenter;

    @Before
    public void setupTicketsPresenter() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        // Get a reference to the class under test
        mTicketsPresenter = new TicketsPresenter(mTicketsRepository, mTicketsView);

        // The presenter won't update the view unless it's active.
        when(mTicketsView.isActive()).thenReturn(true);

        // We start the tasks to 3, with one active and two completed
        Tickets = Lists.newArrayList(new Ticket("Title1", "Description1"),
                new Ticket("Title2", "Description2", true), new Ticket("Title3", "Description3", true));
    }

    @Test
    public void createPresenter_setsThePresenterToView() {
        // Get a reference to the class under test
        mTicketsPresenter = new TicketsPresenter(mTicketsRepository, mTicketsView);

        // Then the presenter is set to the view
        verify(mTicketsView).setPresenter(mTicketsPresenter);
    }

    @Test
    public void loadAllTicketsFromRepositoryAndLoadIntoView() {
        // Given an initialized TicketsPresenter with initialized tasks
        // When loading of Tickets is requested
        mTicketsPresenter.setFiltering(TicketsFilterType.ALL_TASKS);
        mTicketsPresenter.loadTickets(true);

        // Callback is captured and invoked with stubbed tasks
        verify(mTicketsRepository).getTickets(mLoadTicketsCallbackCaptor.capture());
        mLoadTicketsCallbackCaptor.getValue().onTicketsLoaded(Tickets);

        // Then progress indicator is shown
        InOrder inOrder = inOrder(mTicketsView);
        inOrder.verify(mTicketsView).setLoadingIndicator(true);
        // Then progress indicator is hidden and all tasks are shown in UI
        inOrder.verify(mTicketsView).setLoadingIndicator(false);
        ArgumentCaptor<List> showTicketsArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(mTicketsView).showTickets(showTicketsArgumentCaptor.capture());
        assertTrue(showTicketsArgumentCaptor.getValue().size() == 3);
    }

    @Test
    public void loadActiveTicketsFromRepositoryAndLoadIntoView() {
        // Given an initialized TicketsPresenter with initialized tasks
        // When loading of Tickets is requested
        mTicketsPresenter.setFiltering(TicketsFilterType.ACTIVE_TASKS);
        mTicketsPresenter.loadTickets(true);

        // Callback is captured and invoked with stubbed tasks
        verify(mTicketsRepository).getTickets(mLoadTicketsCallbackCaptor.capture());
        mLoadTicketsCallbackCaptor.getValue().onTicketsLoaded(Tickets);

        // Then progress indicator is hidden and active tasks are shown in UI
        verify(mTicketsView).setLoadingIndicator(false);
        ArgumentCaptor<List> showTicketsArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(mTicketsView).showTickets(showTicketsArgumentCaptor.capture());
        assertTrue(showTicketsArgumentCaptor.getValue().size() == 1);
    }

    @Test
    public void loadCompletedTicketsFromRepositoryAndLoadIntoView() {
        // Given an initialized TicketsPresenter with initialized tasks
        // When loading of Tickets is requested
        mTicketsPresenter.setFiltering(TicketsFilterType.COMPLETED_TASKS);
        mTicketsPresenter.loadTickets(true);

        // Callback is captured and invoked with stubbed tasks
        verify(mTicketsRepository).getTickets(mLoadTicketsCallbackCaptor.capture());
        mLoadTicketsCallbackCaptor.getValue().onTicketsLoaded(Tickets);

        // Then progress indicator is hidden and completed tasks are shown in UI
        verify(mTicketsView).setLoadingIndicator(false);
        ArgumentCaptor<List> showTicketsArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(mTicketsView).showTickets(showTicketsArgumentCaptor.capture());
        assertTrue(showTicketsArgumentCaptor.getValue().size() == 2);
    }

    @Test
    public void clickOnFab_ShowsAddTicketUi() {
        // When adding a new task
        mTicketsPresenter.addNewTicket();

        // Then add task UI is shown
        verify(mTicketsView).showAddTicket();
    }

    @Test
    public void clickOnTicket_ShowsDetailUi() {
        // Given a stubbed active task
        Ticket requestedTicket = new Ticket("Details Requested", "For this task");

        // When open task details is requested
        mTicketsPresenter.openTicketDetails(requestedTicket);

        // Then task detail UI is shown
        verify(mTicketsView).showTicketDetailsUi(any(String.class));
    }

    @Test
    public void completeTicket_ShowsTicketMarkedComplete() {
        // Given a stubbed ticket
        Ticket ticket = new Ticket("Details Requested", "For this ticket");

        // When ticket is marked as complete
        mTicketsPresenter.completeTicket(ticket);

        // Then repository is called and ticket marked complete UI is shown
        verify(mTicketsRepository).completeTicket(ticket);
        verify(mTicketsView).showTicketMarkedComplete();
    }

    @Test
    public void activateTicket_ShowsTicketMarkedActive() {
        // Given a stubbed completed ticket
        Ticket ticket = new Ticket("Details Requested", "For this ticket", true);
        mTicketsPresenter.loadTickets(true);

        // When ticket is marked as activated
        mTicketsPresenter.activateTicket(ticket);

        // Then repository is called and ticket marked active UI is shown
        verify(mTicketsRepository).activateTicket(ticket);
        verify(mTicketsView).showTicketMarkedActive();
    }

    @Test
    public void unavailableTickets_ShowsError() {
        // When tasks are loaded
        mTicketsPresenter.setFiltering(TicketsFilterType.ALL_TASKS);
        mTicketsPresenter.loadTickets(true);

        // And the tasks aren't available in the repository
        verify(mTicketsRepository).getTickets(mLoadTicketsCallbackCaptor.capture());
        mLoadTicketsCallbackCaptor.getValue().onDataNotAvailable();

        // Then an error message is shown
        verify(mTicketsView).showLoadingTicketsError();
    }
}
