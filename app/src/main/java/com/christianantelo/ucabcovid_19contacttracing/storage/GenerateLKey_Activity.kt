package com.christianantelo.ucabcovid_19contacttracing.storage

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.christianantelo.ucabcovid_19contacttracing.R
import java.io.FileOutputStream
import java.security.KeyPairGenerator
import java.util.*
import kotlin.random.Random


class GenerateLKey_Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generate_lkey)

        val generator = KeyPairGenerator.getInstance("RSA")
        val pair = generator.generateKeyPair()
        val privateKey = pair.private
        val publicKey = pair.public
        val random = Random(1239984)
        Log.e("Error", random.toString())

/*        val outputStream = FileOutputStream("generated_privkey.pem")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            outputStream.write(Base64.getEncoder().encode(privateKey.encoded))
            outputStream.close()
        }*/
        Log.e("ERROR",privateKey.toString())

    }
}