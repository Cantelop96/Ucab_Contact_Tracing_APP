package com.christianantelo.ucabcovid_19contacttracing.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.christianantelo.ucabcovid_19contacttracing.R
import kotlinx.android.synthetic.main.fragment_formulario_sin_sintomas.*

class FormularioSinSintomasFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_formulario_sin_sintomas, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_return_to_main_sin_sintomas.setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.action_formularioSinSintomasFragment_to_main_View)
        }
    }
}