package com.christianantelo.ucabcovid_19contacttracing.core

import android.content.Context
import android.util.Log

class PkSave(val context: Context){

        val Name = "MyPublicKey"
        val SHARED_KEY = "MyPk"
        val storage = context.getSharedPreferences(Name, 0)

        fun save (publicKey: String){
            storage.edit().putString(SHARED_KEY,publicKey).apply()
        }

        fun getPk() : String? {
            val pk = storage.getString(SHARED_KEY, null)
            return pk
        }



    }
