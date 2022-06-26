package com.christianantelo.ucabcovid_19contacttracing.KeyGenerator_SplashScreen

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.christianantelo.ucabcovid_19contacttracing.DataClasses.Application
import com.christianantelo.ucabcovid_19contacttracing.DataClasses.Application.Companion.pref
import com.christianantelo.ucabcovid_19contacttracing.R
import com.christianantelo.ucabcovid_19contacttracing.ui.MainActivity
import kotlinx.android.synthetic.main.activity_first_time_key_gen.*
import java.security.KeyPairGenerator
import kotlin.properties.Delegates

class FirstTimeKeyGen_Activity : AppCompatActivity() {

    companion object {
        var isBackgroundPermissionGranted by Delegates.notNull<Boolean>()
        var isLocationPermissionGranted by Delegates.notNull<Boolean>()
    }

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isBackgroundPermissionGranted = false
        isLocationPermissionGranted = false

        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                isLocationPermissionGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION]
                    ?: isLocationPermissionGranted
                isBackgroundPermissionGranted =
                    permissions[Manifest.permission.ACCESS_BACKGROUND_LOCATION]
                        ?: isBackgroundPermissionGranted
            }
        var privateKey = Application.pref.getKey()
        setContentView(R.layout.activity_first_time_key_gen)


        val firsttime = Application.pref.getFirstTime()
        if (privateKey == "0".toLong()) {
            requestPermissions()
            Application.pref.saveFirstTime(false) // persist value of false
            val generator = KeyPairGenerator.getInstance("RSA")
            val pair = generator.generateKeyPair()
            privateKey = read4BytesFromBuffer(pair.private.encoded).toLong()
            btn_aceptarTerminosyCondiciones.setOnClickListener {
                if (isBackgroundPermissionGranted) {
                    Application.pref.saveKey(privateKey)
                    pref.saveContactTracingState(true)
                    Log.i("Inicio", "Clave Generada $privateKey")
                    goToMain()
                } else {
                    Toast.makeText(
                        this,
                        "Es necesario que se accepten los permisos para el correcto funcionamiento de la Aplicacion",
                        Toast.LENGTH_SHORT
                    ).show()
                    requestPermissions()
                }


            }
        } else {
            Log.i("Inicio", "Directo al Main $firsttime + $privateKey"
            )
            requestPermissions()
            goToMain()
        }
    }


    private fun requestPermissions() {
        Log.i("Callback", "DENTRO DE REQUEST PERMISOS")

        isBackgroundPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED

        isLocationPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

        val permissionRequest: MutableList<String> = ArrayList()

        if (!isLocationPermissionGranted) {
            permissionRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (!isBackgroundPermissionGranted) {
            permissionRequest.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
        if (permissionRequest.isNotEmpty()) {

            permissionLauncher.launch(permissionRequest.toTypedArray())
        }
    }


    private fun goToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // close this activity
    }

    fun read4BytesFromBuffer(buffer: ByteArray, offset: Int = 0): Int {
        return (buffer[offset + 3].toInt() shl 24) or
                (buffer[offset + 2].toInt() and 0xff shl 16) or
                (buffer[offset + 1].toInt() and 0xff shl 8) or
                (buffer[offset + 0].toInt() and 0xff)
    }

}