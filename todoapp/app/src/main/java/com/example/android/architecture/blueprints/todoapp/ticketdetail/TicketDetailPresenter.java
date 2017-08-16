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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.android.architecture.blueprints.todoapp.data.Ticket;
import com.example.android.architecture.blueprints.todoapp.data.source.TicketsDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.TicketsRepository;
import com.google.common.base.Strings;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Listens to user actions from the UI ({@link TicketDetailFragment}), retrieves the data and updates
 * the UI as required.
 */
public class TicketDetailPresenter implements TicketDetailContract.Presenter {

    private final TicketsRepository mTicketsRepository;

    private final TicketDetailContract.View mTicketDetailView;

    @Nullable
    private String mTicketId;

    public TicketDetailPresenter(@Nullable String taskId,
                                 @NonNull TicketsRepository tasksRepository,
                                 @NonNull TicketDetailContract.View taskDetailView) {
        mTicketId = taskId;
        mTicketsRepository = checkNotNull(tasksRepository, "tasksRepository cannot be null!");
        mTicketDetailView = checkNotNull(taskDetailView, "taskDetailView cannot be null!");

        mTicketDetailView.setPresenter(this);
    }

    @Override
    public void start() {
        openTicket();
    }

    private void openTicket() {
        if (Strings.isNullOrEmpty(mTicketId)) {
            mTicketDetailView.showMissingTicket();
            return;
        }

        mTicketDetailView.setLoadingIndicator(true);
        mTicketsRepository.getTicket(mTicketId, new TicketsDataSource.GetTicketCallback() {
            @Override
            public void onTicketLoaded(Ticket ticket) {
                // The view may not be able to handle UI updates anymore
                if (!mTicketDetailView.isActive()) {
                    return;
                }
                mTicketDetailView.setLoadingIndicator(false);
                if (null == ticket) {
                    mTicketDetailView.showMissingTicket();
                } else {
                    showTicket(ticket);
                }
            }

            @Override
            public void onDataNotAvailable() {
                // The view may not be able to handle UI updates anymore
                if (!mTicketDetailView.isActive()) {
                    return;
                }
                mTicketDetailView.showMissingTicket();
            }
        });
    }

    @Override
    public void editTicket() {
        if (Strings.isNullOrEmpty(mTicketId)) {
            mTicketDetailView.showMissingTicket();
            return;
        }
        mTicketDetailView.showEditTicket(mTicketId);
    }

    @Override
    public void deleteTicket() {
        if (Strings.isNullOrEmpty(mTicketId)) {
            mTicketDetailView.showMissingTicket();
            return;
        }
        mTicketsRepository.deleteTicket(mTicketId);
        mTicketDetailView.showTicketDeleted();
    }

    @Override
    public void completeTicket() {
        if (Strings.isNullOrEmpty(mTicketId)) {
            mTicketDetailView.showMissingTicket();
            return;
        }
        mTicketsRepository.completeTicket(mTicketId);
        mTicketDetailView.showTicketMarkedComplete();
    }

    @Override
    public void activateTicket() {
        if (Strings.isNullOrEmpty(mTicketId)) {
            mTicketDetailView.showMissingTicket();
            return;
        }
        mTicketsRepository.activateTicket(mTicketId);
        mTicketDetailView.showTicketMarkedActive();
    }

    private void showTicket(@NonNull Ticket ticket) {
        String title = ticket.getTitle();
        String description = ticket.getDescription();

        if (Strings.isNullOrEmpty(title)) {
            mTicketDetailView.hideTitle();
        } else {
            mTicketDetailView.showTitle(title);
        }

        if (Strings.isNullOrEmpty(description)) {
            mTicketDetailView.hideDescription();
        } else {
            mTicketDetailView.showDescription(description);
        }
        mTicketDetailView.showCompletionStatus(ticket.isCompleted());
    }
}
