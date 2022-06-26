package com.christianantelo.ucabcovid_19contacttracing.ui

import android.Manifest
import android.app.*
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.input.key.Key.Companion.I
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.christianantelo.ucabcovid_19contacttracing.Constantes.Constantes.ACTION_NOTIFICACION_INFECCION
import com.christianantelo.ucabcovid_19contacttracing.Constantes.Constantes.ACTION_START_OR_RESUME_SERVICE
import com.christianantelo.ucabcovid_19contacttracing.Constantes.Constantes.ACTION_STOP_CONTACT_TRACING_SERVIVE
import com.christianantelo.ucabcovid_19contacttracing.Constantes.Constantes.BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE
import com.christianantelo.ucabcovid_19contacttracing.Constantes.Constantes.ENABLE_BLUETOOTH_REQUEST_CODE
import com.christianantelo.ucabcovid_19contacttracing.Constantes.Constantes.LOCATION_PERMISSION_REQUEST_CODE
import com.christianantelo.ucabcovid_19contacttracing.DataClasses.Application
import com.christianantelo.ucabcovid_19contacttracing.DataClasses.ContactTracing
import com.christianantelo.ucabcovid_19contacttracing.KeyGenerator_SplashScreen.FirstTimeKeyGen_Activity.Companion.isBackgroundPermissionGranted
import com.christianantelo.ucabcovid_19contacttracing.KeyGenerator_SplashScreen.FirstTimeKeyGen_Activity.Companion.isLocationPermissionGranted
import com.christianantelo.ucabcovid_19contacttracing.R
import com.christianantelo.ucabcovid_19contacttracing.Servicios.ContactTracingService
import com.christianantelo.ucabcovid_19contacttracing.alarms.AlarmReciver
import com.christianantelo.ucabcovid_19contacttracing.storage.ContactTracingDatabase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*


class MainActivity : AppCompatActivity() {

    companion object {
        var seTienePermiso = MutableLiveData<Boolean>()
        var bluetoothActivado = MutableLiveData<Boolean>()

    }

    // Initialize Room Database
    private val db by lazy { ContactTracingDatabase.invoke(this).getContactTracingDao() }

    //Detiene todas las funciones del Contact Tracing
    internal fun stopContactTracing() {
        sendCommandToService(ACTION_STOP_CONTACT_TRACING_SERVIVE)
    }

    internal fun startContactTracing() {
        sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        navigateToNotificaciondeInfeccion(intent)

        if (bluetoothAdapter == null) {
            Toast.makeText(
                this,
                "Este equipo no soporta el uso de Bluetooth, lo cual es indispensable para el funcionamiento de la aplicacion.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        if (!bluetoothAdapter.isEnabled) {
            bluetoothActivado.postValue(false)
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, ENABLE_BLUETOOTH_REQUEST_CODE)
        }
        bluetoothActivado.postValue(true)

        Log.i("Callback 2.0",
            "isLocationPermissionGranted ${isLocationPermissionGranted}, isBackgroundPermissionGranted ${isBackgroundPermissionGranted} ")

        if (isLocationPermissionGranted && isBackgroundPermissionGranted) {
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }
    override fun onResume() {
        super.onResume()
        Log.i("Callback 2.0",
            "isLocationPermissionGranted $isLocationPermissionGranted, isBackgroundPermissionGranted $isBackgroundPermissionGranted ")
        if (!bluetoothAdapter.isEnabled) {
            bluetoothActivado.postValue(false)
            promptEnableBluetooth()
        }


    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateToNotificaciondeInfeccion(intent)
    }


    internal fun sendCommandToService(action: String) =
        Intent(this, ContactTracingService::class.java).also {
            it.action = action
            this.startService(it)
        }

    private fun promptEnableBluetooth() {
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, ENABLE_BLUETOOTH_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ENABLE_BLUETOOTH_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                if (bluetoothAdapter.isEnabled) {
                    bluetoothActivado.postValue(true)
                    Toast.makeText(this, "Se a activado el Bluetooth.", Toast.LENGTH_SHORT).show()
                } else {
                    bluetoothActivado.postValue(false)
                    Toast.makeText(this, "No se ha activado el Bluetooth", Toast.LENGTH_SHORT)
                        .show()
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                bluetoothActivado.postValue(false)
                Toast.makeText(
                    this,
                    "Se ha cancelado la activacion del Bluetooth",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // Bluetooth Configuration
    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private fun navigateToNotificaciondeInfeccion(intent: Intent?) {
        if (intent?.action == ACTION_NOTIFICACION_INFECCION) {
            Nav_Host.findNavController().navigate(R.id.action_notificacion_to_reprte_infeccion)
        }
    }

    internal fun deleteAllInfo() =
        lifecycleScope.launch {
            with(Dispatchers.IO) { db.clear() }
        }


    var contactosCercanos: MutableList<ContactTracing> = mutableListOf()

    fun getCloseContacts() =
        lifecycleScope.launch(Dispatchers.IO) {
            contactosCercanos = db.getAllContactSortByDate()
        }

    //todo = terminar de configurar logica de cuarentena con alarma
    internal fun setFinalizarCuarentenaAlarm(){
:
        var alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReciver::class.java)
        var pendingIntent = PendingIntent.getBroadcast(this,0,intent,0)
        alarmManager.set(
            AlarmManager.RTC_WAKEUP,300000,pendingIntent,
            //todo = devolver a 777600000
        )
        Application.pref.saveCuarentenaState(true)
    }
    internal fun finalizarCuarentenaAlarma(){
        var alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReciver::class.java)
        var pendingIntent = PendingIntent.getBroadcast(this,0,intent,0)
        alarmManager.cancel(pendingIntent)
    }


}

