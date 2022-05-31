package com.christianantelo.ucabcovid_19contacttracing.Servicios

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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.christianantelo.ucabcovid_19contacttracing.Constantes.Constantes
import com.christianantelo.ucabcovid_19contacttracing.Constantes.Constantes.ACTION_PAUSE_SERVICE
import com.christianantelo.ucabcovid_19contacttracing.Constantes.Constantes.ACTION_START_OR_RESUME_SERVICE
import com.christianantelo.ucabcovid_19contacttracing.Constantes.Constantes.ACTION_STOP_CONTACT_TRACING_SERVIVE
import com.christianantelo.ucabcovid_19contacttracing.Constantes.Constantes.NOTIFICATION_CHANNEL_ID_SERVICIO
import com.christianantelo.ucabcovid_19contacttracing.Constantes.Constantes.NOTIFICATION_CHANNEL_NAME_SERVICIO
import com.christianantelo.ucabcovid_19contacttracing.Constantes.Constantes.NOTIFICATION_ID_SERVICIO
import com.christianantelo.ucabcovid_19contacttracing.DataClasses.Application
import com.christianantelo.ucabcovid_19contacttracing.DataClasses.ContactTracing
import com.christianantelo.ucabcovid_19contacttracing.R
import com.christianantelo.ucabcovid_19contacttracing.storage.ContactTracingDatabase
import com.christianantelo.ucabcovid_19contacttracing.ui.MainActivity
import com.christianantelo.ucabcovid_19contacttracing.ui.MainActivity.Companion.bluetoothActivado
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.*
import kotlin.random.Random


class ContactTracingService : LifecycleService() {

    var isFirstStart = true

    //Key List Generator
    private val privateKey = Application.pref.getKey()
    val random = Random(privateKey)
    fun getRandomList(random: Random): List<Int> =
        List(300) { random.nextInt() }

    val keyList = getRandomList(random)
    var currentKey = keyList.random()

    //Inicializamos las bases de datos
    val db by lazy { ContactTracingDatabase.invoke(this).getContactTracingDao() }
    val fsdb = Firebase.firestore
    lateinit var contactosCercanos: MutableList<ContactTracing>

    companion object {
        val isInfected = MutableLiveData<Boolean>()
    }

    private fun initialValues() {
        isInfected.postValue(false)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    if (isFirstStart) {
                        startForegroundService()
                        isFirstStart = false
                    }
                }
                ACTION_PAUSE_SERVICE -> {
                }
                ACTION_STOP_CONTACT_TRACING_SERVIVE -> {
                    stopContactTracing()
                    isFirstStart = true
                    stopSelf()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }



    private fun startForegroundService(){
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createNotificationChannel(notificationManager)
        }

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID_SERVICIO)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentText("UCAB Contact Tracing")
            .setContentText("El seguimiento de contactos cercanos esta habilitado")
            .setContentIntent(getMainActivityPendingIntent())

        startForeground(NOTIFICATION_ID_SERVICIO, notificationBuilder.build())

        if (Application.pref.getContactTracingState()) {
            handler.post(scan)
            handler.post(changeKey)
        }
    }

    internal fun stopContactTracing() {
        bleAdvertiser.stopAdvertising(advertiseCallback)
        bleScanner.stopScan(scanCallback)
    }

    //Funcion que inicia un nuevo scan cas 2,5 min
    private val scan = object : Runnable {
        override fun run() {
            startBleScan()
            handler.postDelayed(this, 60000) //Todo(Devolver valor a 150000 cuando termine el test)
        }
    }

    // Cambia la Key del service data cada hora
    private val changeKey = object : Runnable {
        override fun run() {
            handler.postDelayed(this, 120000) //Todo(Cambiar a 3600000 cuando terminen las pruebas)
            bleAdvertiser.stopAdvertising(advertiseCallback)
            //infectionCheck()
            currentKey = keyList.random()
            startAdvertising(advertiseSettings, advertiseData, advertiseCallback)
        }
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
                NOTIFICATION_CHANNEL_ID_SERVICIO,
                NOTIFICATION_CHANNEL_NAME_SERVICIO,
                IMPORTANCE_LOW
            )
        notificationManager.createNotificationChannel(channel)
    }

    // Database fun
    private fun insertContact(contactTracing: ContactTracing) =
        lifecycleScope.launch {
            with(Dispatchers.IO) { db.insertContact(contactTracing) }
        }

    private fun deleteOldContacts() =
        lifecycleScope.launch {
            with(Dispatchers.IO) { db.borrarContactsMasde14Dias() }
        }

    private fun getCloseContacts() = db.getAllContactSortByDate()

    internal fun deleteAllInfo() =
        lifecycleScope.launch {
            with(Dispatchers.IO) { db.clear() }
        }


    //convierte el key en un bytearray para poder agregarlo al advertizer
    private fun numberToByteArray(data: Number, size: Int = 4): ByteArray =
        ByteArray(size) { i -> (data.toInt() shr (i * 8)).toByte() }

    var serviceData = numberToByteArray(currentKey)
    fun read4BytesFromBuffer(buffer: ByteArray, offset: Int = 0): Int {
        return (buffer[offset + 3].toInt() shl 24) or
                (buffer[offset + 2].toInt() and 0xff shl 16) or
                (buffer[offset + 1].toInt() and 0xff shl 8) or
                (buffer[offset + 0].toInt() and 0xff)
    }

    private fun agregarContactoALaBasedeDatos() {
        for (Device in Bluetooth_Devices) {
            if (Device.distance < 2) {
                if (Bluetooth_Devices_D.contains(Device.decodedServiceData)) {
                    insertContact(Device)
                    Log.i("DB", "Añade $Device a la DB")
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
    private val SCAN_PERIOD: Long = 10000 //define tiempo de scan 10 seg
    private val filter = ScanFilter.Builder()
        .setServiceUuid(ParcelUuid(Constantes.My_UUID))
        .build()
    private val devfilters: MutableList<ScanFilter> = ArrayList()
    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()

    private fun startBleScan() {
        devfilters.add(filter)
        if (!bluetoothActivado.value!!) {
            getMainActivityPendingIntent()//todo = revisar funcionalidad
        } else {
            bleScanner.startScan(devfilters, scanSettings, scanCallback)
            if (!scanning) { // Stops scanning after a pre-defined scan period.
                handler.postDelayed({
                    scanning = false
                    Log.i("Device", "termino el Scan")
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
                            Log.i("Results",
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
                Log.i("Device", "empezo el Scan")
                bleScanner.startScan(devfilters, scanSettings, scanCallback)
            } else {
                scanning = false
                bleScanner.stopScan(scanCallback)
            }
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
                        "Found BLE device! Name: ${name ?: "Unnamed"}, address: $address, RSSI: $rssi"
                    )
                }
                scanResults.add(result)

                if (current_list == "A") {
                    Bluetooth_Devices_A.add(
                        read4BytesFromBuffer(result.scanRecord!!.getServiceData(ParcelUuid(
                            Constantes.My_UUID))!!))

                } else {
                    Bluetooth_Devices_B.add(
                        read4BytesFromBuffer(result.scanRecord!!.getServiceData(ParcelUuid(
                            Constantes.My_UUID))!!))
                }
                //Get Contact Date
                val dateTime: Date = Calendar.getInstance().time
                val contactDate: Long = dateTime.time
                Bluetooth_Devices.add(
                    ContactTracing(
                        result.device.address,
                        result.rssi,
                        result.scanRecord!!.txPowerLevel,
                        result.scanRecord!!.getServiceData(ParcelUuid(Constantes.My_UUID))!!,
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
            .setAdvertiseMode(1)
            .setTxPowerLevel(1)
            .build()

    private val advertiseData = AdvertiseData.Builder()
        .setIncludeTxPowerLevel(true)
        .addServiceData(ParcelUuid(Constantes.My_UUID), serviceData)
        .build()

    private fun startAdvertising(
        settings: AdvertiseSettings,
        advertiseData: AdvertiseData,
        callback: AdvertiseCallback,
    ) {
        bleAdvertiser.startAdvertising(settings, advertiseData, callback)
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            super.onStartSuccess(settingsInEffect)
            if (settingsInEffect != null) {
                Log.i(
                    "AdverticeCallback",
                    "Se esta enviando la info $advertiseSettings con tx ${advertiseSettings.txPowerLevel}"
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


    //Revisar si se ha tenido contacto con alguninfectado
    lateinit var publicKeyInfectadosCompleta: MutableList<Int>
    fun infectionCheck() {
        runBlocking {
            deleteOldContacts().join()
            Log.i("Corutinas", "Dentro de la primera")
        }
        getCloseContacts()
        fsdb.collection("Infectados")
            .get()
            .addOnSuccessListener { result ->
                for (infectado in result) {
                    Log.d("Descarga Infectados", "${infectado.id} => ${infectado.data}")
                    var infectadokey = infectado.get("key").toString().toInt()
                    var publicKeyInfectados = getRandomList(Random(infectadokey))
                    for (publickey in publicKeyInfectados) {
                        publicKeyInfectadosCompleta.add(publickey)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w("Descarga Infectados", "Error getting documents.", exception)
            }
        for (contacto in contactosCercanos) {
            if (publicKeyInfectadosCompleta.contains(contacto.decodedServiceData)) {
                sendNotificatioInfeccion()
            } else {
                publicKeyInfectadosCompleta.clear()
            }
        }
    }

    //Configuracion Notificacion Infeccion
    fun sendNotificatioInfeccion() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannelNotificacion(notificationManager)
        }
        val notificationBuilder =
            NotificationCompat.Builder(this, Constantes.NOTIFICATION_CHANNEL_ID_NOTIFICACION)
                .setAutoCancel(false)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentText("UCAB Contact Tracing")
                .setContentText("Uno persona con la que estuvo en contacto resulto positivo para COVID-19")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(getMainActivityPendingIntentNotificacion())
        notificationManager.notify(Constantes.NOTIFICATION_ID_NOTIFICACION,
            notificationBuilder.build())
    }

    fun getMainActivityPendingIntentNotificacion() = PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainActivity::class.java).also {
            it.action = Constantes.ACTION_NOTIFICACION_INFECCION
        },
        0
    )


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannelNotificacion(notificationManager: NotificationManager) {
        val channel =
            NotificationChannel(
                Constantes.NOTIFICATION_CHANNEL_ID_NOTIFICACION,
                Constantes.NOTIFICATION_CHANNEL_NAME_NOTIFICACION,
                NotificationManager.IMPORTANCE_HIGH
            )
        notificationManager.createNotificationChannel(channel)
    }


}