package com.christianantelo.ucabcovid_19contacttracing.storage

import androidx.lifecycle.LiveData
import androidx.room.*
import com.christianantelo.ucabcovid_19contacttracing.DataClasses.BluetoothDevice

@Dao
interface ContactTracingDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(bluetooth_Device: BluetoothDevice)

    @Delete
    suspend fun deleteContact(bluetooth_Device: BluetoothDevice)

    @Query("SELECT * FROM table_of_contacts ORDER BY contactDate DESC")
    fun getAllContactSortByDate(): LiveData<List<BluetoothDevice>>

    @Query("DELETE FROM table_of_contacts WHERE (julianday('now') - julianday(contactDate))>14")
    fun borrarContactsMasde14Dias()

    @Query("DELETE FROM table_of_contacts")
    fun clear()
}


