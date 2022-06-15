package com.christianantelo.ucabcovid_19contacttracing.DataClasses

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Timestamp
import java.util.*
import kotlin.math.pow

val base = 10


@Entity(tableName = "table_of_contacts")
data class ContactTracing(
    @PrimaryKey
    var address: String,
    var RSSI: Int,
    var txPowerLevel: Int,
    var serviceData: ByteArray,
    var contactDate: Long,
) {
    var exponent = ((txPowerLevel - RSSI) / -10 * 2)
    var distance = base.toDouble().pow(exponent.toDouble())

    var decodedServiceData = read4BytesFromBuffer(serviceData)

    fun read4BytesFromBuffer(buffer: ByteArray, offset: Int = 0): Int {
        return (buffer[offset + 3].toInt() shl 24) or
                (buffer[offset + 2].toInt() and 0xff shl 16) or
                (buffer[offset + 1].toInt() and 0xff shl 8) or
                (buffer[offset + 0].toInt() and 0xff)
    }
}
