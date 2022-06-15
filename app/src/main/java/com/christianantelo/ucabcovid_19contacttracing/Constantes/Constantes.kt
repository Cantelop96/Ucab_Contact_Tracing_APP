package com.christianantelo.ucabcovid_19contacttracing.Constantes

import java.util.*

object Constantes {

    const val NOMBRE_DE_LA_BASE_DE_DATOS = "contactos_cercanos_db"
    val My_UUID = UUID.fromString("9778965b-73f1-4003-953a-9432d3c6fb26")
    const val ACTION_START_OR_RESUME_SERVICE = "ACTION_START_OR_RESUME_SERVICE"
    const val ACTION_RESUME_SERVICE_DESPUES_DE_CUARENTENA =
        "ACTION_RESUME_SERVICE_DESPUES_DE_CUARENTENA"
    const val ACTION_STOP_CONTACT_TRACING_SERVIVE = "ACTION_STOP_CONTACT_TRACING_SERVIVE"

    const val NOTIFICATION_CHANNEL_ID_NOTIFICACION = "contactTracing_channel_notificacion"
    const val NOTIFICATION_CHANNEL_ID_SERVICIO = "contactTracing_channel_servicio"
    const val NOTIFICATION_CHANNEL_NAME_NOTIFICACION = "Contact Tracing"
    const val NOTIFICATION_CHANNEL_NAME_SERVICIO = "Contact Tracing"
    var NOTIFICATION_ID_NOTIFICACION = 1
    const val NOTIFICATION_ID_NOTIFICACION_FINALIZA_CUARNTENA = 3
    const val NOTIFICATION_ID_SERVICIO = 2
    const val ACTION_NOTIFICACION_INFECCION = "ACTION_NOTIFICACION_INFECCION"

    const val ENABLE_BLUETOOTH_REQUEST_CODE = 1
    const val LOCATION_PERMISSION_REQUEST_CODE = 2

}