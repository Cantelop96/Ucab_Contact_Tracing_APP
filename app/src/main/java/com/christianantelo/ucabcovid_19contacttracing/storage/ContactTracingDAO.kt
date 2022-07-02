package com.christianantelo.ucabcovid_19contacttracing.storage

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.LiveData
import androidx.room.*
import com.christianantelo.ucabcovid_19contacttracing.DataClasses.ContactTracing


@Dao
interface ContactTracingDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contactTracing: ContactTracing)

    @Delete
    suspend fun deleteContact(contactTracing: ContactTracing)

    @Query("SELECT * FROM table_of_contacts ORDER BY contactDate DESC")
    suspend fun getAllContactSortByDate(): MutableList<ContactTracing>

    @Query("DELETE FROM table_of_contacts WHERE (julianday('now') - julianday(contactDate))>10")
    suspend fun borrarContactsMasde14Dias()

    @Query("DELETE FROM table_of_contacts")
    suspend fun clear()
}