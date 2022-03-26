package com.christianantelo.ucabcovid_19contacttracing.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.christianantelo.ucabcovid_19contacttracing.R
import kotlinx.android.synthetic.main.fragment_formulario_con_sintomas.*

class FormularioConSintomasFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_formulario_con_sintomas, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,object:
            OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_formularioConSintomasFragment_to_main_View)
            }
        })

        btn_return_to_main_con_sintomas.setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.action_formularioConSintomasFragment_to_main_View)
        }

    }
}