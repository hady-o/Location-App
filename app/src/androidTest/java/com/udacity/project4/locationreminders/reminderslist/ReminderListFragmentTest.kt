package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.test.get
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@MediumTest
class ReminderListFragmentTest : AutoCloseKoinTest() {
    private lateinit var context: Application
    private lateinit var source: ReminderDataSource
    @get:Rule
    var iexecutorRule = InstantTaskExecutorRule()
    @Before
    fun setup() {
        stopKoin()
        context = getApplicationContext()
        val mo = module {

            viewModel {
                RemindersListViewModel(
                    get(),
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    get(),
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(context) }
        }

        startKoin {
            androidContext(context)
            modules(listOf(mo))
        }

        source = get()

        runBlocking {
            source.deleteAllReminders()
        }
    }

    @Test
    fun navTest()  {
        val scen = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        val con = mock(NavController::class.java)
        scen.onFragment {
            Navigation.setViewNavController(it.view!!, con)
        }
        onView(withId(R.id.addReminderFAB))
            .perform(click())
        verify(con).navigate(
            ReminderListFragmentDirections.toSaveReminder()
        )
    }

    @Test
    fun remTest() = runBlockingTest {
        runBlocking {
            val rem =   ReminderDTO("t1", "des", "loc", 5.6, 3.2, "33")
            source.saveReminder(rem)
            launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
            onView(withText(rem.title)).check(matches(isDisplayed()))
            onView(withText(rem.description)).check(matches(isDisplayed()))
            onView(withText(rem.location)).check(matches(isDisplayed()))
        }
    }
}