package com.udacity.project4.locationreminders.savereminder

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.MatcherAssert
import org.hamcrest.core.Is
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.P])
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {


    private lateinit var source: FakeDataSource
    private lateinit var viewModel: SaveReminderViewModel

    @get:Rule
    var coroutineRule = com.udacity.project4.locationreminders.coroutineRule()

    // app context provider
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()
    private val reminder = ReminderDataItem("rem", "des", "loc", 1.0, 2.2,"33")

    // initializations
    @Before
    fun setUpViewModel(){
        stopKoin()
        source = FakeDataSource()
        viewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), source)
    }

    @Test
    fun reminderValidation(){

        val result = viewModel.validateEnteredData(reminder)
        MatcherAssert.assertThat(result, Is.`is`(true))
    }

}