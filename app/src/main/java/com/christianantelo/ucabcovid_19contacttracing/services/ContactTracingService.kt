package com.christianantelo.ucabcovid_19contacttracing.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.christianantelo.ucabcovid_19contacttracing.DataClasses.Bluetooth_Device
import com.christianantelo.ucabcovid_19contacttracing.MainActivity
import com.christianantelo.ucabcovid_19contacttracing.R
import com.christianantelo.ucabcovid_19contacttracing.constants.Constantes
import com.christianantelo.ucabcovid_19contacttracing.constants.Constantes.ACTION_PAUSE_SERVICE
import com.christianantelo.ucabcovid_19contacttracing.constants.Constantes.ACTION_START_OR_RESUME_SERVICE
import com.christianantelo.ucabcovid_19contacttracing.constants.Constantes.ACTION_STOP_SERVICE
import com.christianantelo.ucabcovid_19contacttracing.constants.Constantes.NOTIFICATION_CHANNEL_ID
import com.christianantelo.ucabcovid_19contacttracing.constants.Constantes.NOTIFICATION_CHANNEL_NAME
import com.christianantelo.ucabcovid_19contacttracing.constants.Constantes.NOTIFICATION_ID
import com.christianantelo.ucabcovid_19contacttracing.contactDate
import java.util.ArrayList


class ContactTracingService : Service() {

    var isFirstStart = true


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    if (isFirstStart){
                        startForegroundService()
                        isFirstStart = false
                    }
                }
                ACTION_PAUSE_SERVICE -> {
                }
                ACTION_STOP_SERVICE -> {
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }


    override fun onBind(intent: Intent?): IBinder? = null


    private fun startForegroundService(){
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
            as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createNotificationChannel(notificationManager)
        }

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentText("UCAB Contact Tracing")
            .setContentText("El seguimiento de contactos cercanos esta habilitado")
            .setContentIntent(getMainActivityPendingIntent())

        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainActivity::class.java),
        FLAG_UPDATE_CURRENT

    )

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel =
            NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                IMPORTANCE_LOW
            )
        notificationManager.createNotificationChannel(channel)
    }


}