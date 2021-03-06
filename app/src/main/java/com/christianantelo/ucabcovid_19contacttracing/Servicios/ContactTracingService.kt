package com.christianantelo.ucabcovid_19contacttracing.Servicios

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.christianantelo.ucabcovid_19contacttracing.Constantes.Constantes
import com.christianantelo.ucabcovid_19contacttracing.Constantes.Constantes.ACTION_RESUME_SERVICE_DESPUES_DE_CUARENTENA
import com.christianantelo.ucabcovid_19contacttracing.Constantes.Constantes.ACTION_START_OR_RESUME_SERVICE
import com.christianantelo.ucabcovid_19contacttracing.Constantes.Constantes.ACTION_STOP_CONTACT_TRACING_SERVIVE
import com.christianantelo.ucabcovid_19contacttracing.Constantes.Constantes.My_UUID
import com.christianantelo.ucabcovid_19contacttracing.Constantes.Constantes.NOTIFICATION_CHANNEL_ID_NOTIFICACION
import com.christianantelo.ucabcovid_19contacttracing.Constantes.Constantes.NOTIFICATION_CHANNEL_ID_SERVICIO
import com.christianantelo.ucabcovid_19contacttracing.Constantes.Constantes.NOTIFICATION_CHANNEL_NAME_NOTIFICACION
import com.christianantelo.ucabcovid_19contacttracing.Constantes.Constantes.NOTIFICATION_CHANNEL_NAME_SERVICIO
import com.christianantelo.ucabcovid_19contacttracing.Constantes.Constantes.NOTIFICATION_ID_NOTIFICACION
import com.christianantelo.ucabcovid_19contacttracing.Constantes.Constantes.NOTIFICATION_ID_NOTIFICACION_BLUETOOTH_DISABLE
import com.christianantelo.ucabcovid_19contacttracing.Constantes.Constantes.NOTIFICATION_ID_SERVICIO
import com.christianantelo.ucabcovid_19contacttracing.DataClasses.Application.Companion.pref
import com.christianantelo.ucabcovid_19contacttracing.DataClasses.ContactTracing
import com.christianantelo.ucabcovid_19contacttracing.R
import com.christianantelo.ucabcovid_19contacttracing.storage.ContactTracingDatabase
import com.christianantelo.ucabcovid_19contacttracing.ui.MainActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.random.Random

var isFirstStart = true

class ContactTracingService : LifecycleService() {

    //Key List Generator
    private val privateKey = pref.getKey()
    val random = Random(privateKey)
    fun getRandomList(random: Random): List<Int> =
        List(300) { random.nextInt() }

    val keyList = getRandomList(random)
    var currentKey = keyList.random()

    //Inicializamos las bases de datos
    val db by lazy { ContactTracingDatabase.invoke(this).getContactTracingDao() }
    val fsdb = Firebase.firestore
    var contactosCercanos: MutableList<ContactTracing> = mutableListOf()
    var contactosInfectados: MutableList<ContactTracing> = mutableListOf()

    companion object {
        val isInfected = MutableLiveData<Boolean>()
    }

    private fun initialValues() {
        isInfected.postValue(false)
    }

    override fun onCreate() {
        super.onCreate()
        initialValues()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    pref.saveContactTracingState(true)
                    if (isFirstStart) {
                        handler.postDelayed({
                            startForegroundService()
                        }, 1000)
                        isFirstStart = false
                    }
                }
                ACTION_STOP_CONTACT_TRACING_SERVIVE -> {
                    killService()
                }
                ACTION_RESUME_SERVICE_DESPUES_DE_CUARENTENA -> {

                    //sendNotificatioInfeccion(NOTIFICATION_ID_NOTIFICACION_FINALIZA_CUARNTENA)
                    pref.saveContactTracingState(true)

                    if (isFirstStart) {
                        handler.postDelayed({
                            startForegroundService()
                        }, 1000)
                        isFirstStart = false
                    }
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun killService() {
        handler.removeCallbacksAndMessages(null)
        isFirstStart = true
        stopContactTracing()
        stopForeground(true)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("service", "en el on Destroy del servicio")
        isFirstStart = true
    }


    private fun startForegroundService() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID_SERVICIO)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ucovid_icon)
            .setContentText("UCAB Contact Tracing")
            .setContentText("El seguimiento de contactos cercanos esta habilitado.")
            .setContentIntent(getMainActivityPendingIntent())

        startForeground(NOTIFICATION_ID_SERVICIO, notificationBuilder.build())

        if (pref.getContactTracingState()) {
            handler.post(scan)
            handler.post(changeKey)
            handler.post(check)
        }
    }

    private fun bluetoothcheck(){
        Log.i("check","${bluetoothAdapter.state}, is enable ${bluetoothAdapter.isEnabled}")
        if (bluetoothAdapter.state == BluetoothAdapter.STATE_OFF) {
            sendNotificatioInfeccion(NOTIFICATION_ID_NOTIFICACION_BLUETOOTH_DISABLE)
            killService()
            handler.removeCallbacksAndMessages(null)
            pref.saveContactTracingState(false)
        }
    }

    internal fun stopContactTracing() {
        bleAdvertiser.stopAdvertising(advertiseCallback)
        bleScanner.stopScan(scanCallback)
    }

    private val check = object : Runnable {
        override fun run() {
            bluetoothcheck()
            handler.postDelayed(this, 10000)
        }
    }

    //Funcion que inicia un nuevo scan cas 5 min
    private val scan = object : Runnable {
        override fun run() {
            startBleScan()
            handler.postDelayed(this, 300000) //Todo(Devolver valor a 300000 cuando termine el test)
        }
    }

    // Cambia la Key del service data cada hora
    private val changeKey = object : Runnable {
        override fun run() {
            bleAdvertiser.stopAdvertising(advertiseCallback)
            infectionCheck()
            Log.i("Cambio de public key", "Llave antes del cambio: $currentKey")
            currentKey = keyList.random()
            Log.i("Cambio de public key", "Llave despues del cambio: $currentKey")
            bluetoothAdapter.name = "$currentKey"
            startAdvertising(advertiseSettings, advertiseData, advertiseCallback)
            handler.postDelayed(this,
                3600000) //Todo(Cambiar a 3600000 cuando terminen las pruebas)

        }
    }

    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainActivity::class.java),
        FLAG_UPDATE_CURRENT

    )

    // Database fun
    private fun insertContact(contactTracing: ContactTracing) =
        lifecycleScope.launch {
            with(Dispatchers.IO) { db.insertContact(contactTracing) }
        }

    private fun deleteContacts(contactTracing: ContactTracing) =
        lifecycleScope.launch {
            with(Dispatchers.IO) { db.deleteContact(contactTracing) }
        }

    private fun deleteOldContacts() =
        lifecycleScope.launch {
            with(Dispatchers.IO) { db.borrarContactsMasde14Dias() }
        }

    private fun getCloseContacts() =
        lifecycleScope.launch(Dispatchers.IO) {
            contactosCercanos = db.getAllContactSortByDate()
        }

    private fun agregarContactoALaBasedeDatos() {
        for (Device in Bluetooth_Devices) {
            Log.i(
                "Test Distancia Callback",
                "Dispocitivo Key publica ${Device.serviceData},\nRSSI: ${Device.RSSI} \nPower Level:${Device.txPowerLevel} \nDistancia ${Device.distance}"
            )
            if (Device.distance < 2) {
                if (Bluetooth_Devices_D.contains(Device.serviceData)) {
                    insertContact(Device)
                    Log.i("DB", "A??ade $Device a la DB")
                }
            }
        }
        Bluetooth_Devices.clear()
    }

    // Bluetooth Configuration
    private val bleScanner by lazy { bluetoothAdapter.bluetoothLeScanner }
    private val bleAdvertiser by lazy { bluetoothAdapter.bluetoothLeAdvertiser }
    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    // Bluetooth Scan
    private val scanResults = mutableListOf<ScanResult>()
    private val Bluetooth_Devices = mutableListOf<ContactTracing>()
    private var Bluetooth_Devices_A = mutableListOf<Int>()
    private var Bluetooth_Devices_B = mutableListOf<Int>()
    private var Bluetooth_Devices_C = mutableListOf<Int>()
    private var Bluetooth_Devices_D = mutableListOf<Int>()
    var current_list = "A"
    var firstscan = true
    private var scanning = false
    private val handler = Handler(Looper.getMainLooper())
    private val SCAN_PERIOD: Long = 10000 //define tiempo de scan 10 seg todo = cambiar a 10000


    var filter = ScanFilter.Builder()
        .setServiceUuid(ParcelUuid.fromString(My_UUID))
        .build()

    private val devfilters: List<ScanFilter> = arrayListOf(filter)
    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()

    private fun startBleScan() {
        bluetoothcheck()
        if (!scanning) { // Stops scanning after a pre-defined scan period.
            handler.postDelayed({
                scanning = false
                Log.i("Test Distancia Callback", "termino el Scan")
                bleScanner.stopScan(scanCallback)
                scanResults.clear()
                if (current_list == "A") {
                    current_list = "B"
                    if (!firstscan) {
                        Bluetooth_Devices_C =
                            Bluetooth_Devices_A.filter { Bluetooth_Devices_B.contains(it) } as MutableList<Int>
                        Bluetooth_Devices_D.addAll(Bluetooth_Devices_C.filterNot {
                            Bluetooth_Devices_D.contains(it)
                        } as MutableList<Int>)
                        Log.i("ScanCallback",
                            "Results\nlista A: $Bluetooth_Devices_A\nLista B: $Bluetooth_Devices_B\nLista C: $Bluetooth_Devices_C\nLista D: $Bluetooth_Devices_D")
                        Bluetooth_Devices_B.clear()
                    }
                } else {
                    current_list = "A"
                    if (firstscan) {
                        Bluetooth_Devices_D =
                            Bluetooth_Devices_A.filter { (Bluetooth_Devices_B).contains(it) } as MutableList<Int>
                        Bluetooth_Devices_C = Bluetooth_Devices_D
                        firstscan = false
                        Bluetooth_Devices_A.clear()
                    } else {
                        Bluetooth_Devices_C =
                            Bluetooth_Devices_A.filter { Bluetooth_Devices_B.contains(it) } as MutableList<Int>
                        Bluetooth_Devices_D.addAll(Bluetooth_Devices_C.filterNot {
                            Bluetooth_Devices_D.contains(it)
                        } as MutableList<Int>)
                        Bluetooth_Devices_A.clear()
                    }

                }
                agregarContactoALaBasedeDatos()
            }, SCAN_PERIOD)
            scanning = true
            Log.i("Test Distancia Callback", "empezo el Scan")
            bleScanner.stopScan(scanCallback)
            bleScanner.startScan(devfilters, scanSettings, scanCallback)
        } else {
            scanning = false
            bleScanner.stopScan(scanCallback)
        }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val rssi = result.rssi
            val indexQuery = scanResults.indexOfFirst { it.device.address == result.device.address }
            if (indexQuery != -1) { // A scan result already exists with the same address
                scanResults[indexQuery] = result
            } else {
                with(result.device) {
                    Log.i(
                        "ScanCallback",
                        "Found BLE device! Name: ${name ?: "Unnamed"}, \naddress: $address, \nRSSI: $rssi, TXPower: ${result.scanRecord!!.txPowerLevel}"
                    )
                }
                scanResults.add(result)

                if (current_list == "A") {
                    Bluetooth_Devices_A.add(
                        result.device.name.toInt()

                    )

                } else {
                    Bluetooth_Devices_B.add(
                        result.device.name.toInt()
                    )
                }
                //Get Contact Date
                val dateTime: Date = Calendar.getInstance().time
                val contactDate: Long = dateTime.time
                Bluetooth_Devices.add(
                    ContactTracing(
                        result.device.address,
                        result.rssi,
                        result.scanRecord!!.txPowerLevel,
                        result.device.name.toInt(),
                        contactDate
                    )
                )
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("ScanCallback", "onScanFailed: code $errorCode")
        }
    }

    // Bluetooth Advertiser
    private val advertiseSettings =
        AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
            .setConnectable(false)
            .build()

    private val advertiseData = AdvertiseData.Builder()
        .setIncludeDeviceName(true)
        .setIncludeTxPowerLevel(true)
        .addServiceUuid(ParcelUuid.fromString(My_UUID))

        .build()

    private fun startAdvertising(
        settings: AdvertiseSettings,
        advertiseData: AdvertiseData,
        callback: AdvertiseCallback,
    ) {
        bluetoothAdapter.name = "$currentKey"
        bleAdvertiser.startAdvertising(settings, advertiseData, callback)
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            super.onStartSuccess(settingsInEffect)
            if (settingsInEffect != null) {
                Log.i(
                    "AdverticeCallback",
                    "Se esta enviando la info $advertiseSettings con tx ${advertiseSettings.txPowerLevel} y UUID:${advertiseData.serviceUuids} /n Service Data: ${advertiseData.serviceData}\n ${currentKey}"
                )
            } else {
                Log.i(
                    "AdverticeCallback",
                    "Se esta enviando la info $advertiseData"
                )
            }
        }

        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            Log.i(
                "AdverticeCallback",
                "Fallo con codigo: $errorCode"
            )
        }
    }


    //Revisar si se ha tenido contacto con algun infectado
    var publicKeyInfectadosCompleta: MutableList<Int> = mutableListOf<Int>()

    fun infectionCheck() {
        deleteOldContacts()
        getCloseContacts()
        fsdb.collection("Infectados")
            .get()
            .addOnSuccessListener { result ->
                for (infectado in result) {
                    Log.d("Descarga Infectados", "${infectado.id} => ${infectado.data}")
                    val infectadokey = infectado.get("key").toString().toInt()
                    val publicKeyInfectados = getRandomList(Random(infectadokey))
                    for (publickey in publicKeyInfectados) {
                        publicKeyInfectadosCompleta.add(publickey)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w("Descarga Infectados", "Error getting documents.", exception)
            }
        var infectado = false
        for (contacto in contactosCercanos) {
            if (publicKeyInfectadosCompleta.contains(contacto.serviceData)) {
                contactosInfectados.add(contacto)
                isInfected.postValue(true)
                infectado = true
            }
        }
        if(infectado){
            for (contacto in contactosInfectados){
                deleteContacts(contacto)
            }
            sendNotificatioInfeccion(NOTIFICATION_ID_NOTIFICACION)
            contactosInfectados.clear()
        }
        if (isInfected.value == false) {
            publicKeyInfectadosCompleta.clear()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel =
            NotificationChannel(
                NOTIFICATION_CHANNEL_ID_SERVICIO,
                NOTIFICATION_CHANNEL_NAME_SERVICIO,
                IMPORTANCE_LOW
            )
        notificationManager.createNotificationChannel(channel)
    }

    //Configuracion Notificacion Infeccion
    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannelNotificacion(notificationManager: NotificationManager) {
        val channel =
            NotificationChannel(
                NOTIFICATION_CHANNEL_ID_NOTIFICACION,
                NOTIFICATION_CHANNEL_NAME_NOTIFICACION,
                NotificationManager.IMPORTANCE_HIGH
            )
        notificationManager.createNotificationChannel(channel)
    }

    fun sendNotificatioInfeccion(notificationIDs: Int) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannelNotificacion(notificationManager)
        }
        if (NOTIFICATION_ID_NOTIFICACION == notificationIDs) {
            val notificationBuilder =
                NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID_NOTIFICACION)
                    .setAutoCancel(false)
                    .setSmallIcon(R.drawable.ucovid_icon)
                    .setContentText("UCAB Contact Tracing")
                    .setContentText("Una persona con la que estuvo en contacto resulto positivo para COVID-19")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(getMainActivityPendingIntentNotificacion())
            notificationManager.notify(NOTIFICATION_ID_NOTIFICACION,
                notificationBuilder.build())
        } else {
            val notificationBuilder =
                NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID_NOTIFICACION)
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.ucovid_icon)
                    .setContentText("UCAB Contact Tracing")
                    .setContentText("Se ha desactivado el Bluetooth, para el seguimiento de contactos cercano es necesario que el Bluetooth este activo en todo momento.")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(getMainActivityPendingIntent())
            notificationManager.notify(NOTIFICATION_ID_NOTIFICACION_BLUETOOTH_DISABLE,
                notificationBuilder.build())
        }
    }

    fun getMainActivityPendingIntentNotificacion() = PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainActivity::class.java).also {
            it.action = Constantes.ACTION_NOTIFICACION_INFECCION
        },
        0
    )


}