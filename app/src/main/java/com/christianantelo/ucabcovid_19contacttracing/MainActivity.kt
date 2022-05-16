package com.christianantelo.ucabcovid_19contacttracing

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.christianantelo.ucabcovid_19contacttracing.Constantes.Constantes.My_UUID
import com.christianantelo.ucabcovid_19contacttracing.DataClasses.ContactTracing
import com.christianantelo.ucabcovid_19contacttracing.Servicios.ContactTracingService
import com.christianantelo.ucabcovid_19contacttracing.storage.ContactTracingDatabase
import kotlinx.coroutines.launch
import java.util.*
import kotlin.random.Random

private const val ENABLE_BLUETOOTH_REQUEST_CODE = 1
private const val LOCATION_PERMISSION_REQUEST_CODE = 2
private const val ADVERTISE_MODE_BALANCED = 1

class MainActivity : AppCompatActivity() {

    val dateTime: Date = Calendar.getInstance().time

    val contactDate: Long = dateTime.time

    private val db by lazy { ContactTracingDatabase.invoke(this).getContactTracingDao()}

    private val scan = object : Runnable{
        override fun run() {
            startBleScan()
            handler.postDelayed(this,60000) //Todo(Devolver valor a 150000 cuando termine el test)
        }
    }

    fun getRandomList(random: Random): List<Long> =
        List(300) { random.nextLong() }

    private fun numberToByteArray(data: Number, size: Int = 4): ByteArray =
        ByteArray(size) { i -> (data.toLong() shr (i * 8)).toByte() }

    var serviceData = numberToByteArray(1321456789456)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (bluetoothAdapter == null) {
            Toast.makeText(
                this,
                "Este equipo no soporta el uso de Bluetooth, lo cual es indispensable para el funcionamiento de la aplicacion.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        if (bluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, ENABLE_BLUETOOTH_REQUEST_CODE)
        }
        startAdvertising(advertiseSettings, advertiseData, advertiseCallback)
        //sendCommandToService(ACTION_START_OR_RESUME_SERVICE)


    }
    override fun onResume() {
        super.onResume()
        if (!bluetoothAdapter.isEnabled) {
            promptEnableBluetooth()
        }
        handler.post (scan)
        Log.i("Results", "Results lista A: $Bluetooth_Devices_A Lista B: $Bluetooth_Devices_B")
    }

    private fun  sendCommandToService(action: String) =
        Intent(this, ContactTracingService::class.java).also {
            it.action = action
            this.startService(it)
        }

    // Permisos

    val isLocationPermissionGranted
        get() = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)

    fun Context.hasPermission(permissionType: String): Boolean {
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
        if (isLocationPermissionGranted) {
            return
        }
        requestPermission(
            Manifest.permission.ACCESS_FINE_LOCATION,
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.firstOrNull() == PackageManager.PERMISSION_DENIED) {
                    requestLocationPermission()
                } else {
                    startBleScan()
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
                if (bluetoothAdapter!!.isEnabled) {
                    Toast.makeText(this, "Se a activado el Bluetooth.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "No se ha activado el Bluetooth", Toast.LENGTH_SHORT)
                        .show()
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(
                    this,
                    "Se ha cancelado la activacion del Bluetooth",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // Bluetooth Configuration

    private val bleScanner by lazy { bluetoothAdapter.bluetoothLeScanner }
    private val bleAdvertiser by lazy { bluetoothAdapter.bluetoothLeAdvertiser }
    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    // Bluetooth Advertiser

    private val advertiseSettings =
        AdvertiseSettings.Builder()
            .setAdvertiseMode(1)
            .setTxPowerLevel(1)
            .build()

    private val advertiseData = AdvertiseData.Builder()
        .setIncludeTxPowerLevel(true)
        .addServiceData(ParcelUuid(My_UUID), serviceData)
        .build()

    private fun startAdvertising(
        settings: AdvertiseSettings,
        advertiseData: AdvertiseData,
        callback: AdvertiseCallback
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


    // Bluetooth Scan

    private val scanResults = mutableListOf<ScanResult>()
    private val Bluetooth_Devices = mutableListOf<ContactTracing>()
    private var Bluetooth_Devices_A = mutableListOf<String>()
    private var Bluetooth_Devices_B = mutableListOf<String>()
    private var Bluetooth_Devices_C = mutableListOf<String>()
    private var Bluetooth_Devices_D = mutableListOf<String>()
    var current_list = "A"
    var firstscan = true
    private var scanning = false
    private val handler = Handler(Looper.getMainLooper())
    private val SCAN_PERIOD: Long = 10000 //define tiempo de scan 10 seg


    private val filter = ScanFilter.Builder()
        .setServiceUuid(ParcelUuid(My_UUID))
        .build()


    private val devfilters: MutableList<ScanFilter> = ArrayList()


    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()

    private fun startBleScan() {
        devfilters.add(filter)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isLocationPermissionGranted) {
            requestLocationPermission()
        } else {
            bleScanner.startScan(null, scanSettings, scanCallback)
            if (!scanning) { // Stops scanning after a pre-defined scan period.
                handler.postDelayed({
                    scanning = false
                    Log.i("Device", "termino el Scan")
                    bleScanner.stopScan(scanCallback)
                    scanResults.clear()
                    if (current_list == "A"){
                        current_list = "B"
                        if (!firstscan){
                            Bluetooth_Devices_C = Bluetooth_Devices_A.filter{Bluetooth_Devices_B.contains(it)} as MutableList<String>
                            Bluetooth_Devices_D.addAll( Bluetooth_Devices_C.filterNot {Bluetooth_Devices_D.contains(it)} as MutableList<String>)
                            Log.i("Results", "Results\nlista A: $Bluetooth_Devices_A\nLista B: $Bluetooth_Devices_B\nLista C: $Bluetooth_Devices_C\nLista D: $Bluetooth_Devices_D")
                            Bluetooth_Devices_B.clear()
                        }
                    }
                    else{
                        current_list = "A"
                        if (firstscan){
                            Bluetooth_Devices_D = Bluetooth_Devices_A.filter{(Bluetooth_Devices_B).contains(it)} as MutableList<String>
                            Bluetooth_Devices_C = Bluetooth_Devices_D
                            firstscan = false
                            Log.i("Results", "Results\nlista A: $Bluetooth_Devices_A\nLista B: $Bluetooth_Devices_B\nLista C: $Bluetooth_Devices_C\nLista D: $Bluetooth_Devices_D")
                            Bluetooth_Devices_A.clear()
                        }
                        else{
                            Bluetooth_Devices_C = Bluetooth_Devices_A.filter{Bluetooth_Devices_B.contains(it)} as MutableList<String>
                            Bluetooth_Devices_D.addAll( Bluetooth_Devices_C.filterNot {Bluetooth_Devices_D.contains(it)} as MutableList<String>)
                            Log.i("Results", "Results\nlista A: $Bluetooth_Devices_A\nLista B: $Bluetooth_Devices_B\nLista C: $Bluetooth_Devices_C\nLista D: $Bluetooth_Devices_D")
                            Bluetooth_Devices_A.clear()
                        }

                    }
                    for(Device in Bluetooth_Devices){
                        if(Bluetooth_Devices_D.contains(Device.address)){
                            insertContact(Device)
                            Log.i("DB", "Añade $Device a la DB")

                        }
                        Log.i("DB", "Deentro del For fuera de if")
                    }

                }, SCAN_PERIOD)
                scanning = true
                Log.i("Device", "empezo el Scan")
                bleScanner.startScan(null, scanSettings, scanCallback)
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
            }
            else {
                with(result.device) {
                    Log.i(
                        "ScanCallback",
                        "Found BLE device! Name: ${name ?: "Unnamed"}, address: $address, RSSI: $rssi"
                    )
                }
                scanResults.add(result)

                if (current_list == "A"){
                    Bluetooth_Devices_A.add(
                        result.device.address)

                }
                else{
                    Bluetooth_Devices_B.add(
                        result.device.address)
                }
                Bluetooth_Devices.add(
                    ContactTracing(
                        result.device.address,
                        result.rssi,
                        contactDate
                    )
                )
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("ScanCallback", "onScanFailed: code $errorCode")
        }
    }

    // Database fun

    fun insertContact(contactTracing: ContactTracing) =
        lifecycleScope.launch {
            db.insertContact(contactTracing)
        }


    fun  deleteContact(contactTracing: ContactTracing) =
        db.deleteContact(contactTracing)

    fun getCloseContacts() = db.getAllContactSortByDate()

    fun deleteOldContacts() = db.borrarContactsMasde14Dias()

    fun deleteAllInfo() = db.clear()



}

