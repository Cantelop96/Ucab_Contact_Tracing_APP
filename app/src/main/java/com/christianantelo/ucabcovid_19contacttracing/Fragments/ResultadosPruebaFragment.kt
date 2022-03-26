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
import kotlinx.android.synthetic.main.fragment_resultados_prueba.*

class ResultadosPruebaFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_resultados_prueba, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btn_siguiente_pruebas.setOnClickListener{
            if (rb_resultado_positivo.isChecked){
                Navigation.findNavController(it).navigate(R.id.action_resultadosPruebaFragment_to_resultadoPruebaPositivoFragment)
            }
            else{
                Navigation.findNavController(it).navigate(R.id.action_resultadosPruebaFragment_to_main_View)
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,object:
            OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_resultadosPruebaFragment_to_main_View)
            }
        })
    }


}