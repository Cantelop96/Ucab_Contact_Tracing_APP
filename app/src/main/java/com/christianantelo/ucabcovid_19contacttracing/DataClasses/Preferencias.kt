package com.christianantelo.ucabcovid_19contacttracing.DataClasses

import android.content.Context
import java.util.*


class Preferencias(val context: Context) {
    val storage = context.getSharedPreferences("Pref", 0)
    fun saveKey(privateKey: Long) {
        storage.edit().putLong("privKey", privateKey).apply()
    }

    fun saveContactTracingState(Active: Boolean) {
        storage.edit().putBoolean("contactTracingState", Active).apply()
    }

    fun saveFirstTime(firstTime: Boolean) {
        storage.edit().putBoolean("firstTime", firstTime).apply()
    }

    fun getKey(): Long {
        return storage.getLong("privKey", "0".toLong())!!
    }

    fun getContactTracingState(): Boolean {
        return storage.getBoolean("contactTracingState", true)
    }

    fun getFirstTime(): Boolean {
        return storage.getBoolean("firstTime", true)
    }

    fun deleteall() {
        storage.edit().clear()
    }

    fun saveCuarentenaState(state:Boolean) {
        storage.edit().putBoolean("CuarentenaState", state)
    }

    fun getCuarentenaState(): Boolean {
        return storage.getBoolean("CuarentenaState", false)
    }

}