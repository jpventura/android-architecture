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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.android.architecture.blueprints.todoapp.data.Ticket;
import com.example.android.architecture.blueprints.todoapp.data.source.TicketsDataSource;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Listens to user actions from the UI ({@link AddEditTicketFragment}), retrieves the data and updates
 * the UI as required.
 */
public class AddEditTicketPresenter implements AddEditTicketContract.Presenter,
        TicketsDataSource.GetTicketCallback {

    @NonNull
    private final TicketsDataSource mTicketsRepository;

    @NonNull
    private final AddEditTicketContract.View mAddTicketView;

    @Nullable
    private String mTicketId;

    private boolean mIsDataMissing;

    /**
     * Creates a presenter for the add/edit view.
     *
     * @param TicketId ID of the task to edit or null for a new task
     * @param tasksRepository a repository of data for tasks
     * @param addTicketView the add/edit view
     * @param shouldLoadDataFromRepo whether data needs to be loaded or not (for config changes)
     */
    public AddEditTicketPresenter(@Nullable String taskId, @NonNull TicketsDataSource tasksRepository,
                                  @NonNull AddEditTicketContract.View addTicketView, boolean shouldLoadDataFromRepo) {
        mTicketId = taskId;
        mTicketsRepository = checkNotNull(tasksRepository);
        mAddTicketView = checkNotNull(addTicketView);
        mIsDataMissing = shouldLoadDataFromRepo;

        mAddTicketView.setPresenter(this);
    }

    @Override
    public void start() {
        if (!isNewTicket() && mIsDataMissing) {
            populateTicket();
        }
    }

    @Override
    public void saveTicket(String title, String description) {
        if (isNewTicket()) {
            createTicket(title, description);
        } else {
            updateTicket(title, description);
        }
    }

    @Override
    public void populateTicket() {
        if (isNewTicket()) {
            throw new RuntimeException("populateTicket() was called but task is new.");
        }
        mTicketsRepository.getTicket(mTicketId, this);
    }

    @Override
    public void onTicketLoaded(Ticket ticket) {
        // The view may not be able to handle UI updates anymore
        if (mAddTicketView.isActive()) {
            mAddTicketView.setTitle(ticket.getTitle());
            mAddTicketView.setDescription(ticket.getDescription());
        }
        mIsDataMissing = false;
    }

    @Override
    public void onDataNotAvailable() {
        // The view may not be able to handle UI updates anymore
        if (mAddTicketView.isActive()) {
            mAddTicketView.showEmptyTicketError();
        }
    }

    @Override
    public boolean isDataMissing() {
        return mIsDataMissing;
    }

    private boolean isNewTicket() {
        return mTicketId == null;
    }

    private void createTicket(String title, String description) {
        Ticket newTicket = new Ticket(title, description);
        if (newTicket.isEmpty()) {
            mAddTicketView.showEmptyTicketError();
        } else {
            mTicketsRepository.saveTicket(newTicket);
            mAddTicketView.showTicketsList();
        }
    }

    private void updateTicket(String title, String description) {
        if (isNewTicket()) {
            throw new RuntimeException("updateTicket() was called but task is new.");
        }
        mTicketsRepository.saveTicket(new Ticket(title, description, mTicketId));
        mAddTicketView.showTicketsList(); // After an edit, go back to the list.
    }
}
