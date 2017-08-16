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

import android.app.Activity;
import android.content.Intent;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.filters.LargeTest;

import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.TestUtils;
import com.example.android.architecture.blueprints.todoapp.data.FakeTicketsRemoteDataSource;
import com.example.android.architecture.blueprints.todoapp.data.Ticket;
import com.example.android.architecture.blueprints.todoapp.data.source.TicketsRepository;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.IsNot.not;

/**
 * Tests for the tasks screen, the main screen which contains a list of all tasks.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class TicketDetailScreenTest {

    private static String TASK_TITLE = "ATSL";

    private static String TASK_DESCRIPTION = "Rocks";

    /**
     * {@link Ticket} stub that is added to the fake service API layer.
     */
    private static Ticket activeTicket = new Ticket(TASK_TITLE, TASK_DESCRIPTION, false);

    /**
     * {@link Ticket} stub that is added to the fake service API layer.
     */
    private static Ticket completedTicket = new Ticket(TASK_TITLE, TASK_DESCRIPTION, true);

    /**
     * {@link ActivityTestRule} is a JUnit {@link Rule @Rule} to launch your activity under test.
     *
     * <p>
     * Rules are interceptors which are executed for each test method and are important building
     * blocks of Junit tests.
     *
     * <p>
     * Sometimes an {@link Activity} requires a custom start {@link Intent} to receive data
     * from the source Activity. ActivityTestRule has a feature which let's you lazily start the
     * Activity under test, so you can control the Intent that is used to start the target Activity.
     */
    @Rule
    public ActivityTestRule<TicketDetailActivity> mTaskDetailActivityTestRule =
            new ActivityTestRule<>(TicketDetailActivity.class, true /* Initial touch mode  */,
                    false /* Lazily launch activity */);

    private void loadActiveTask() {
        startActivityWithWithStubbedTask(activeTicket);
    }

    private void loadCompletedTask() {
        startActivityWithWithStubbedTask(completedTicket);
    }

    /**
     * Setup your test fixture with a fake ticket id. The {@link TicketDetailActivity} is started with
     * a particular ticket id, which is then loaded from the service API.
     *
     * <p>
     * Note that this test runs hermetically and is fully isolated using a fake implementation of
     * the service API. This is a great way to make your tests more reliable and faster at the same
     * time, since they are isolated from any outside dependencies.
     */
    private void startActivityWithWithStubbedTask(Ticket ticket) {
        // Add a ticket stub to the fake service api layer.
        TicketsRepository.destroyInstance();
        FakeTicketsRemoteDataSource.getInstance().addTasks(ticket);

        // Lazily start the Activity from the ActivityTestRule this time to inject the start Intent
        Intent startIntent = new Intent();
        startIntent.putExtra(TicketDetailActivity.EXTRA_TASK_ID, ticket.getId());
        mTaskDetailActivityTestRule.launchActivity(startIntent);
    }

    @Test
    public void activeTaskDetails_DisplayedInUi() throws Exception {
        loadActiveTask();

        // Check that the task title and description are displayed
        onView(withId(R.id.task_detail_title)).check(matches(withText(TASK_TITLE)));
        onView(withId(R.id.task_detail_description)).check(matches(withText(TASK_DESCRIPTION)));
        onView(withId(R.id.task_detail_complete)).check(matches(not(isChecked())));
    }

    @Test
    public void completedTaskDetails_DisplayedInUi() throws Exception {
        loadCompletedTask();

        // Check that the task title and description are displayed
        onView(withId(R.id.task_detail_title)).check(matches(withText(TASK_TITLE)));
        onView(withId(R.id.task_detail_description)).check(matches(withText(TASK_DESCRIPTION)));
        onView(withId(R.id.task_detail_complete)).check(matches(isChecked()));
    }

    @Test
    public void orientationChange_menuAndTaskPersist() {
        loadActiveTask();

        // Check delete menu item is displayed and is unique
        onView(withId(R.id.menu_delete)).check(matches(isDisplayed()));

        TestUtils.rotateOrientation(mTaskDetailActivityTestRule.getActivity());

        // Check that the task is shown
        onView(withId(R.id.task_detail_title)).check(matches(withText(TASK_TITLE)));
        onView(withId(R.id.task_detail_description)).check(matches(withText(TASK_DESCRIPTION)));

        // Check delete menu item is displayed and is unique
        onView(withId(R.id.menu_delete)).check(matches(isDisplayed()));
    }

}
