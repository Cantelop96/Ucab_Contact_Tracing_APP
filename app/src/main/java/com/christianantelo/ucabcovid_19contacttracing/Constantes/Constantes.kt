package com.christianantelo.ucabcovid_19contacttracing.Constantes

import java.util.*

object Constantes {

    const val NOMBRE_DE_LA_BASE_DE_DATOS = "contactos_cercanos_db"
    val My_UUID = UUID.fromString("9778965b-73f1-4003-953a-9432d3c6fb26")
    const val ACTION_START_OR_RESUME_SERVICE = "ACTION_START_OR_RESUME_SERVICE"
    const val ACTION_PAUSE_SERVICE = "ACTION_PAUSE_SERVICE"
    const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"

    const val NOTIFICATION_CHANNEL_ID = "contactTracing_channel"
    const val NOTIFICATION_CHANNEL_NAME = "Contact Tracing"
    const val NOTIFICATION_ID = 1
    const val ACTION_NOTIFICACION_INFECCION = "ACTION_NOTIFICACION_INFECCION"

    const val ENABLE_BLUETOOTH_REQUEST_CODE = 1
    const val LOCATION_PERMISSION_REQUEST_CODE = 2
}