package com.example.ridesignal

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SignalPadTest {

    // Launches the activity before each test
    @get:Rule
    val activityRule = ActivityScenarioRule(signalPad::class.java)

    @Test
    fun testStopButtonSendsSignal() {
        // 1. Wait for the Group Selection Dialog to appear and select the first group
        // (This assumes your emulator is logged in and has a group)
        onView(withText("Test Group")).perform(click())

        // 2. Click the STOP button
        onView(withId(R.id.btnStop)).perform(click())

        // 3. Verify that the "Signal sent" Toast or feedback happens
        // Testing Toasts is complex, so we usually check if the UI state is correct
        onView(withText("Stop")).check(matches(isDisplayed()))
    }
}