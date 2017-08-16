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

package com.example.android.architecture.blueprints.todoapp.addeditticket;

import com.example.android.architecture.blueprints.todoapp.data.Ticket;
import com.example.android.architecture.blueprints.todoapp.data.source.TicketsDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.TicketsRepository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the implementation of {@link AddEditTicketPresenter}.
 */
public class AddEditTicketPresenterTest {

    @Mock
    private TicketsRepository mTicketsRepository;

    @Mock
    private AddEditTicketContract.View mAddEditTicketView;

    /**
     * {@link ArgumentCaptor} is a powerful Mockito API to capture argument values and use them to
     * perform further actions or assertions on them.
     */
    @Captor
    private ArgumentCaptor<TicketsDataSource.GetTicketCallback> mGetTicketCallbackCaptor;

    private AddEditTicketPresenter mAddEditTicketPresenter;

    @Before
    public void setupMocksAndView() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        // The presenter wont't update the view unless it's active.
        when(mAddEditTicketView.isActive()).thenReturn(true);
    }

    @Test
    public void createPresenter_setsThePresenterToView(){
        // Get a reference to the class under test
        mAddEditTicketPresenter = new AddEditTicketPresenter(
                null, mTicketsRepository, mAddEditTicketView, true);

        // Then the presenter is set to the view
        verify(mAddEditTicketView).setPresenter(mAddEditTicketPresenter);
    }

    @Test
    public void saveNewTicketToRepository_showsSuccessMessageUi() {
        // Get a reference to the class under test
        mAddEditTicketPresenter = new AddEditTicketPresenter(
                null, mTicketsRepository, mAddEditTicketView, true);

        // When the presenter is asked to save a task
        mAddEditTicketPresenter.saveTicket("New Ticket Title", "Some Ticket Description");

        // Then a task is saved in the repository and the view updated
        verify(mTicketsRepository).saveTicket(any(Ticket.class)); // saved to the model
        verify(mAddEditTicketView).showTicketsList(); // shown in the UI
    }

    @Test
    public void saveTicket_emptyTicketShowsErrorUi() {
        // Get a reference to the class under test
        mAddEditTicketPresenter = new AddEditTicketPresenter(
                null, mTicketsRepository, mAddEditTicketView, true);

        // When the presenter is asked to save an empty task
        mAddEditTicketPresenter.saveTicket("", "");

        // Then an empty not error is shown in the UI
        verify(mAddEditTicketView).showEmptyTicketError();
    }

    @Test
    public void saveExistingTicketToRepository_showsSuccessMessageUi() {
        // Get a reference to the class under test
        mAddEditTicketPresenter = new AddEditTicketPresenter(
                "1", mTicketsRepository, mAddEditTicketView, true);

        // When the presenter is asked to save an existing task
        mAddEditTicketPresenter.saveTicket("Existing Ticket Title", "Some Ticket Description");

        // Then a task is saved in the repository and the view updated
        verify(mTicketsRepository).saveTicket(any(Ticket.class)); // saved to the model
        verify(mAddEditTicketView).showTicketsList(); // shown in the UI
    }

    @Test
    public void populateTicket_callsRepoAndUpdatesView() {
        Ticket testTicket = new Ticket("TITLE", "DESCRIPTION");
        // Get a reference to the class under test
        mAddEditTicketPresenter = new AddEditTicketPresenter(testTicket.getId(),
                mTicketsRepository, mAddEditTicketView, true);

        // When the presenter is asked to populate an existing task
        mAddEditTicketPresenter.populateTicket();

        // Then the task repository is queried and the view updated
        verify(mTicketsRepository).getTicket(eq(testTicket.getId()), mGetTicketCallbackCaptor.capture());
        assertThat(mAddEditTicketPresenter.isDataMissing(), is(true));

        // Simulate callback
        mGetTicketCallbackCaptor.getValue().onTicketLoaded(testTicket);

        verify(mAddEditTicketView).setTitle(testTicket.getTitle());
        verify(mAddEditTicketView).setDescription(testTicket.getDescription());
        assertThat(mAddEditTicketPresenter.isDataMissing(), is(false));
    }
}
