package com.udacity.project4.locationreminders.reminderslist

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    private lateinit var viewModel : RemindersListViewModel
    private lateinit var source: FakeDataSource



    @get:Rule
    var excutor = InstantTaskExecutorRule()
    @get:Rule
    var coroutineRule = com.udacity.project4.locationreminders.coroutineRule()


    @Before
    fun getModel() {
        stopKoin()
        source = FakeDataSource()

        viewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), source)
    }
    @Test
    fun loading(){
        coroutineRule.pauseDispatcher()
        viewModel.loadReminders()
        Assert.assertThat(viewModel.showLoading.getOrAwaitValue(), CoreMatchers.`is`(true))
        coroutineRule.resumeDispatcher()
        Assert.assertThat(viewModel.showLoading.getOrAwaitValue(), CoreMatchers.`is`(false))
        Assert.assertThat(viewModel.remindersList.getOrAwaitValue().isEmpty(), CoreMatchers.`is`(true))
    }


    @Test
    fun emptyTest(){
        source.deleteAll()
        viewModel.loadReminders()
        Assert.assertThat(viewModel.remindersList.getOrAwaitValue().isEmpty(), CoreMatchers.`is`(true))
    }
    @Test
    fun errorTest(){
        source.dataLoded=false
        viewModel.loadReminders()
        Assert.assertThat(viewModel.showSnackBar.getOrAwaitValue(), CoreMatchers.`is`("error"))
        source.dataLoded=true

    }
}