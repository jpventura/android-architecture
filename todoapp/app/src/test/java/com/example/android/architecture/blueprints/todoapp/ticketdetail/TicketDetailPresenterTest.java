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

package com.example.android.architecture.blueprints.todoapp.ticketdetail;

import com.example.android.architecture.blueprints.todoapp.data.Ticket;
import com.example.android.architecture.blueprints.todoapp.data.source.TicketsDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.TicketsRepository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the implementation of {@link TicketDetailPresenter}
 */
public class TicketDetailPresenterTest {

    public static final String TITLE_TEST = "title";

    public static final String DESCRIPTION_TEST = "description";

    public static final String INVALID_TASK_ID = "";

    public static final Ticket ACTIVE_TICKET = new Ticket(TITLE_TEST, DESCRIPTION_TEST);

    public static final Ticket COMPLETED_TICKET = new Ticket(TITLE_TEST, DESCRIPTION_TEST, true);

    @Mock
    private TicketsRepository mTicketsRepository;

    @Mock
    private TicketDetailContract.View mTicketDetailView;

    /**
     * {@link ArgumentCaptor} is a powerful Mockito API to capture argument values and use them to
     * perform further actions or assertions on them.
     */
    @Captor
    private ArgumentCaptor<TicketsDataSource.GetTicketCallback> mGetTicketCallbackCaptor;

    private TicketDetailPresenter mTicketDetailPresenter;

    @Before
    public void setup() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        // The presenter won't update the view unless it's active.
        when(mTicketDetailView.isActive()).thenReturn(true);
    }

    @Test
    public void createPresenter_setsThePresenterToView() {
        // Get a reference to the class under test
        mTicketDetailPresenter = new TicketDetailPresenter(
                ACTIVE_TICKET.getId(), mTicketsRepository, mTicketDetailView);

        // Then the presenter is set to the view
        verify(mTicketDetailView).setPresenter(mTicketDetailPresenter);
    }

    @Test
    public void getActiveTicketFromRepositoryAndLoadIntoView() {
        // When tasks presenter is asked to open a task
        mTicketDetailPresenter = new TicketDetailPresenter(
                ACTIVE_TICKET.getId(), mTicketsRepository, mTicketDetailView);
        mTicketDetailPresenter.start();

        // Then task is loaded from model, callback is captured and progress indicator is shown
        verify(mTicketsRepository).getTicket(eq(ACTIVE_TICKET.getId()), mGetTicketCallbackCaptor.capture());
        InOrder inOrder = inOrder(mTicketDetailView);
        inOrder.verify(mTicketDetailView).setLoadingIndicator(true);

        // When task is finally loaded
        mGetTicketCallbackCaptor.getValue().onTicketLoaded(ACTIVE_TICKET); // Trigger callback

        // Then progress indicator is hidden and title, description and completion status are shown
        // in UI
        inOrder.verify(mTicketDetailView).setLoadingIndicator(false);
        verify(mTicketDetailView).showTitle(TITLE_TEST);
        verify(mTicketDetailView).showDescription(DESCRIPTION_TEST);
        verify(mTicketDetailView).showCompletionStatus(false);
    }

    @Test
    public void getCompletedTicketFromRepositoryAndLoadIntoView() {
        mTicketDetailPresenter = new TicketDetailPresenter(
                COMPLETED_TICKET.getId(), mTicketsRepository, mTicketDetailView);
        mTicketDetailPresenter.start();

        // Then task is loaded from model, callback is captured and progress indicator is shown
        verify(mTicketsRepository).getTicket(
                eq(COMPLETED_TICKET.getId()), mGetTicketCallbackCaptor.capture());
        InOrder inOrder = inOrder(mTicketDetailView);
        inOrder.verify(mTicketDetailView).setLoadingIndicator(true);

        // When task is finally loaded
        mGetTicketCallbackCaptor.getValue().onTicketLoaded(COMPLETED_TICKET); // Trigger callback

        // Then progress indicator is hidden and title, description and completion status are shown
        // in UI
        inOrder.verify(mTicketDetailView).setLoadingIndicator(false);
        verify(mTicketDetailView).showTitle(TITLE_TEST);
        verify(mTicketDetailView).showDescription(DESCRIPTION_TEST);
        verify(mTicketDetailView).showCompletionStatus(true);
    }

    @Test
    public void getUnknownTicketFromRepositoryAndLoadIntoView() {
        // When loading of a task is requested with an invalid task ID.
        mTicketDetailPresenter = new TicketDetailPresenter(
                INVALID_TASK_ID, mTicketsRepository, mTicketDetailView);
        mTicketDetailPresenter.start();
        verify(mTicketDetailView).showMissingTicket();
    }

    @Test
    public void deleteTicket() {
        // Given an initialized TicketDetailPresenter with stubbed ticket
        Ticket ticket = new Ticket(TITLE_TEST, DESCRIPTION_TEST);

        // When the deletion of a ticket is requested
        mTicketDetailPresenter = new TicketDetailPresenter(
                ticket.getId(), mTicketsRepository, mTicketDetailView);
        mTicketDetailPresenter.deleteTicket();

        // Then the repository and the view are notified
        verify(mTicketsRepository).deleteTicket(ticket.getId());
        verify(mTicketDetailView).showTicketDeleted();
    }

    @Test
    public void completeTicket() {
        // Given an initialized presenter with an active ticket
        Ticket ticket = new Ticket(TITLE_TEST, DESCRIPTION_TEST);
        mTicketDetailPresenter = new TicketDetailPresenter(
                ticket.getId(), mTicketsRepository, mTicketDetailView);
        mTicketDetailPresenter.start();

        // When the presenter is asked to complete the ticket
        mTicketDetailPresenter.completeTicket();

        // Then a request is sent to the ticket repository and the UI is updated
        verify(mTicketsRepository).completeTicket(ticket.getId());
        verify(mTicketDetailView).showTicketMarkedComplete();
    }

    @Test
    public void activateTicket() {
        // Given an initialized presenter with a completed ticket
        Ticket ticket = new Ticket(TITLE_TEST, DESCRIPTION_TEST, true);
        mTicketDetailPresenter = new TicketDetailPresenter(
                ticket.getId(), mTicketsRepository, mTicketDetailView);
        mTicketDetailPresenter.start();

        // When the presenter is asked to activate the ticket
        mTicketDetailPresenter.activateTicket();

        // Then a request is sent to the ticket repository and the UI is updated
        verify(mTicketsRepository).activateTicket(ticket.getId());
        verify(mTicketDetailView).showTicketMarkedActive();
    }

    @Test
    public void activeTicketIsShownWhenEditing() {
        // When the edit of an ACTIVE_TICKET is requested
        mTicketDetailPresenter = new TicketDetailPresenter(
                ACTIVE_TICKET.getId(), mTicketsRepository, mTicketDetailView);
        mTicketDetailPresenter.editTicket();

        // Then the view is notified
        verify(mTicketDetailView).showEditTicket(ACTIVE_TICKET.getId());
    }

    @Test
    public void invalidTicketIsNotShownWhenEditing() {
        // When the edit of an invalid task id is requested
        mTicketDetailPresenter = new TicketDetailPresenter(
                INVALID_TASK_ID, mTicketsRepository, mTicketDetailView);
        mTicketDetailPresenter.editTicket();

        // Then the edit mode is never started
        verify(mTicketDetailView, never()).showEditTicket(INVALID_TASK_ID);
        // instead, the error is shown.
        verify(mTicketDetailView).showMissingTicket();
    }

}
