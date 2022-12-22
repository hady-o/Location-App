package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.InstrumentationRegistry
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    private lateinit var instance: RemindersDatabase
    private lateinit var dao: RemindersDao
    private lateinit var repo: RemindersLocalRepository
    val remnds : MutableList<ReminderDTO> = ArrayList()

    @Before
    fun getData() {
        instance = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().context,
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
        dao = instance.reminderDao()
        repo =
            RemindersLocalRepository(
                dao
            )
        for(i in 0..5){
            remnds.add(
                ReminderDTO(i.toString(), "des","loc", i.toDouble(), i.toDouble(), i.toString())
            )
        }
    }

    @Test
    fun insertAllTest2() = runBlocking {

        for (remnd in remnds) {
            instance.reminderDao().saveReminder(remnd)
        }

        val list = dao.getReminders()

        for (i in 0 until 5)
        {
            val expected = (remnds[i]==list[i])
            MatcherAssert.assertThat(expected,`is`(true))
        }
        instance.close()

    }

}