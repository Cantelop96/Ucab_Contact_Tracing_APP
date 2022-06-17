package com.christianantelo.ucabcovid_19contacttracing.ui

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
import com.christianantelo.ucabcovid_19contacttracing.DataClasses.ContactTracing
import com.christianantelo.ucabcovid_19contacttracing.R
import com.christianantelo.ucabcovid_19contacttracing.Servicios.ContactTracingService
import com.christianantelo.ucabcovid_19contacttracing.storage.ContactTracingDatabase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


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

        sendCommandToService(ACTION_START_OR_RESUME_SERVICE)

    }

    override fun onResume() {
        super.onResume()
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

    // Permisos
    private val isLocationPermissionGranted
        get() = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)

    private val isBackgroundLocationPermissionGranted
        get() = hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)

    private fun Context.hasPermission(permissionType: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permissionType) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun promptEnableBluetooth() {
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, ENABLE_BLUETOOTH_REQUEST_CODE)
        }
    }

    private fun requestLocationPermission() {
        if (isLocationPermissionGranted && isBackgroundLocationPermissionGranted) {
            seTienePermiso.postValue(true)
            return
        }
        if (!isLocationPermissionGranted) {
            requestPermission(
                Manifest.permission.ACCESS_FINE_LOCATION,
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
        if (!isBackgroundLocationPermissionGranted) {
            requestPermission(
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.firstOrNull() == PackageManager.PERMISSION_DENIED) {
                    requestLocationPermission()
                }
            }
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.firstOrNull() == PackageManager.PERMISSION_DENIED) {
                    requestLocationPermission()
                }
            }
        }
    }

    private fun Activity.requestPermission(permission: String, requestCode: Int) {
        ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
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
    /*fun setFinalizarCuarentenaAlarm(){

        val dateTime: Date = Calendar.getInstance().time
        var alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this,AlarmReciver::class.java)
        var pendingIntent = PendingIntent.getBroadcast(this,0,intent,0)
        var calendar = Calendar.getInstance()

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,calendar.timeInMillis
        )


    }*/


}

