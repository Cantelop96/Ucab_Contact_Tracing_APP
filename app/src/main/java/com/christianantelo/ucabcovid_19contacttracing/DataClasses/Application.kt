package com.christianantelo.ucabcovid_19contacttracing.DataClasses

import android.app.Application

class Application : Application() {
    companion object {
        lateinit var pref: Preferencias
    }

    override fun onCreate() {
        super.onCreate()
        pref = Preferencias(applicationContext)
    }
}