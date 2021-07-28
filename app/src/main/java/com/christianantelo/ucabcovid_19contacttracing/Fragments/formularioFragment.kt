package com.christianantelo.ucabcovid_19contacttracing.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.christianantelo.ucabcovid_19contacttracing.R
import kotlinx.android.synthetic.main.fragment_formulario.*
import kotlin.properties.Delegates


class formularioFragment : Fragment() {
    var sintoma_tos by Delegates.notNull<Boolean>()
    var sintoma_fiebre by Delegates.notNull<Boolean>()
    var sintoma_GustoOlor by Delegates.notNull<Boolean>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_formulario, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        btnsiguente_sintomas_boton.setOnClickListener {
            ceckboxcheck()
            if(sintoma_GustoOlor or sintoma_fiebre or sintoma_tos){
                Navigation.findNavController(view).navigate(R.id.action_formularioFragment_to_formularioConSintomasFragment)
            }
            else{
                Navigation.findNavController(view).navigate((R.id.action_formularioFragment_to_formularioSinSintomasFragment))
            }
        }

    }
    fun ceckboxcheck(){
        sintoma_tos = rb_tos.isChecked
        sintoma_fiebre = rb_fiebre_alta.isChecked
        sintoma_GustoOlor = rb_perdidadeOlfatoGusto.isChecked
    }



}