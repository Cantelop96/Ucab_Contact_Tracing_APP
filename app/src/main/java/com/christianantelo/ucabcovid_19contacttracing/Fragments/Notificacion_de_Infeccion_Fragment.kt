package com.christianantelo.ucabcovid_19contacttracing.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.christianantelo.ucabcovid_19contacttracing.DataClasses.Application
import com.christianantelo.ucabcovid_19contacttracing.DataClasses.Application.Companion.pref
import com.christianantelo.ucabcovid_19contacttracing.R
import com.christianantelo.ucabcovid_19contacttracing.ui.MainActivity
import kotlinx.android.synthetic.main.fragment_notificacion_de__infeccion.*


class Notificacion_de_Infeccion_Fragment : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notificacion_de__infeccion, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

            (activity as MainActivity).stopContactTracing()
            (activity as MainActivity).setFinalizarCuarentenaAlarm()
            pref.saveCuarentenaState(true)
            pref.saveContactTracingState(false)
        btn_return_to_main_notificacion_infeccion.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_notificacion_de_Infeccion_Fragment_to_main_View)

        }
    }

}