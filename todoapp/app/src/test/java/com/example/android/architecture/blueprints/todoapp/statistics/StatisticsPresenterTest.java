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

package com.example.android.architecture.blueprints.todoapp.statistics;

import com.example.android.architecture.blueprints.todoapp.data.Ticket;
import com.example.android.architecture.blueprints.todoapp.data.source.TicketsDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.TicketsRepository;
import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the implementation of {@link StatisticsPresenter}
 */
public class StatisticsPresenterTest {

    private static List<Ticket> Tickets;

    @Mock
    private TicketsRepository mTicketsRepository;

    @Mock
    private StatisticsContract.View mStatisticsView;

    /**
     * {@link ArgumentCaptor} is a powerful Mockito API to capture argument values and use them to
     * perform further actions or assertions on them.
     */
    @Captor
    private ArgumentCaptor<TicketsDataSource.LoadTicketsCallback> mLoadTicketsCallbackCaptor;


    private StatisticsPresenter mStatisticsPresenter;

    @Before
    public void setupStatisticsPresenter() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        // Get a reference to the class under test
        mStatisticsPresenter = new StatisticsPresenter(mTicketsRepository, mStatisticsView);

        // The presenter won't update the view unless it's active.
        when(mStatisticsView.isActive()).thenReturn(true);

        // We start the tasks to 3, with one active and two completed
        Tickets = Lists.newArrayList(new Ticket("Title1", "Description1"),
                new Ticket("Title2", "Description2", true), new Ticket("Title3", "Description3", true));
    }

    @Test
    public void createPresenter_setsThePresenterToView() {
        // Get a reference to the class under test
        mStatisticsPresenter = new StatisticsPresenter(mTicketsRepository, mStatisticsView);

        // Then the presenter is set to the view
        verify(mStatisticsView).setPresenter(mStatisticsPresenter);
    }

    @Test
    public void loadEmptyTicketsFromRepository_CallViewToDisplay() {
        // Given an initialized StatisticsPresenter with no tasks
        Tickets.clear();

        // When loading of Tickets is requested
        mStatisticsPresenter.start();

        //Then progress indicator is shown
        verify(mStatisticsView).setProgressIndicator(true);

        // Callback is captured and invoked with stubbed tasks
        verify(mTicketsRepository).getTickets(mLoadTicketsCallbackCaptor.capture());
        mLoadTicketsCallbackCaptor.getValue().onTicketsLoaded(Tickets);

        // Then progress indicator is hidden and correct data is passed on to the view
        verify(mStatisticsView).setProgressIndicator(false);
        verify(mStatisticsView).showStatistics(0, 0);
    }

    @Test
    public void loadNonEmptyTicketsFromRepository_CallViewToDisplay() {
        // Given an initialized StatisticsPresenter with 1 active and 2 completed tasks

        // When loading of Tickets is requested
        mStatisticsPresenter.start();

        //Then progress indicator is shown
        verify(mStatisticsView).setProgressIndicator(true);

        // Callback is captured and invoked with stubbed tasks
        verify(mTicketsRepository).getTickets(mLoadTicketsCallbackCaptor.capture());
        mLoadTicketsCallbackCaptor.getValue().onTicketsLoaded(Tickets);

        // Then progress indicator is hidden and correct data is passed on to the view
        verify(mStatisticsView).setProgressIndicator(false);
        verify(mStatisticsView).showStatistics(1, 2);
    }

    @Test
    public void loadStatisticsWhenTicketsAreUnavailable_CallErrorToDisplay() {
        // When statistics are loaded
        mStatisticsPresenter.start();

        // And tasks data isn't available
        verify(mTicketsRepository).getTickets(mLoadTicketsCallbackCaptor.capture());
        mLoadTicketsCallbackCaptor.getValue().onDataNotAvailable();

        // Then an error message is shown
        verify(mStatisticsView).showLoadingStatisticsError();
    }
}
