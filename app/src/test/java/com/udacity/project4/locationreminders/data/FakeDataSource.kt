package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {


    var dataLoded: Boolean = true
    var source: HashMap<String, ReminderDTO> = HashMap()


    override suspend fun getReminders(): Result<List<ReminderDTO>> {

        try {
            if (dataLoded != true) {
                throw Exception("error")
            } else {
                val mylist: MutableList<ReminderDTO> = ArrayList()
                source.forEach {
                    mylist.add(it.value)
                }
                return Result.Success(mylist)
            }
        } catch (e: Exception) {
          return  Result.Error(e.localizedMessage)
        }

    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        source.put(reminder.id,reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
         try {
            if (!dataLoded) {

                throw Exception("error")
            } else {

                source[id]?.let {
                    Result.Success(it)
                }
                return Result.Error("error")
            }
        }
        catch (ex:Exception)
        {
           return Result.Error(ex.localizedMessage)
        }
    }

    override suspend fun deleteAllReminders() {
       source.clear()
    }


}