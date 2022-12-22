package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    @get:Rule
    var rule = InstantTaskExecutorRule()
    private lateinit var instance: RemindersDatabase

    @Before
    fun getDb() {
        instance = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @Test
    fun insertFunctionTest1() = runBlockingTest {


        val case1 = ReminderDTO("t1", "des", "loc", 4.6, 8.6, "case1")


        instance.reminderDao().saveReminder(case1)

        val expectedOutput =
            instance.reminderDao().getReminderById("case1")
                ?.let { case1 == it }

        assertThat(expectedOutput,`is`(true))

        instance.close()
    }

}