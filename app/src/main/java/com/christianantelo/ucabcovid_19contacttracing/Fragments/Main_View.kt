package com.christianantelo.ucabcovid_19contacttracing.Fragments

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.christianantelo.ucabcovid_19contacttracing.R
import kotlinx.android.synthetic.main.fragment_main__view.*


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

        main_boton_sintomas.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_main_View_to_formularioFragment)
        }
        main_boton_contactTracing.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_main_View_to_settingsFragment)
        }
    }

}