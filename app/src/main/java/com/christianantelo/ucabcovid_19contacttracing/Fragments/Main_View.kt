package com.christianantelo.ucabcovid_19contacttracing.Fragments

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.ui.graphics.vector.PathNode
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.christianantelo.ucabcovid_19contacttracing.DataClasses.Application.Companion.pref
import com.christianantelo.ucabcovid_19contacttracing.DataClasses.ContactTracing
import com.christianantelo.ucabcovid_19contacttracing.R
import com.christianantelo.ucabcovid_19contacttracing.storage.ContactTracingDatabase
import com.christianantelo.ucabcovid_19contacttracing.ui.MainActivity
import kotlinx.android.synthetic.main.fragment_main__view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class Main_View : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main__view, container, false)

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        if (pref.getContactTracingState()) {
            tx_status_contactTracing.text = "El Seguimiento de Contactos Sercanos esta Activo"
            gif_imagen.setImageResource(R.drawable.radar)
        } else {
            tx_status_contactTracing.text = "El Seguimiento de Contactos Sercanos esta Desactivado"
            gif_imagen.setImageResource(R.drawable.warning)
        }

        main_boton_sintomas.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_main_View_to_formularioFragment)
        }
        main_boton_contactTracing.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_main_View_to_settingsFragment)
        }
        main_boton_SobreelApp.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.sobreelAppFragment)
        }
        main_boton_Resultadospruebas.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.resultadosPruebaFragment)
        }

    }



}