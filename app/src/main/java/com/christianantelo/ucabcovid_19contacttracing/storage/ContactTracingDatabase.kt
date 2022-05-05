package com.christianantelo.ucabcovid_19contacttracing.storage

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.christianantelo.ucabcovid_19contacttracing.DataClasses.BluetoothDevice

@Database(
    entities = [BluetoothDevice::class],
    version = 1
)
abstract class ContactTracingDatabase : RoomDatabase() {


    abstract fun getContactTracingDao(): ContactTracingDAO
}