package com.christianantelo.ucabcovid_19contacttracing.storage

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.christianantelo.ucabcovid_19contacttracing.MainActivity
import com.christianantelo.ucabcovid_19contacttracing.R
import com.christianantelo.ucabcovid_19contacttracing.core.PkSave

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        val pkSave = PkSave(this)

        if(pkSave.getPk() == null)
        {
            startActivity(Intent(this, GenerateLKey_Activity::class.java))
            finish()
        }
        else
            {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }


    }
}