package com.christianantelo.ucabcovid_19contacttracing.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.christianantelo.ucabcovid_19contacttracing.R
import kotlinx.android.synthetic.main.fragment_resultado_prueba_positivo.*
import kotlinx.android.synthetic.main.fragment_sobreel_app.*

class ResultadoPruebaPositivoFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_resultado_prueba_positivo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,object:
            OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_resultadoPruebaPositivoFragment_to_main_View)
            }
        })

        btn_return_to_main_positivo.setOnClickListener{
            Navigation.findNavController(it).navigate(R.id.action_resultadoPruebaPositivoFragment_to_main_View)
        }

    }


}