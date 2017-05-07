package com.fabriciosuarte.taskmanager.test;

import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.fabriciosuarte.taskmanager.MainActivity;
import com.fabriciosuarte.taskmanager.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.ComponentNameMatchers.hasShortClassName;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static org.hamcrest.Matchers.allOf;

/**
 * Created by suarte on 02/04/17.
 */

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ShowActivityBehaviorTest {

    @Rule
    public IntentsTestRule<MainActivity> mActivityRule = new IntentsTestRule<>(
            MainActivity.class);

    @Test
    public void verifyAddTaskActivityStarted() {

        //Perform the click on the Fab...
        onView(ViewMatchers.withId(R.id.fab)).perform(click());

        //Checks if the AddTaskActivity was loaded properly
        intended( allOf(
                        hasComponent(hasShortClassName(".AddTaskActivity"))
                        )
                );
    }

}
