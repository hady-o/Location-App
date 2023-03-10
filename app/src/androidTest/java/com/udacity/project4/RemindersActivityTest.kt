package com.udacity.project4

import android.app.Activity
import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers
import org.hamcrest.core.IsNot
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get

@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application
    private val data = DataBindingIdlingResource()

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @After
    fun unregisterIdlingResource() {

        IdlingRegistry.getInstance().unregister(data)
    }
    @Before
    fun registerIdlingResource() {

        IdlingRegistry.getInstance().register(data)
    }
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }


    @ExperimentalCoroutinesApi
    @Test
    fun showReminderSavedToast() = runBlocking{

        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        data.monitorActivity(activityScenario)
        val rem1 = ReminderDTO("t1" , "des1" , "lov1" , 114.0 , 114.0 , "32")

        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.reminderTitle)).perform(
            ViewActions.typeText(rem1.title),
            ViewActions.closeSoftKeyboard()
        )
        onView(withId(R.id.reminderDescription)).perform(
            ViewActions.typeText(rem1.description),
            ViewActions.closeSoftKeyboard()
        )
        onView(withId(R.id.selectLocation)).perform(click())
        onView(withId(com.google.android.material.R.id.snackbar_action)).perform(click())
        onView(withId(R.id.map)).perform(ViewActions.longClick())
        onView(withId(R.id.saveReminder)).perform(click())
        onView(withId(R.id.saveReminder)).perform(click())
        var activity: Activity? = null
        activityScenario.onActivity {
            activity = it
        }
        onView(ViewMatchers.withText(R.string.reminder_saved)).inRoot(
            RootMatchers.withDecorView(
                IsNot.not(Matchers.`is`(activity?.window?.decorView))
            )
        )
            .check(
                ViewAssertions.matches(
                    ViewMatchers.isDisplayed()
                )
            )
        activityScenario.close()
    }





}
