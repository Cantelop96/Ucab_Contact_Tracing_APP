package com.christianantelo.ucabcovid_19contacttracing.Fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.christianantelo.ucabcovid_19contacttracing.DataClasses.Application.Companion.pref
import com.christianantelo.ucabcovid_19contacttracing.R
import com.christianantelo.ucabcovid_19contacttracing.ui.MainActivity
import kotlinx.android.synthetic.main.fragment_formulario_sin_sintomas.*
import kotlinx.android.synthetic.main.fragment_settings.*

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sw_seguimiento_activado.isChecked = pref.getContactTracingState()
        if (sw_seguimiento_activado.isChecked) {
            sw_seguimiento_activado.text = "El Seguimiento de contactos cercanos esta Activado"
        } else {
            sw_seguimiento_activado.text = "El Seguimiento de contactos cercanos esta Desactivado"
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object :
            OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_settingsFragment2_to_main_View)
            }
        })

        btn_delete_all_info.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.action_settingsFragment2_to_borrarTodoConfirmationFragment2)
        }

        sw_seguimiento_activado.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (!pref.getContactTracingState()) {
                    sw_seguimiento_activado.text =
                        "El Seguimiento de contactos cercanos esta Activado"
                    if (pref.getCuarentenaState()) {
                        (activity as MainActivity).finalizarCuarentenaAlarma()
                        pref.saveCuarentenaState(false)
                    }
                    pref.saveContactTracingState(true)
                    Log.i("Settings", "Se activo el seguimiento contacto cercano")
                    (activity as MainActivity).startContactTracing()
                }
            } else {
                sw_seguimiento_activado.text =
                    "El Seguimiento de contactos cercanos esta Desactivado"
                pref.saveContactTracingState(false)
                (activity as MainActivity).stopContactTracing()
                Log.i("Settings", "Se desactivo el seguimiento contacto cercano")

            }
        }
    }
}