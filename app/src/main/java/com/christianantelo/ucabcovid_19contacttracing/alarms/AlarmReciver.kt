package com.christianantelo.ucabcovid_19contacttracing.alarms

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import com.christianantelo.ucabcovid_19contacttracing.Constantes.Constantes
import com.christianantelo.ucabcovid_19contacttracing.Constantes.Constantes.ACTION_RESUME_SERVICE_DESPUES_DE_CUARENTENA
import com.christianantelo.ucabcovid_19contacttracing.Constantes.Constantes.NOTIFICATION_CHANNEL_ID_NOTIFICACION
import com.christianantelo.ucabcovid_19contacttracing.Constantes.Constantes.NOTIFICATION_ID_NOTIFICACION
import com.christianantelo.ucabcovid_19contacttracing.Constantes.Constantes.NOTIFICATION_ID_NOTIFICACION_FINALIZA_CUARNTENA
import com.christianantelo.ucabcovid_19contacttracing.DataClasses.Application
import com.christianantelo.ucabcovid_19contacttracing.DataClasses.Application.Companion.pref
import com.christianantelo.ucabcovid_19contacttracing.R
import com.christianantelo.ucabcovid_19contacttracing.Servicios.ContactTracingService
import com.christianantelo.ucabcovid_19contacttracing.ui.MainActivity
import com.christianantelo.ucabcovid_19contacttracing.ui.MainActivity.Companion.estadoCuarentena

class AlarmReciver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        pref.saveCuarentenaState(false)
        pref.saveContactTracingState(true)
        estadoCuarentena.setValue(false)

        fun getMainActivityPendingIntent() = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).also {
                it.action = ACTION_RESUME_SERVICE_DESPUES_DE_CUARENTENA
            },
            0
        )



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            // Create the NotificationChannel
            val name = "Alarma"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val mChannel = NotificationChannel("AlarmId", name, importance)
            val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }

        val notificationBuilder =
            NotificationCompat.Builder(context!!, "AlarmId")
                .setAutoCancel(false)
                .setSmallIcon(R.drawable.ucovid_icon)
                .setContentText("UCAB Contact Tracing")
                .setContentText("A finalizado su periodo de cuarentena para activar el seguimiento de contactos cercanos haga click en la notificación")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(getMainActivityPendingIntent())

        // Get the Notification manager service
        val am = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        am.notify(NOTIFICATION_ID_NOTIFICACION_FINALIZA_CUARNTENA,
            notificationBuilder.build())


    }



}