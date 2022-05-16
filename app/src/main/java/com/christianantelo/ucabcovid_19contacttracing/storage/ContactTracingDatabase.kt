package com.christianantelo.ucabcovid_19contacttracing.storage

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.christianantelo.ucabcovid_19contacttracing.DataClasses.ContactTracing
import com.christianantelo.ucabcovid_19contacttracing.Fragments.BorrarTodoConfirmationFragment


@Database(
    entities = [ContactTracing::class],
    version = 1
)
abstract class ContactTracingDatabase : RoomDatabase() {
    abstract fun getContactTracingDao(): ContactTracingDAO

    companion object{

        @Volatile
        private  var instance: ContactTracingDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: BorrarTodoConfirmationFragment) = instance ?: synchronized(LOCK){
            instance ?: createDatabase(context).also{ instance = it}
        }
        private fun createDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                ContactTracingDatabase::class.java,
                "contact_tracing_db.db"
            ).build()
    }
}