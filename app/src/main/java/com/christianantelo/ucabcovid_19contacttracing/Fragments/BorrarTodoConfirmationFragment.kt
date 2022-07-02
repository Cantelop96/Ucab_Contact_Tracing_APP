package com.christianantelo.ucabcovid_19contacttracing.Fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.christianantelo.ucabcovid_19contacttracing.DataClasses.Application
import com.christianantelo.ucabcovid_19contacttracing.DataClasses.Application.Companion.pref
import com.christianantelo.ucabcovid_19contacttracing.R
import com.christianantelo.ucabcovid_19contacttracing.storage.ContactTracingDatabase
import com.christianantelo.ucabcovid_19contacttracing.ui.MainActivity
import kotlinx.android.synthetic.main.fragment_borrar_todo_confirmation.*
import kotlin.system.exitProcess

class BorrarTodoConfirmationFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_borrar_todo_confirmation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btn_delete_all_info_confirmado.setOnClickListener {
            pref.saveFirstTime(true)
            pref.saveKey(0)
            if (pref.getCuarentenaState()) {
                (activity as MainActivity).finalizarCuarentenaAlarma()
                pref.saveCuarentenaState(false)
            }
            pref.deleteall()
            (activity as MainActivity).stopContactTracing()
            (activity as MainActivity).deleteAllInfo()
            (activity as MainActivity).finish()
        }
    }

}