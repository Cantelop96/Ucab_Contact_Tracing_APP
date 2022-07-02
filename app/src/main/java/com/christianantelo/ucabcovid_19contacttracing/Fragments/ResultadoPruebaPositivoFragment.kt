package com.christianantelo.ucabcovid_19contacttracing.Fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.christianantelo.ucabcovid_19contacttracing.DataClasses.Application
import com.christianantelo.ucabcovid_19contacttracing.DataClasses.Application.Companion.pref
import com.christianantelo.ucabcovid_19contacttracing.DataClasses.Preferencias
import com.christianantelo.ucabcovid_19contacttracing.R
import com.christianantelo.ucabcovid_19contacttracing.ui.MainActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_resultado_prueba_positivo.*
import kotlinx.android.synthetic.main.fragment_sobreel_app.*
import java.util.*

class ResultadoPruebaPositivoFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_resultado_prueba_positivo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fsdb = Firebase.firestore
        val key = Application.pref.getKey()
        val diaDeInfeccion = Calendar.getInstance().time
        val contactoInfectado = hashMapOf(
            "key" to key,
            "Fecha" to diaDeInfeccion
        )
        Log.i("Subir Infectados",
            "en el onview created con key:$key y dia de infeccion:$diaDeInfeccion" +
                    "Contacto infectado:$contactoInfectado")

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object :
            OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_resultadoPruebaPositivoFragment_to_main_View)
            }
        })

        btn_return_to_main_positivo.setOnClickListener {
            fsdb.collection("Infectados").add(contactoInfectado)
                .addOnSuccessListener { documentReference ->
                    Log.i("Subir Infectados",
                        "Contacto Infectado Añadido con ID: ${documentReference.id}")
                }
                .addOnFailureListener { e ->
                    Log.i("Subir Infectados", "Error subiendo contacto infectado", e)
                }
            Log.d("Subir Infectados",
                "Saliendo de publicar el contacto infectado")
            (activity as MainActivity).stopContactTracing()
            (activity as MainActivity).setFinalizarCuarentenaAlarm()
            pref.saveCuarentenaState(true)
            pref.saveContactTracingState(false)
            Navigation.findNavController(it)
                .navigate(R.id.action_resultadoPruebaPositivoFragment_to_main_View)
        }

    }

    override fun onResume() {
        super.onResume()
        val key = Application.pref.getKey()
        val diaDeInfeccion = Calendar.getInstance().time
        val contactoInfectado = hashMapOf(
            "key" to key,
            "Fecha" to diaDeInfeccion
        )
        Log.i("Subir Infectados",
            "en el onview created con key:$key y dia de infeccion:$diaDeInfeccion" +
                    "Contacto infectado:$contactoInfectado")
    }


}