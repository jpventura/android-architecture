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

import android.app.Activity;
import android.support.annotation.NonNull;

import com.example.android.architecture.blueprints.todoapp.addeditticket.AddEditTicketActivity;
import com.example.android.architecture.blueprints.todoapp.data.Ticket;
import com.example.android.architecture.blueprints.todoapp.data.source.TicketsDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.TicketsRepository;
import com.example.android.architecture.blueprints.todoapp.util.EspressoIdlingResource;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Listens to user actions from the UI ({@link TicketsFragment}), retrieves the data and updates the
 * UI as required.
 */
public class TicketsPresenter implements TicketsContract.Presenter {

    private final TicketsRepository mTicketsRepository;

    private final TicketsContract.View mTicketsView;

    private TicketsFilterType mCurrentFiltering = TicketsFilterType.ALL_TASKS;

    private boolean mFirstLoad = true;

    public TicketsPresenter(@NonNull TicketsRepository tasksRepository, @NonNull TicketsContract.View tasksView) {
        mTicketsRepository = checkNotNull(tasksRepository, "tasksRepository cannot be null");
        mTicketsView = checkNotNull(tasksView, "tasksView cannot be null!");

        mTicketsView.setPresenter(this);
    }

    @Override
    public void start() {
        loadTickets(false);
    }

    @Override
    public void result(int requestCode, int resultCode) {
        // If a task was successfully added, show snackbar
        if (AddEditTicketActivity.REQUEST_ADD_TASK == requestCode && Activity.RESULT_OK == resultCode) {
            mTicketsView.showSuccessfullySavedMessage();
        }
    }

    @Override
    public void loadTickets(boolean forceUpdate) {
        // Simplification for sample: a network reload will be forced on first load.
        loadTickets(forceUpdate || mFirstLoad, true);
        mFirstLoad = false;
    }

    /**
     * @param forceUpdate   Pass in true to refresh the data in the {@link TicketsDataSource}
     * @param showLoadingUI Pass in true to display a loading icon in the UI
     */
    private void loadTickets(boolean forceUpdate, final boolean showLoadingUI) {
        if (showLoadingUI) {
            mTicketsView.setLoadingIndicator(true);
        }
        if (forceUpdate) {
            mTicketsRepository.refreshTickets();
        }

        // The network request might be handled in a different thread so make sure Espresso knows
        // that the app is busy until the response is handled.
        EspressoIdlingResource.increment(); // App is busy until further notice

        mTicketsRepository.getTickets(new TicketsDataSource.LoadTicketsCallback() {
            @Override
            public void onTicketsLoaded(List<Ticket> tickets) {
                List<Ticket> tasksToShow = new ArrayList<Ticket>();

                // This callback may be called twice, once for the cache and once for loading
                // the data from the server API, so we check before decrementing, otherwise
                // it throws "Counter has been corrupted!" exception.
                if (!EspressoIdlingResource.getIdlingResource().isIdleNow()) {
                    EspressoIdlingResource.decrement(); // Set app as idle.
                }

                // We filter the tickets based on the requestType
                for (Ticket ticket : tickets) {
                    switch (mCurrentFiltering) {
                        case ALL_TASKS:
                            tasksToShow.add(ticket);
                            break;
                        case ACTIVE_TASKS:
                            if (ticket.isActive()) {
                                tasksToShow.add(ticket);
                            }
                            break;
                        case COMPLETED_TASKS:
                            if (ticket.isCompleted()) {
                                tasksToShow.add(ticket);
                            }
                            break;
                        default:
                            tasksToShow.add(ticket);
                            break;
                    }
                }
                // The view may not be able to handle UI updates anymore
                if (!mTicketsView.isActive()) {
                    return;
                }
                if (showLoadingUI) {
                    mTicketsView.setLoadingIndicator(false);
                }

                processTickets(tasksToShow);
            }

            @Override
            public void onDataNotAvailable() {
                // The view may not be able to handle UI updates anymore
                if (!mTicketsView.isActive()) {
                    return;
                }
                mTicketsView.showLoadingTicketsError();
            }
        });
    }

    private void processTickets(List<Ticket> tickets) {
        if (tickets.isEmpty()) {
            // Show a message indicating there are no tickets for that filter type.
            processEmptyTickets();
        } else {
            // Show the list of tickets
            mTicketsView.showTickets(tickets);
            // Set the filter label's text.
            showFilterLabel();
        }
    }

    private void showFilterLabel() {
        switch (mCurrentFiltering) {
            case ACTIVE_TASKS:
                mTicketsView.showActiveFilterLabel();
                break;
            case COMPLETED_TASKS:
                mTicketsView.showCompletedFilterLabel();
                break;
            default:
                mTicketsView.showAllFilterLabel();
                break;
        }
    }

    private void processEmptyTickets() {
        switch (mCurrentFiltering) {
            case ACTIVE_TASKS:
                mTicketsView.showNoActiveTickets();
                break;
            case COMPLETED_TASKS:
                mTicketsView.showNoCompletedTickets();
                break;
            default:
                mTicketsView.showNoTickets();
                break;
        }
    }

    @Override
    public void addNewTicket() {
        mTicketsView.showAddTicket();
    }

    @Override
    public void openTicketDetails(@NonNull Ticket requestedTicket) {
        checkNotNull(requestedTicket, "requestedTicket cannot be null!");
        mTicketsView.showTicketDetailsUi(requestedTicket.getId());
    }

    @Override
    public void completeTicket(@NonNull Ticket completedTicket) {
        checkNotNull(completedTicket, "completedTicket cannot be null!");
        mTicketsRepository.completeTicket(completedTicket);
        mTicketsView.showTicketMarkedComplete();
        loadTickets(false, false);
    }

    @Override
    public void activateTicket(@NonNull Ticket activeTicket) {
        checkNotNull(activeTicket, "activeTicket cannot be null!");
        mTicketsRepository.activateTicket(activeTicket);
        mTicketsView.showTicketMarkedActive();
        loadTickets(false, false);
    }

    @Override
    public void clearCompletedTickets() {
        mTicketsRepository.clearCompletedTickets();
        mTicketsView.showCompletedTicketsCleared();
        loadTickets(false, false);
    }

    /**
     * Sets the current task filtering type.
     *
     * @param requestType Can be {@link TicketsFilterType#ALL_TASKS},
     *                    {@link TicketsFilterType#COMPLETED_TASKS}, or
     *                    {@link TicketsFilterType#ACTIVE_TASKS}
     */
    @Override
    public void setFiltering(TicketsFilterType requestType) {
        mCurrentFiltering = requestType;
    }

    @Override
    public TicketsFilterType getFiltering() {
        return mCurrentFiltering;
    }

}
