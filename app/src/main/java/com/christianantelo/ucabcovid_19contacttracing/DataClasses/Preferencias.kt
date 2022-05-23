package com.christianantelo.ucabcovid_19contacttracing.DataClasses

import android.content.Context


class Preferencias(val context: Context) {
    val storage = context.getSharedPreferences("Pref", 0)
    fun saveKey(privateKey: Long) {
        storage.edit().putLong("privKey", privateKey).apply()
    }

    fun saveFirstTime(firstTime: Boolean) {
        storage.edit().putBoolean("firstTime", firstTime).apply()
    }

    fun getKey(): Long {
        return storage.getLong("privKey", "0".toLong())!!
    }

    fun getFirstTime(): Boolean {
        return storage.getBoolean("firstTime", true)
    }

    fun deleteall() {
        storage.edit().clear()
    }

}