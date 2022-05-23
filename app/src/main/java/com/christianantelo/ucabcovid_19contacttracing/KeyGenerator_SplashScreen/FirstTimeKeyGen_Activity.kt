package com.christianantelo.ucabcovid_19contacttracing.KeyGenerator_SplashScreen

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.christianantelo.ucabcovid_19contacttracing.DataClasses.Application
import com.christianantelo.ucabcovid_19contacttracing.MainActivity
import com.christianantelo.ucabcovid_19contacttracing.R
import kotlinx.android.synthetic.main.activity_first_time_key_gen.*
import java.security.KeyPairGenerator

class FirstTimeKeyGen_Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var privateKey = Application.pref.getKey()
        setContentView(R.layout.activity_first_time_key_gen)
        val firsttime = Application.pref.getFirstTime()
        if (privateKey == "0".toLong()) {
            Application.pref.saveFirstTime(false) // persist value of false
            val generator = KeyPairGenerator.getInstance("RSA")
            val pair = generator.generateKeyPair()
            privateKey = read4BytesFromBuffer(pair.private.encoded).toLong()
            btn_aceptarTerminosyCondiciones.setOnClickListener {
                Application.pref.saveKey(privateKey)
                Log.i("Inicio", "Clave Generada $privateKey")
                goToMain()
            }
        } else {
            Log.i("Inicio", "Directo al Main $firsttime + $privateKey"
            )
            goToMain()
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