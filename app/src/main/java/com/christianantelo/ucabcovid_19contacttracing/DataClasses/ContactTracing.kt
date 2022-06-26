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
    var serviceData: Int,
    var contactDate: Long,
) {
    var exponent = ((-87.4 - RSSI) / (10 * 4))
    var distance = base.toDouble().pow(exponent.toDouble())

    //var decodedServiceData = read4BytesFromBuffer(serviceData)

}
