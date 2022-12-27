package com.udacity.project4.locationreminders.savereminder

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.hamcrest.core.Is
import org.junit.Assert
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
    private val nullReminder=  ReminderDataItem("r","des",null,null,null)
    private val reminder = ReminderDataItem("rem", "des", "loc", 1.0, 2.2,"33")

    // initializations
    @Before
    fun setUpViewModel(){
        stopKoin()
        source = FakeDataSource()
        viewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), source)
    }

    @Test
    fun loading(){

        coroutineRule.pauseDispatcher()
        viewModel.saveReminder(reminder)
        Assert.assertThat(viewModel.showLoading.getOrAwaitValue(), CoreMatchers.`is`(true))
        coroutineRule.resumeDispatcher()
        Assert.assertThat(viewModel.showLoading.getOrAwaitValue(), CoreMatchers.`is`(false))
        Assert.assertThat(viewModel.showToast.getOrAwaitValue(), CoreMatchers.`is`("Reminder Saved !"))
        Assert.assertThat(viewModel.navigationCommand.getOrAwaitValue(), CoreMatchers.`is`(
            NavigationCommand.Back))
    }
    @Test
    fun setDataTest(){
        Assert.assertThat(viewModel.validateEnteredData(nullReminder), CoreMatchers.`is`(false))
        Assert.assertThat(viewModel.validateEnteredData(reminder), CoreMatchers.`is`(true))


    }
}