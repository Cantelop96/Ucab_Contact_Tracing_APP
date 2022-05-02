package com.christianantelo.ucabcovid_19contacttracing

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val REQUEST_ENABLE_BT = 1
    lateinit var m_pairedDevices:Set<BluetoothDevice>
    private val bluetoothLeScanner = bluetoothAdapter!!.bluetoothLeScanner
    private var scanning = false
    private val handler = Handler(Looper.getMainLooper())
    private val SCAN_PERIOD: Long = 10000 //define tiempo de scan 10 seg

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(bluetoothAdapter == null){
            Toast.makeText(this, "Este equipo no soporta el uso de Bluetooth, lo cual es indispensable para el funcionamiento de la aplicacion.", Toast.LENGTH_SHORT).show()
            return
        }
        if (bluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }
        Log.i("Device","antes de llamas a la funcion")
        scanLeDevice()
        Log.i("Device","despues de la funcion")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_ENABLE_BT){
            if(resultCode == Activity.RESULT_OK){
                if(bluetoothAdapter!!.isEnabled){
                    Toast.makeText(this, "Se a activado el Bluetooth.", Toast.LENGTH_SHORT).show()
                }
                else{
                    Toast.makeText(this, "No se ha activado el Bluetooth", Toast.LENGTH_SHORT).show()
                }
            }
            else if (resultCode == Activity.RESULT_CANCELED){
                Toast.makeText(this, "Se ha cancelado la activacion del Bluetooth", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val isLocationPermissionGranted
        get() = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)

    fun Context.hasPermission(permissionType: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permissionType) ==
                PackageManager.PERMISSION_GRANTED
    }


    private fun scanLeDevice() {
        if (!scanning) { // Stops scanning after a pre-defined scan period.
            /*handler.postDelayed({
                scanning = false
                Log.i("Device","termino dentro del if")
                bluetoothLeScanner.stopScan(leScanCallback)
            }, SCAN_PERIOD)*/
            scanning = true
            Log.i("Device","empezo el Scann")
            bluetoothLeScanner.startScan(leScanCallback)
        } else {
            Log.i("Device","en el else")
            scanning = false
            bluetoothLeScanner.stopScan(leScanCallback)
        }
    }
    private val leScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            with(result.device) {
                Log.i("ScanCallback", "Found BLE device! Name: ${name ?: "Unnamed"}, address: $address")
            }
        }
    }

}

